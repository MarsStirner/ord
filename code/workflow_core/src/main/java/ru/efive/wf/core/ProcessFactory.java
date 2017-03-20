package ru.efive.wf.core;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import ru.efive.wf.core.activity.InvokeMethodActivity;
import ru.efive.wf.core.activity.ParametrizedPropertyLocalActivity;
import ru.efive.wf.core.activity.SendMailActivity;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.MailMessage;
import ru.efive.wf.core.data.impl.*;
import ru.efive.wf.core.util.EngineHelper;
import ru.entity.model.enums.DocumentAction;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.enums.RoleType;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.entity.model.wf.HumanTaskTree;
import ru.external.ProcessUser;
import ru.external.ProcessedData;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class ProcessFactory {

    private static String getHost() {
        Properties properties = new Properties();
        try {
            properties.load(ProcessFactory.class.getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty("app.host");
    }

    public static <T extends ProcessedData> Process getProcessByType(T t) {
        Process process;
        try {
            System.out.println("process factory initialization");
            process = new Process();
            process.setProcessedData(t);

            Object prop = PropertyUtils.getProperty(t, "id");
            String id = prop == null ? null : prop.toString();

            if (id != null && !id.equals("")) {
                Status<T> currentStatus = new Status<>();
                if (t.getDocumentType().equals(DocumentType.IncomingDocument)) {
                    System.out.println("Initialization process for incoming document");
                    currentStatus = getCurrentStatusInDocument(t, process, prop);
                } else if (t.getDocumentType().equals(DocumentType.OutgoingDocument)) {
                    System.out.println("Initialization process for outgoing document");
                    currentStatus = getCurrentStatusOutDocument(t, process);
                } else if (t.getDocumentType().equals(DocumentType.Task)) {
                    System.out.println("Initialization process for task");
                    currentStatus = getCurrentStatusTask(t, process, prop);
                } else if (t.getDocumentType().equals(DocumentType.InternalDocument)) {
                    System.out.println("Initialization process for internal document");
                    currentStatus = getCurrentStatusInternalDocument(t, process, prop);
                } else if (t.getDocumentType().equals(DocumentType.RequestDocument)) {
                    System.out.println("Initialization process for request document");
                    currentStatus = getCurrentStatusRequestDocument(t, process, prop);
                } else if (t.getDocumentType().equals(DocumentType.OfficeKeepingFile)) {
                    System.out.println("Initialization process for office keeping file");
                    currentStatus = getCurrentStatusOfficeKeepingFile(t, process);
                    System.out.println("new process initialization");
                } else if (t.getDocumentType().equals(DocumentType.OfficeKeepingVolume)) {
                    System.out.println("Initialization process for office keeping volume");
                    currentStatus = getCurrentStatusOfficeKeepingVolume(t, process);
                }
                if (currentStatus != null) {
                    process.setCurrentStatus(currentStatus);
                }
                System.out.println("new process initialization");
            } else {
                System.out.println("Document id is null or equal blank string. Abort.");
            }
        } catch (Exception e) {
            System.out.println("exception in initialization process");
            process = null;
            e.printStackTrace();
        }
        return process;
    }

    private static <T extends ProcessedData> Status<T> getCurrentStatusInternalDocument(T t, final Process process, Object prop) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<DocumentStatus, Status<T>> statuses = new HashMap<>();
        prop = PropertyUtils.getProperty(t, "id");
        String id = prop == null ? null : prop.toString();

        String docFormValue = "";
        if (PropertyUtils.getProperty(t, "form") != null) {
            prop = PropertyUtils.getProperty(t, "form.value");
            docFormValue = (prop == null ? "" : (String) prop);
        }
        Set<String> recipients = getRecipients(t);

        Status<T> processStatus = new Status<>();
        processStatus.setStatus(DocumentStatus.DOC_PROJECT_1);
        processStatus.setProcessedData(t);

        StatusChangeAction toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.REDIRECT_TO_CONSIDERATION_1);
        toStatusAction.setInitialStatus(processStatus);

        InvokeMethodActivity activity = new InvokeMethodActivity();
        List<Serializable> list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkInternalDocumentPropertiesForReview", list);

        List<IActivity> activites = new ArrayList<>();
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        Status<T> toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.ON_CONSIDERATION);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        //1-mail
        List<String> sendTo = new ArrayList<>();
        if (!recipients.isEmpty()) {
            sendTo.addAll(recipients);
        }
        //если приказ, то отправить письмо Адресатам, Автору, Руководителя,Контроль исполнения
        prop = PropertyUtils.getProperty(t, "author");
        if (prop != null)
            sendTo.add(((User) prop).getEmail());

        prop = PropertyUtils.getProperty(t, "responsible");
        if (prop != null)
            sendTo.add(((User) prop).getEmail());

        if (sendTo.size() > 0) {
            SendMailActivity mailActivity = new SendMailActivity();
            String subject = "Вы назнaчены ответственным по документу @DocumentNumber";
            String body = "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" +
                    "<a href=\"" + getHost() + "/component/internal/internal_document.xhtml?docId=" +
                    id + "\" >Ссылка на документ</a>";
            MailMessage message = new MailMessage(sendTo, null, subject, body);
            message.setContentType("text/html");
            mailActivity.setMessage(message);
            activites.add(mailActivity);
            toStatus.setPreStatusActivities(activites);
        }

        List<StatusChangeAction> fromStatusActions = new ArrayList<>();
        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);

        // На рассмотрении - Зарегистрирован
        processStatus = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                        if ((RoleType.OFFICE_MANAGER.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };
        toStatusAction.setAction(DocumentAction.CHECK_IN_5);
        toStatusAction.setInitialStatus(processStatus);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumber", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.CHECK_IN_5);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);


        // Проект документа - Зарегистрирован
        processStatus = statuses.get(DocumentStatus.DOC_PROJECT_1);
        fromStatusActions = statuses.get(DocumentStatus.DOC_PROJECT_1).getAvailableActions();
        toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                        if ((RoleType.OFFICE_MANAGER.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        toStatusAction.setAction(DocumentAction.CHECK_IN_55);
        toStatusAction.setInitialStatus(processStatus);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumber", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);
        toStatusAction.setDestinationStatus(toStatus);

        prop = PropertyUtils.getProperty(t, "registrationNumber");
        String docNumber = (prop == null ? "" : (String) prop);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(toStatus.getStatus(), toStatus);

        // Регистрация изменений закрытого периода (Проект документа - Зарегистрирован)
        processStatus = statuses.get(DocumentStatus.DOC_PROJECT_1);
        fromStatusActions = statuses.get(DocumentStatus.DOC_PROJECT_1).getAvailableActions();
        toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();

                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                        if ((RoleType.OFFICE_MANAGER.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        toStatusAction.setAction(DocumentAction.REGISTRATION_CLOSE_PERIOD_551);
        toStatusAction.setInitialStatus(processStatus);

        activites = new ArrayList<>();

        Date defaultRegistrationDate = new Date(Calendar.getInstance().getTimeInMillis() - 1000 * 60 * 60 * 24);
        ParametrizedPropertyLocalActivity localActivity = new ParametrizedPropertyLocalActivity();
        localActivity.setParentAction(toStatusAction);
        InputDateForm dateForm = new InputDateForm();
        dateForm.setBeanName(process.getProcessedData().getBeanName());
        dateForm.setActionDateField("registrationDate");
        dateForm.setActionDate(defaultRegistrationDate);
        dateForm.setScope(EditablePropertyScope.GLOBAL);
        dateForm.setActionCommentaryField("WFResultDescription");
        dateForm.setActionCommentary("Дата регистрации установлена");
        dateForm.setFormTitle("Укажите дату регистрации");
        dateForm.setDateTitle("Дата регистрации");
        localActivity.setDocument(dateForm);
        activites.add(localActivity);

        ParametrizedPropertyLocalActivity localActivity2 = new ParametrizedPropertyLocalActivity();
        localActivity2.setParentAction(toStatusAction);
        InputRegistrationNumberForm registrationNumberForm = new InputRegistrationNumberForm();
        registrationNumberForm.setBeanName("internal_doc");
        registrationNumberForm.setActionCommentaryField("registrationNumber");
        registrationNumberForm.setScope(EditablePropertyScope.GLOBAL);
        localActivity2.setDocument(registrationNumberForm);
        activites.add(localActivity2);

        toStatusAction.setLocalActivities(activites);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumberOnOutDate", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(toStatus.getStatus(), toStatus);

        // Регистрация изменений закрытого периода (На рассмотрении - Зарегистрирован)
        processStatus = statuses.get(DocumentStatus.ON_CONSIDERATION);
        fromStatusActions = processStatus.getAvailableActions();
        toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                        if ((RoleType.OFFICE_MANAGER.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        toStatusAction.setAction(DocumentAction.REGISTRATION_CLOSE_PERIOD_552);
        toStatusAction.setInitialStatus(processStatus);

        activites = new ArrayList<>();
        defaultRegistrationDate = new Date(Calendar.getInstance().getTimeInMillis());
        localActivity = new ParametrizedPropertyLocalActivity();
        localActivity.setParentAction(toStatusAction);
        dateForm = new InputDateForm();
        dateForm.setBeanName(process.getProcessedData().getBeanName());
        dateForm.setActionDateField("registrationDate");
        dateForm.setActionDate(defaultRegistrationDate);
        dateForm.setScope(EditablePropertyScope.GLOBAL);
        dateForm.setActionCommentaryField("WFResultDescription");
        dateForm.setActionCommentary("Дата регистрации установлена");
        localActivity.setDocument(dateForm);
        activites.add(localActivity);

        localActivity2 = new ParametrizedPropertyLocalActivity();
        localActivity2.setParentAction(toStatusAction);
        registrationNumberForm = new InputRegistrationNumberForm();
        registrationNumberForm.setBeanName("internal_doc");
        registrationNumberForm.setActionCommentaryField("registrationNumber");
        registrationNumberForm.setScope(EditablePropertyScope.GLOBAL);
        localActivity2.setDocument(registrationNumberForm);
        activites.add(localActivity2);

        toStatusAction.setLocalActivities(activites);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumberOnOutDate", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);
        toStatusAction.setDestinationStatus(toStatus);

        prop = PropertyUtils.getProperty(t, "registrationNumber");

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(toStatus.getStatus(), toStatus);

        // Проект документа - Согласование
        processStatus = statuses.get(DocumentStatus.DOC_PROJECT_1);
        fromStatusActions = statuses.get(DocumentStatus.DOC_PROJECT_1).getAvailableActions();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.REDIRECT_TO_AGREEMENT);
        toStatusAction.setInitialStatus(processStatus);

        Status<T> agreeStatus = new Status<>();
        agreeStatus.setStatus(DocumentStatus.AGREEMENT_3);
        agreeStatus.setProcessedData(t);
        agreeStatus.setAgreementEnabled(true);

        //List<String> sendTo=new ArrayList<String>();
        activites = new ArrayList<>();
        Object agreementTree = PropertyUtils.getProperty(t, "agreementTree");
        sendTo = new ArrayList<>();

        if (agreementTree != null) {
            HumanTaskTree tree = (HumanTaskTree) agreementTree;
            //List<String> sendTo = EngineHelper.doGenerateAgreementPrimaryNotificationList(tree);
            //List<String> blindCopyTo = new ArrayList<String>();
            InvokeMethodActivity addAgreementUsersActivity;
            Set<User> executors = EngineHelper.doGenerateAgreementPrimaryExecutors(tree);
            if (executors.size() > 0) {
                addAgreementUsersActivity = new InvokeMethodActivity();
                list = new ArrayList<>();
                list.add(t);
                list.add(new ArrayList(executors));
                addAgreementUsersActivity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "addToDocumentAgreementUsers", list);
                activites.add(addAgreementUsersActivity);
            }

            for (User executor : executors) {
                if ((executor.getEmail() != null) && (!executor.getEmail().isEmpty())) {
                    sendTo.add(executor.getEmail());
                }
            }
        }

        if (!recipients.isEmpty()) sendTo.addAll(recipients);

        prop = PropertyUtils.getProperty(t, "author");
        if (prop != null)
            sendTo.add(((User) prop).getEmail());

        prop = PropertyUtils.getProperty(t, "responsible");
        if (prop != null)
            sendTo.add(((User) prop).getEmail());

        if (sendTo.size() > 0) {
            MailMessage message = new MailMessage(sendTo, null, "Новый запрос на согласование",
                    "Новый запрос на согласование\n\n" + "<a href=\"" + getHost() + "/component/internal/internal_document.xhtml?docId=" +
                            process.getProcessedData().getId() + "\" >Ссылка на документ</a>");
            message.setContentType("text/html");
            SendMailActivity mailActivity = new SendMailActivity();
            mailActivity.setMessage(message);
            activites.add(mailActivity);
        }

        agreeStatus.setPreStatusActivities(activites);
        toStatusAction.setDestinationStatus(agreeStatus);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);

        // На согласовании - Зарегистрирован
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if (RoleType.ADMINISTRATOR.equals(role.getRoleType())) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        toStatusAction.setAction(DocumentAction.CHECK_IN_6);
        toStatusAction.setInitialStatus(agreeStatus);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumber", list);
        activites.add(activity);

        toStatusAction.setPreActionActivities(activites);

        fromStatusActions.add(toStatusAction);
        agreeStatus.setAvailableActions(fromStatusActions);

        statuses.put(agreeStatus.getStatus(), agreeStatus);

        // Регистрация изменений закрытого периода (На согласовании - Зарегистрирован)
        processStatus = statuses.get(DocumentStatus.AGREEMENT_3);
        fromStatusActions = processStatus.getAvailableActions();
        toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                        if ((RoleType.OFFICE_MANAGER.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        toStatusAction.setAction(DocumentAction.REGISTRATION_CLOSE_PERIOD_661);
        toStatusAction.setInitialStatus(processStatus);

        activites = new ArrayList<>();
        defaultRegistrationDate = new Date(Calendar.getInstance().getTimeInMillis());
        localActivity = new ParametrizedPropertyLocalActivity();
        localActivity.setParentAction(toStatusAction);
        dateForm = new InputDateForm();
        dateForm.setBeanName(process.getProcessedData().getBeanName());
        dateForm.setActionDateField("registrationDate");
        dateForm.setActionDate(defaultRegistrationDate);
        dateForm.setScope(EditablePropertyScope.GLOBAL);
        dateForm.setActionCommentaryField("WFResultDescription");
        dateForm.setActionCommentary("Дата регистрации установлена");
        localActivity.setDocument(dateForm);
        activites.add(localActivity);

        localActivity2 = new ParametrizedPropertyLocalActivity();
        localActivity2.setParentAction(toStatusAction);
        registrationNumberForm = new InputRegistrationNumberForm();
        registrationNumberForm.setBeanName("internal_doc");
        registrationNumberForm.setActionCommentaryField("registrationNumber");
        registrationNumberForm.setScope(EditablePropertyScope.GLOBAL);
        localActivity2.setDocument(registrationNumberForm);
        activites.add(localActivity2);

        toStatusAction.setLocalActivities(activites);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setInternalRegistrationNumberOnOutDate", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);
        toStatusAction.setDestinationStatus(toStatus);

        prop = PropertyUtils.getProperty(t, "registrationNumber");
        docNumber = (prop == null ? "" : (String) prop);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(toStatus.getStatus(), toStatus);

        //Зарегистрирован - На исполнении
        processStatus = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.REDIRECT_TO_EXECUTION_80);
        toStatusAction.setCommentNecessary(true);
        toStatusAction.setInitialStatus(processStatus);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.ON_EXECUTION_80);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);


        activites = new ArrayList<>();
        //1-mail
        sendTo = new ArrayList<>();

        prop = PropertyUtils.getProperty(t, "responsible");
        User executor = (prop == null ? null : (User) prop);

        if (executor != null) {
            if ((executor.getEmail() != null) && (!executor.getEmail().isEmpty())) {
                sendTo.add(executor.getEmail());
            }
        }

        if (!recipients.isEmpty()) sendTo.addAll(recipients);

        //если приказ, то отправить письмо Адресатам, Автору, Руководителя,Контроль исполнения
        prop = PropertyUtils.getProperty(t, "author");
        if (prop != null)
            sendTo.add(((User) prop).getEmail());

        prop = PropertyUtils.getProperty(t, "responsible");
        if (prop != null)
            sendTo.add(((User) prop).getEmail());

        if (sendTo.size() > 0) {
            SendMailActivity mailActivity = new SendMailActivity();
            String subject = "Вы назнaчены ответственным по документу @DocumentNumber";
            String body = "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" +
                    "<a href=\"" + getHost() + "/component/internal/internal_document.xhtml?docId=" +
                    id + "\" >Ссылка на документ</a>";
            MailMessage message = new MailMessage(sendTo, null, subject, body);
            message.setContentType("text/html");
            mailActivity.setMessage(message);
            activites.add(mailActivity);
            toStatus.setPreStatusActivities(activites);
        }

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);

        activites = new ArrayList<>();

        //На исполнении - Исполнен
        processStatus = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.EXECUTE_80);
        toStatusAction.setCommentNecessary(true);
        toStatusAction.setInitialStatus(processStatus);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.EXECUTE);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        prop = PropertyUtils.getProperty(t, "responsible");
        User responsibleUser = (prop == null ? null : (User) prop);

        prop = PropertyUtils.getProperty(t, "controller");
        User controller = (prop == null ? null : (User) prop);

        sendTo = new ArrayList<>();

        if (!recipients.isEmpty()) sendTo.addAll(recipients);

        prop = PropertyUtils.getProperty(t, "author");
        if (prop != null)
            sendTo.add(((User) prop).getEmail());

        prop = PropertyUtils.getProperty(t, "responsible");
        if (prop != null)
            sendTo.add(((User) prop).getEmail());

        if (responsibleUser != null && controller != null) {
            if ((responsibleUser.getEmail() != null) && (!responsibleUser.getEmail().isEmpty())) {
                sendTo.add(responsibleUser.getEmail());
            }
            if ((controller.getEmail() != null) && (!controller.getEmail().isEmpty())) {
                sendTo.add(controller.getEmail());
            }
            if (sendTo.size() > 0) {
                SendMailActivity mailActivity = new SendMailActivity();
                String subject = "Вы назнaчены ответственным по документу @DocumentNumber";
                String body = "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" +
                        "<a href=\"" + getHost() + "/component/internal/internal_document.xhtml?docId=" +
                        id + "\" >Ссылка на документ</a>";
                MailMessage message = new MailMessage(sendTo, null, subject, body);
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                activites.add(mailActivity);
                toStatus.setPreStatusActivities(activites);
            }
        }

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);

        //Исполнен - Архив
        processStatus = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.IN_ARCHIVE_90);
        toStatusAction.setInitialStatus(processStatus);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkInternalPropertiesForArchiving", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.IN_ARCHIVE_100);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);
        statuses.put(toStatus.getStatus(), toStatus);

        //На рассмотрении - Отказ
        processStatus = statuses.get(DocumentStatus.ON_CONSIDERATION);
        fromStatusActions = statuses.get(DocumentStatus.ON_CONSIDERATION).getAvailableActions();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.CANCEL_150);
        toStatusAction.setInitialStatus(processStatus);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.CANCEL_150);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        ParametrizedPropertyLocalActivity localActivity3 = new ParametrizedPropertyLocalActivity();
        localActivity3.setParentAction(toStatusAction);
        InputReasonForm reasonForm = new InputReasonForm();
        reasonForm.setBeanName("internal_doc");
        reasonForm.setActionCommentaryField("WFResultDescription");
        reasonForm.setScope(EditablePropertyScope.LOCAL);
        localActivity3.setDocument(reasonForm);
        activites.add(localActivity3);
        toStatusAction.setLocalActivities(activites);

        prop = PropertyUtils.getProperty(t, "author");
        User author = (prop == null ? null : (User) prop);

        if (author != null) {
            sendTo = new ArrayList<>();
            if (StringUtils.isNotEmpty(author.getEmail())) {
                sendTo.add(author.getEmail());
            }
            if (recipients.size() > 0 && !docFormValue.equals("Приказ")) {
                sendTo.addAll(recipients);
            }
            if (sendTo.size() > 0) {
                activites = new ArrayList<>();
                SendMailActivity mailActivity = new SendMailActivity();
                String subject = "Ваш документ перешел перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"";
                String body = "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" +
                        "<a href=\"" + getHost() + "/component/internal/internal_document.xhtml?docId=" +
                        id + "\" >Ссылка на документ</a>";
                MailMessage message = new MailMessage(sendTo, null, subject, body);
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                activites.add(mailActivity);
                toStatus.setPreStatusActivities(activites);
            }
        }

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);

        //Изменить уровень допуска
        List<NoStatusAction> noStatusActions = new ArrayList<>();
        NoStatusAction changeAccessLevelAction = new NoStatusAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    if (getProcess().getProcessedData().getDocumentStatus().getId() == 1) return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        };
        changeAccessLevelAction.setAction(DocumentAction.CHANGE_ACCESS_LEVEL);

        activites = new ArrayList<>();

        ParametrizedPropertyLocalActivity localActivity4 = new ParametrizedPropertyLocalActivity();
        localActivity4.setParentAction(changeAccessLevelAction);
        SelectAccessLevelForm accessLevelForm = new SelectAccessLevelForm();
        accessLevelForm.setBeanName(process.getProcessedData().getBeanName());
        accessLevelForm.setSelectedAccessLevelField("userAccessLevel");
        accessLevelForm.setScope(EditablePropertyScope.GLOBAL);
        localActivity4.setDocument(accessLevelForm);
        activites.add(localActivity4);

        changeAccessLevelAction.setLocalActivities(activites);

        noStatusActions.add(changeAccessLevelAction);

        process.setNoStatusActions(noStatusActions);

        System.out.println("new process initialization");

        return statuses.get(t.getDocumentStatus());
    }

    private static <T extends ProcessedData> SendMailActivity getMailActivity(MailMessage message) {
        SendMailActivity mailActivity = new SendMailActivity();
        message.setContentType("text/html");
        mailActivity.setMessage(message);
        return mailActivity;
    }

    // Получить адресатов
    private static <T extends ProcessedData> Set<String> getRecipients(T t)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object properties;
        Set<String> recipients = new HashSet<>();

        properties = PropertyUtils.getProperty(t, "recipientUsers");
        Set<User> recipientUsers = (properties == null ? null : (Set<User>) properties);

        if (recipientUsers != null) {
            for (User user : recipientUsers) {
                if ((user.getEmail() != null) && (!user.getEmail().isEmpty())) {
                    recipients.add(user.getEmail());
                }
            }
        }

        properties = PropertyUtils.getProperty(t, "recipientGroups");
        Set<Group> recipientGroups = (properties == null ? null : (Set<Group>) properties);
        if (recipientGroups != null) {
            for (Group group : recipientGroups) {
                if (group != null) {
                    Set<User> members = group.getMembers();
                    for (User user : members) {
                        if ((user.getEmail() != null) && (!user.getEmail().isEmpty())) {
                            recipients.add(user.getEmail());
                        }
                    }
                }
            }
        }
        return recipients;
    }

    private static <T extends ProcessedData> Status<T> getCurrentStatusRequestDocument(T t, Process process, Object prop) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<DocumentStatus, Status<T>> statuses = new HashMap<>();
        prop = PropertyUtils.getProperty(t, "id");
        String id = prop == null ? null : prop.toString();

        // На регистрации - Зарегистрирован
        Status<T> processStatus = new Status<>();
        processStatus.setStatus(DocumentStatus.ON_REGISTRATION);
        processStatus.setProcessedData(t);
        List<StatusChangeAction> fromStatusActions = new ArrayList<>();
        StatusChangeAction toStatusAction = new StatusChangeAction(process);

        toStatusAction.setAction(DocumentAction.CHECK_IN_1);
        toStatusAction.setInitialStatus(processStatus);

        List<IActivity> activites = new ArrayList<>();
        InvokeMethodActivity activity = new InvokeMethodActivity();
        List<Serializable> list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setRequestRegistrationNumber", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        Status<T> toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.CHECK_IN_2);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        SendMailActivity mailActivity = null;
        prop = PropertyUtils.getProperty(t, "registrationNumber");
        String docNumber = (prop == null ? "" : (String) prop);

        //2-mail
        List<String> sendTo = new ArrayList<>();
        Set<String> recipients = new HashSet<>();
        activites = new ArrayList<>();
        prop = PropertyUtils.getProperty(t, "recipientUsers");
        Set<User> recipientUsers = (prop == null ? null : (Set<User>) prop);
        if (recipientUsers != null) {
            for (User user : recipientUsers) {
                if ((user.getEmail() != null) && (!user.getEmail().isEmpty())) {
                    recipients.add(user.getEmail());
                }
            }
        }

        prop = PropertyUtils.getProperty(t, "recipientGroups");
        Set<Group> recipientGroups = (prop == null ? null : (Set<Group>) prop);
        if (recipientGroups != null) {
            for (Group group : recipientGroups) {
                if (group != null) {
                    Set<User> members = group.getMembers();
                    for (User user : members) {
                        if ((user.getEmail() != null) && (!user.getEmail().isEmpty())) {
                            recipients.add(user.getEmail());
                        }
                    }
                }
            }
        }

        if (recipients.size() > 0) {
            mailActivity = new SendMailActivity();
            MailMessage message2 = new MailMessage(new ArrayList(recipients), null, "Вам адресован документ @DocumentNumber" + docNumber,
                    "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" + "<a href=\"" + getHost() + "/component/request/request_document.xhtml?docId=" +
                            id + "\" >Ссылка на документ</a>");
            message2.setContentType("text/html");
            mailActivity.setMessage(message2);
            activites.add(mailActivity);
            toStatus.setPreStatusActivities(activites);
        }
        if (mailActivity != null) {
            activites.add(mailActivity);
            mailActivity = null;
        }
        if (activites.size() > 0) {
            toStatus.setPreStatusActivities(activites);
        }

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);


        //Зарегистрирован - На исполнении
        processStatus = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.REDIRECT_TO_EXECUTION_2);
        toStatusAction.setCommentNecessary(true);
        toStatusAction.setInitialStatus(processStatus);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.ON_EXECUTION_80);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        sendTo.clear();
        mailActivity = null;
        prop = PropertyUtils.getProperty(t, "responsible");
        User responsible = (prop == null ? null : (User) prop);
        if (responsible != null && StringUtils.isNotEmpty(responsible.getEmail())) {
            mailActivity = new SendMailActivity();
            sendTo.add(responsible.getEmail());
            MailMessage message1 = new MailMessage(sendTo, null, "Вы назнaчены ответственным по документу @DocumentNumber" + docNumber,
                    "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" + "<a href=\"" + getHost() + "/component/request/request_document.xhtml?docId=" +
                            id + "\" >Ссылка на документ</a>");
            message1.setContentType("text/html");
            mailActivity.setMessage(message1);
        }
        if (mailActivity != null) {
            activites.add(mailActivity);
            mailActivity = null;
        }
        if (activites.size() > 0) {
            toStatus.setPreStatusActivities(activites);
        }

        statuses.put(processStatus.getStatus(), processStatus);

        //На исполнении - Исполнен
        processStatus = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.EXECUTE_80);
        toStatusAction.setCommentNecessary(true);
        toStatusAction.setInitialStatus(processStatus);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.EXECUTE);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);

        //Исполнен - Архив
        processStatus = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.IN_ARCHIVE_90);
        toStatusAction.setInitialStatus(processStatus);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.IN_ARCHIVE_100);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkRequestPropertiesForArchiving", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);
        statuses.put(toStatus.getStatus(), toStatus);

        System.out.println("new process initialization");

        return statuses.get(t.getDocumentStatus());
    }

    private static <T extends ProcessedData> Status<T> getCurrentStatusOfficeKeepingFile(T t, Process process) {
        Map<DocumentStatus, Status<T>> statuses = new HashMap<>();

        // На регистрации - Зарегистрирован
        Status<T> processStatus = new Status<>();
        processStatus.setStatus(DocumentStatus.PROJECT);
        processStatus.setProcessedData(t);
        List<StatusChangeAction> fromStatusActions = new ArrayList<>();
        StatusChangeAction toStatusAction = new StatusChangeAction(process);

        toStatusAction.setAction(DocumentAction.CHECK_IN_1);
        toStatusAction.setInitialStatus(processStatus);

        List<IActivity> activites = new ArrayList<>();
        InvokeMethodActivity activity = new InvokeMethodActivity();
        List<Serializable> list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOfficeKeepingFileRegistrationNumber", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        Status<T> toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.REGISTERED);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        processStatus.setAvailableActions(fromStatusActions);

        statuses.put(processStatus.getStatus(), processStatus);
        statuses.put(toStatus.getStatus(), toStatus);

        return statuses.get(t.getDocumentStatus());
    }

    private static <T extends ProcessedData> Status<T> getCurrentStatusOfficeKeepingVolume(T t, Process process) {
        Map<DocumentStatus, Status<T>> statuses = new HashMap<>();
        // Проект тома - Открыт
        Status<T> status = new Status<>();
        status.setStatus(DocumentStatus.VOLUME_PROJECT);
        status.setProcessedData(t);

        StatusChangeAction toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.CHECK_IN_1);
        toStatusAction.setInitialStatus(status);

        List<IActivity> activites = new ArrayList<>();
        InvokeMethodActivity activity = new InvokeMethodActivity();
        List<Serializable> list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOfficeKeepingVolumeRegistrationNumber", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        Status<T> toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.OPEN);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        List<StatusChangeAction> fromStatusActions = new ArrayList<>();
        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        // Открыт - Закрыт
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);

        toStatusAction.setAction(DocumentAction.CLOSE);
        toStatusAction.setInitialStatus(status);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.CLOSE);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        //Закрыт - изъят
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.EXTRACT);
        toStatusAction.setInitialStatus(status);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkOfficeKeepingVolumePropertiesForUnfile", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.EXTRACT);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        //Изъят из архива - Зарегистрирован
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.RETURN);
        toStatusAction.setInitialStatus(status);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOfficeKeepingVolumeCollectorToEmpty", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        toStatus = statuses.get(DocumentStatus.CLOSE);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        statuses.put(toStatus.getStatus(), toStatus);

        return statuses.get(t.getDocumentStatus());
    }

    private static <T extends ProcessedData> Status<T> getCurrentStatusTask(T t, Process process, Object prop)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<DocumentStatus, Status<T>> statuses = new HashMap<>();
        prop = PropertyUtils.getProperty(t, "id");
        String id = prop == null ? null : prop.toString();

        prop = PropertyUtils.getProperty(t, "author");
        User author = (prop == null ? null : (User) prop);

        prop = PropertyUtils.getProperty(t, "executors");
        Set<User> executors = prop == null ? null : (Set<User>) prop;

        prop = PropertyUtils.getProperty(t, "form");
        Object form = (prop == null ? null : prop);

        prop = null;
        if (form != null) {
            prop = PropertyUtils.getProperty(form, "value");
        }
        String formDescription = (prop == null ? "" : (String) prop);

        prop = PropertyUtils.getProperty(t, "executionDate");
        Date executionDate = (prop == null ? null : (Date) prop);

        // Черновик - На исполнении
        Status<T> status = new Status<>();
        status.setStatus(DocumentStatus.DRAFT);
        status.setProcessedData(t);
        List<StatusChangeAction> fromStatusActions = new ArrayList<>();
        StatusChangeAction toStatusAction = new StatusChangeAction(process);

        toStatusAction.setAction(DocumentAction.REDIRECT_TO_EXECUTION_1);
        toStatusAction.setInitialStatus(status);

        Status<T> toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.ON_EXECUTION_2);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        List<IActivity> activites = new ArrayList<>();
        InvokeMethodActivity activity = new InvokeMethodActivity();
        List<Serializable> list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setTaskRegistrationNumber", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        activites = new ArrayList<>();
        if (executors != null) {
            List<String> sendTo = new ArrayList<>();
            for (User executor : executors) {
                if (StringUtils.isNotEmpty(executor.getEmail())) {
                    sendTo.add(executor.getEmail());
                }
            }
            if (sendTo.size() > 0) {
                SendMailActivity mailActivity = new SendMailActivity();
                String subject = "Поступило новое поручение @DocumentNumber";
                StringBuffer body = new StringBuffer();
                switch (formDescription) {
                    case "resolution":
                        subject = "Поступила новая резолюция @DocumentNumber";
                        body = body.append("К вам поступила новая резолюция на исполнение \n\n");
                        break;
                    case "exercise":
                        prop = PropertyUtils.getProperty(t, "exerciseType");
                        Object exerciseType = (prop == null ? null : prop);

                        prop = null;
                        if (form != null) {
                            prop = PropertyUtils.getProperty(exerciseType, "value");
                        }
                        String exerciseTypeValue = (prop == null ? "" : (String) prop).toLowerCase();

                        if ("обращение".equals(exerciseType) || "заявление".equals(exerciseType)) {
                            subject = "Поступило новое" + exerciseTypeValue + " @DocumentNumber";
                            body = body.append("К вам поступила новое ").append(exerciseTypeValue).append(" на исполнение \n\n");
                        } else {
                            subject = "Поступила новая " + exerciseTypeValue + " @DocumentNumber";
                            body = body.append("К вам поступила новая").append(exerciseTypeValue).append(" на исполнение \n\n");
                        }
                        break;
                    default:
                        body = body.append("К вам поступило новое поручение на исполнение \n\n");
                        break;
                }

                MailMessage message = new MailMessage(sendTo, null, subject, body.append("<a href=\"").append(getHost()).append("/component/task/task.xhtml?docId=").
                        append(id).append("\" >Ссылка на документ</a>").toString());
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                activites.add(mailActivity);
            }
        }
        InvokeMethodActivity postActivity = new InvokeMethodActivity();
        postActivity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "cloneTasks", list);
        activites.add(postActivity);
        toStatus.setPreStatusActivities(activites);


        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        // На исполнении - Исполнен
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.EXECUTED);
        toStatusAction.setInitialStatus(status);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.EXECUTED);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        if (author != null) {
            List<String> sendTo = new ArrayList<>();
            if ((author.getEmail() != null) && (!author.getEmail().isEmpty())) {
                sendTo.add(author.getEmail());
            }
            if (sendTo.size() > 0) {
                SendMailActivity mailActivity = new SendMailActivity();
                String docNumber = (String) PropertyUtils.getProperty(t, "taskNumber");
                String statusName = toStatusAction.getDestinationStatus().getStatus().getName();
                String subject = "Ваше поручение " + docNumber + " " + statusName;
                StringBuffer body = new StringBuffer();
                if (formDescription.equals("resolution")) {
                    subject = "Ваша резолюция " + docNumber + " " + statusName;
                    body = body.append("Резолюция перешла в статус \"").append(statusName).append("\"\n\n");
                } else {
                    body = body.append("Поручение перешло в статус ").append(statusName).append("\n\n");
                }

                MailMessage message = new MailMessage(sendTo, null, subject, body.append("<a href=\"").append(getHost()).append("/component/task/task.xhtml?docId=").
                        append(id).append("\" >Ссылка на документ</a>").toString());
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                activites.add(mailActivity);
            }
        }
        if (activites.size() > 0) {
            toStatus.setPreStatusActivities(activites);
        }

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);
        statuses.put(toStatus.getStatus(), toStatus);

        if (formDescription.equals("exercise") || formDescription.equals("task")) {
            //На рассмотрении - Отказ
            status = statuses.get(DocumentStatus.ON_EXECUTION_2);
            fromStatusActions = statuses.get(DocumentStatus.ON_EXECUTION_2).getAvailableActions();//new ArrayList<StatusChangeAction>();
            toStatusAction = new StatusChangeAction(process);
            toStatusAction.setAction(DocumentAction.CANCEL_25);
            toStatusAction.setInitialStatus(status);

            toStatus = new Status<>();
            toStatus.setStatus(DocumentStatus.CANCEL_4);
            toStatus.setProcessedData(t);
            toStatusAction.setDestinationStatus(toStatus);

            activites = new ArrayList<>();
            ParametrizedPropertyLocalActivity localActivity = new ParametrizedPropertyLocalActivity();
            localActivity.setParentAction(toStatusAction);
            InputReasonForm reasonForm = new InputReasonForm();
            reasonForm.setBeanName("task");
            reasonForm.setActionCommentaryField("WFResultDescription");
            reasonForm.setScope(EditablePropertyScope.LOCAL);
            localActivity.setDocument(reasonForm);
            activites.add(localActivity);
            toStatusAction.setLocalActivities(activites);

            activites = new ArrayList<>();
            if (author != null) {
                List<String> sendTo = new ArrayList<>();
                if ((author.getEmail() != null) && (!author.getEmail().isEmpty())) {
                    sendTo.add(author.getEmail());
                }
                if (sendTo.size() > 0) {
                    SendMailActivity mailActivity = new SendMailActivity();
                    String docNumber = (String) PropertyUtils.getProperty(t, "taskNumber");
                    String statusName = toStatusAction.getDestinationStatus().getStatus().getName();
                    String subject = "Ваше поручение " + docNumber + " " + statusName;
                    StringBuffer body = new StringBuffer();
                    if (formDescription.equals("resolution")) {
                        subject = "Ваша резолюция " + docNumber + " " + statusName;
                        body = body.append("Резолюция перешла в статус \"").append(statusName).append("\"\n\n");
                    } else {
                        body = body.append("Поручение перешло в статус ").append(statusName).append("\n\n");
                    }

                    MailMessage message = new MailMessage(sendTo, null, subject, body.append("<a href=\"").append(getHost()).append("/component/task/task.xhtml?docId=").
                            append(id).append("\" >Ссылка на документ</a>").toString());
                    message.setContentType("text/html");
                    mailActivity.setMessage(message);
                    activites.add(mailActivity);
                }
            }
            if (activites.size() > 0) {
                toStatus.setPreStatusActivities(activites);
            }

            fromStatusActions.add(toStatusAction);
            status.setAvailableActions(fromStatusActions);

            statuses.put(status.getStatus(), status);
            statuses.put(toStatus.getStatus(), toStatus);
        }

        if (formDescription.equals("task") && t.getDocumentStatus().equals(DocumentStatus.ON_EXECUTION_2)) {

            List<NoStatusAction> noStatusActions = new ArrayList<>();
            NoStatusAction delegateAction = new NoStatusAction(process);

            delegateAction.setAction(DocumentAction.DELEGATE_1001);

            activites = new ArrayList<>();
            ParametrizedPropertyLocalActivity localActivity = new ParametrizedPropertyLocalActivity();
            localActivity.setParentAction(delegateAction);
            SelectUserForm selectUserForm = new SelectUserForm();
            selectUserForm.setBeanName(process.getProcessedData().getBeanName());
            selectUserForm.setUserListBeanName("userList");
            selectUserForm.setSelectedUserField(EngineHelper.PROP_DELEGATION_USER);
            selectUserForm.setScope(EditablePropertyScope.LOCAL);
            localActivity.setDocument(selectUserForm);
            activites.add(localActivity);
            delegateAction.setLocalActivities(activites);

            activites = new ArrayList<>();
            activity = new InvokeMethodActivity();
            list = new ArrayList<>();
            list.add(delegateAction);
            list.add(t);
            activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "doTaskDelegateAction", list);
            activites.add(activity);

            if (executors != null) {
                List<String> sendTo = new ArrayList<>();
                for (User executor : executors) {
                    if (StringUtils.isNotEmpty(executor.getEmail())) {
                        sendTo.add(executor.getEmail());
                    }
                }

                if (sendTo.size() > 0) {
                    SendMailActivity mailActivity = new SendMailActivity();
                    String subject = "Поступило новое поручение @DocumentNumber";
                    StringBuffer body = new StringBuffer();
                    if (formDescription.equals("resolution")) {
                        subject = "Поступила новая резолюция @DocumentNumber";
                        body = body.append("К вам поступила новая резолюция на исполнение \n\n");
                    }  else {
                        body = body.append("К вам поступило новое поручение на исполнение \n\n");
                    }

                    MailMessage message = new MailMessage(sendTo, null, subject, body.append("<a href=\"").append(getHost()).append("/component/task/task.xhtml?docId=").
                            append(id).append("\" >Ссылка на документ</a>").toString());
                    message.setContentType("text/html");
                    mailActivity.setMessage(message);
                    activites.add(mailActivity);
                }

                if (activites.size() > 0) {
                    delegateAction.setPreActionActivities(activites);
                }
                noStatusActions.add(delegateAction);
            }

            if (formDescription.equals("task")) {

                NoStatusAction changeDateAction = new NoStatusAction(process);
                changeDateAction.setAction(DocumentAction.CHANGE_EXECUTION_DATE);

                activites = new ArrayList<>();
                ParametrizedPropertyLocalActivity localActivity2 = new ParametrizedPropertyLocalActivity();
                localActivity2.setParentAction(changeDateAction);
                InputDateForm dateForm = new InputDateForm();
                dateForm.setBeanName(process.getProcessedData().getBeanName());
                dateForm.setActionDateField("controlDate");
                dateForm.setActionDate(executionDate);
                dateForm.setScope(EditablePropertyScope.GLOBAL);
                dateForm.setActionCommentaryField("WFResultDescription");
                dateForm.setActionCommentary("Срок исполнения изменен");
                dateForm.setFormTitle("Укажите срок исполнения");
                dateForm.setDateTitle("Срок исполнения");
                localActivity2.setDocument(dateForm);
                activites.add(localActivity2);
                changeDateAction.setLocalActivities(activites);
                noStatusActions.add(changeDateAction);

                process.setNoStatusActions(noStatusActions);
            }

            process.setNoStatusActions(noStatusActions);
        }

        System.out.println("new process initialization");

        return statuses.get(t.getDocumentStatus());
    }

    private static <T extends ProcessedData> Status<T> getCurrentStatusOutDocument(T t, final Process process) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Map<DocumentStatus, Status<T>> statuses = new HashMap<>();
        // Проект документа - На исполнении
        Status<T> status = new Status<>();
        status.setStatus(DocumentStatus.DOC_PROJECT_1);
        status.setProcessedData(t);
        List<StatusChangeAction> fromStatusActions = new ArrayList<>();
        StatusChangeAction toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                        if ((RoleType.OFFICE_MANAGER.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        toStatusAction.setAction(DocumentAction.CHECK_IN_80);
        toStatusAction.setInitialStatus(status);

        List<IActivity> activites = new ArrayList<>();
        InvokeMethodActivity activity = new InvokeMethodActivity();
        List<Serializable> list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOutgoingRegistrationNumber", list);
        activites.add(activity);

        toStatusAction.setPreActionActivities(activites);

        Status<T> toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.CHECK_IN_80);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);
        statuses.put(toStatus.getStatus(), toStatus);

        // Проект документа - На рассмотрении
        status = statuses.get(DocumentStatus.DOC_PROJECT_1);
        fromStatusActions = statuses.get(DocumentStatus.DOC_PROJECT_1).getAvailableActions();
        toStatusAction = new StatusChangeAction(process);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        toStatusAction.setAction(DocumentAction.REDIRECT_TO_CONSIDERATION_2);
        toStatusAction.setInitialStatus(status);
        toStatusAction.setPreActionActivities(activites);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.ON_CONSIDERATION);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);
        statuses.put(toStatus.getStatus(), toStatus);

        // Проект документа - Согласование
        status = statuses.get(DocumentStatus.DOC_PROJECT_1);
        fromStatusActions = statuses.get(DocumentStatus.DOC_PROJECT_1).getAvailableActions();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.REDIRECT_TO_AGREEMENT);
        toStatusAction.setInitialStatus(status);

        Status<T> agreeStatus = new Status<>();
        agreeStatus.setStatus(DocumentStatus.AGREEMENT_3);
        agreeStatus.setProcessedData(t);
        agreeStatus.setAgreementEnabled(true);
        activites = new ArrayList<>();
        Object agreementTree = PropertyUtils.getProperty(t, "agreementTree");
        if (agreementTree != null) {
            HumanTaskTree tree = (HumanTaskTree) agreementTree;
            InvokeMethodActivity addAgreementUsersActivity = null;
            Set<User> executors = EngineHelper.doGenerateAgreementPrimaryExecutors(tree);
            if (executors.size() > 0) {
                addAgreementUsersActivity = new InvokeMethodActivity();
                list = new ArrayList<>();
                list.add(t);
                list.add(new ArrayList(executors));
                addAgreementUsersActivity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "addToDocumentAgreementUsers", list);
                activites.add(addAgreementUsersActivity);
            }

            List<String> sendTo = new ArrayList<>();
            for (User executor : executors) {
                if ((executor.getEmail() != null) && (!executor.getEmail().isEmpty())) {
                    sendTo.add(executor.getEmail());
                }
            }
            MailMessage message = new MailMessage(sendTo, null, "Новый запрос на согласование",
                    "Новый запрос на согласование\n\n" + "<a href=\"" + getHost() + "/component/out/out_document.xhtml?docId=" +
                            process.getProcessedData().getId() + "\" >Ссылка на документ</a>");
            //message.setBlindCopyTo(blindCopyTo);
            message.setContentType("text/html");
            SendMailActivity mailActivity = new SendMailActivity();
            mailActivity.setMessage(message);
            if (sendTo.size() > 0) activites.add(mailActivity);
            agreeStatus.setPreStatusActivities(activites);
        }
        toStatusAction.setDestinationStatus(agreeStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);
        statuses.put(agreeStatus.getStatus(), agreeStatus);

        //На рассмотрении - Зарегистрирован
        status = statuses.get(DocumentStatus.ON_CONSIDERATION);
        fromStatusActions = statuses.get(DocumentStatus.ON_CONSIDERATION).getAvailableActions();
        toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                        if ((RoleType.OFFICE_MANAGER.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        toStatusAction.setAction(DocumentAction.CHECK_IN_81);
        toStatusAction.setCommentNecessary(true);
        toStatusAction.setInitialStatus(status);

        toStatus = statuses.get(DocumentStatus.CHECK_IN_80);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOutgoingRegistrationNumber", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        activites = new ArrayList<>();

        //1-mail
        Object prop = PropertyUtils.getProperty(t, "executor");
        User executor = (prop == null ? null : (User) prop);

        prop = PropertyUtils.getProperty(t, "id");
        String id = prop == null ? null : prop.toString();

        if (executor != null) {
            List<String> sendTo = new ArrayList<>();
            if ((executor.getEmail() != null) && (!executor.getEmail().isEmpty())) {
                sendTo.add(executor.getEmail());
            }
            if (sendTo.size() > 0) {
                SendMailActivity mailActivity = new SendMailActivity();
                String subject = "Вы назнaчены ответственным по документу @DocumentNumber";
                String body = "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" +
                        "<a href=\"" + getHost() + "/component/out/out_document.xhtml?docId=" +
                        id + "\" >Ссылка на документ</a>";
                MailMessage message = new MailMessage(sendTo, null, subject, body);
                message.setContentType("text/html");
                mailActivity.setMessage(message);
                activites.add(mailActivity);
            }
        }

        if (activites.size() > 0) {
            toStatus.setPreStatusActivities(activites);
        }

        statuses.put(toStatus.getStatus(), toStatus);

        // На согласовании - Зарегистрирован
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };
        toStatus = statuses.get(DocumentStatus.CHECK_IN_80);

        toStatusAction.setAction(DocumentAction.CHECK_IN_82);
        toStatusAction.setInitialStatus(agreeStatus);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setOutgoingRegistrationNumber", list);
        activites.add(activity);

        toStatusAction.setPreActionActivities(activites);

        fromStatusActions.add(toStatusAction);
        agreeStatus.setAvailableActions(fromStatusActions);

        statuses.put(agreeStatus.getStatus(), agreeStatus);

        //Зарегистрирован - Исполнен
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.EXECUTE_90);
        toStatusAction.setCommentNecessary(true);
        toStatusAction.setInitialStatus(status);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.EXECUTE);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        //Исполнен - В архив
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.IN_ARCHIVE_99);
        toStatusAction.setInitialStatus(status);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkOutgoingPropertiesForArchiving", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.IN_ARCHIVE_100);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);
        statuses.put(toStatus.getStatus(), toStatus);

        //Изменить уровень допуска
        List<NoStatusAction> noStatusActions = new ArrayList<>();
        ParametrizedPropertyLocalActivity localActivity2 = new ParametrizedPropertyLocalActivity();
        NoStatusAction changeAccessLevelAction = new NoStatusAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    if (getProcess().getProcessedData().getDocumentStatus().equals(DocumentStatus.NEW)) return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        };
        changeAccessLevelAction.setAction(DocumentAction.CHANGE_ACCESS_LEVEL);

        activites = new ArrayList<>();

        localActivity2 = new ParametrizedPropertyLocalActivity();
        localActivity2.setParentAction(changeAccessLevelAction);
        SelectAccessLevelForm accessLevelForm = new SelectAccessLevelForm();
        accessLevelForm.setBeanName(process.getProcessedData().getBeanName());
        accessLevelForm.setSelectedAccessLevelField("userAccessLevel");
        accessLevelForm.setScope(EditablePropertyScope.GLOBAL);
        localActivity2.setDocument(accessLevelForm);
        activites.add(localActivity2);

        changeAccessLevelAction.setLocalActivities(activites);

        noStatusActions.add(changeAccessLevelAction);

        process.setNoStatusActions(noStatusActions);
        System.out.println("new process initialization");

        return statuses.get(t.getDocumentStatus());
    }

    private static <T extends ProcessedData> Status<T> getCurrentStatusInDocument(T t, final Process process, Object prop)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        prop = PropertyUtils.getProperty(t, "executionDate");
        Date executionDate = (prop == null ? null : (Date) prop);
        Map<DocumentStatus, Status<T>> statuses = new HashMap<>();
        prop = PropertyUtils.getProperty(t, "id");
        String id = prop == null ? null : prop.toString();

        // На регистрации - Зарегистрирован
        Status<T> status = new Status<>();
        status.setStatus(DocumentStatus.ON_REGISTRATION);
        status.setProcessedData(t);
        List<StatusChangeAction> fromStatusActions = new ArrayList<>();
        StatusChangeAction toStatusAction = new StatusChangeAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    ProcessUser user = getProcess().getProcessUser();
                    for (Role role : user.getRoles()) {
                        if ((RoleType.ADMINISTRATOR.equals(role.getRoleType()))) {
                            return true;
                        }
                        if ((RoleType.OFFICE_MANAGER.equals(role.getRoleType()))) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };

        toStatusAction.setAction(DocumentAction.CHECK_IN_1);
        toStatusAction.setInitialStatus(status);

        List<IActivity> activites = new ArrayList<>();
        InvokeMethodActivity activity = new InvokeMethodActivity();
        List<Serializable> list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "setIncomingRegistrationNumber", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        Status<T> toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.CHECK_IN_2);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        activites = new ArrayList<>();
        SendMailActivity mailActivity = null;
        prop = PropertyUtils.getProperty(t, "registrationNumber");
        String docNumber = (prop == null ? "" : (String) prop);

        //1-mail
        List<String> sendTo = new ArrayList<>();
        prop = PropertyUtils.getProperty(t, "executors");
        Set<User> executors = (prop == null ? null : (Set<User>) prop);
        if (executors != null) {
            for (User user : executors) {
                if ((user.getEmail() != null) && (!user.getEmail().isEmpty())) {
                    sendTo.add(user.getEmail());
                }
            }
            if (sendTo.size() > 0) {
                mailActivity = new SendMailActivity();
                MailMessage message1 = new MailMessage(sendTo, null, "Вы назнaчены ответственным по документу @DocumentNumber" + docNumber,
                        "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" + "<a href=\"" + getHost() + "/component/in/in_document.xhtml?docId=" +
                                id + "\" >Ссылка на документ</a>");
                message1.setContentType("text/html");
                mailActivity.setMessage(message1);
            }
        }
        if (mailActivity != null) {
            activites.add(mailActivity);
            mailActivity = null;
        }

        //2-mail
        Set<String> recipients = getRecipients(t);

        if (recipients.size() > 0) {
            MailMessage message2 = new MailMessage(new ArrayList(recipients), null, "Вам адресован документ @DocumentNumber" + docNumber,
                    "Документ перешел в статус \"" + toStatusAction.getDestinationStatus().getStatus().getName() + "\"\n\n" + "<a href=\"" + getHost() + "/component/in/in_document.xhtml?docId=" +
                            id + "\" >Ссылка на документ</a>");
            activites.add(getMailActivity(message2));
            toStatus.setPreStatusActivities(activites);
        }


        if (activites.size() > 0) {
            toStatus.setPreStatusActivities(activites);
        }

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        //На рассмотрении - На исполнении
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.REDIRECT_TO_EXECUTION_2);
        toStatusAction.setCommentNecessary(true);
        toStatusAction.setInitialStatus(status);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.ON_EXECUTION_80);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);


        //На исполнении - Исполнен
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.EXECUTE_80);
        toStatusAction.setCommentNecessary(true);
        toStatusAction.setInitialStatus(status);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.EXECUTE);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);

        //Исполнен - Архив
        status = toStatus;
        fromStatusActions = new ArrayList<>();
        toStatusAction = new StatusChangeAction(process);
        toStatusAction.setAction(DocumentAction.IN_ARCHIVE_90);
        toStatusAction.setInitialStatus(status);

        activites = new ArrayList<>();
        activity = new InvokeMethodActivity();
        list = new ArrayList<>();
        list.add(t);
        activity.setInvokeInformation("ru.efive.dms.util.WorkflowHelper", "checkIncomingPropertiesForArchiving", list);
        activites.add(activity);
        toStatusAction.setPreActionActivities(activites);

        toStatus = new Status<>();
        toStatus.setStatus(DocumentStatus.IN_ARCHIVE_100);
        toStatus.setProcessedData(t);
        toStatusAction.setDestinationStatus(toStatus);

        fromStatusActions.add(toStatusAction);
        status.setAvailableActions(fromStatusActions);

        statuses.put(status.getStatus(), status);
        statuses.put(toStatus.getStatus(), toStatus);
        List<NoStatusAction> noStatusActions = new ArrayList<>();

        NoStatusAction changeDateAction = new NoStatusAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    if (getProcess().getProcessedData().getDocumentStatus().getId() >= 2 &&
                            getProcess().getProcessedData().getDocumentStatus().getId() < 100)
                        return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        };
        changeDateAction.setAction(DocumentAction.CHANGE_EXECUTION_DATE);

        activites = new ArrayList<>();
        ParametrizedPropertyLocalActivity localActivity2 = new ParametrizedPropertyLocalActivity();
        localActivity2.setParentAction(changeDateAction);
        InputDateForm dateForm = new InputDateForm();
        dateForm.setBeanName(process.getProcessedData().getBeanName());
        dateForm.setActionDateField("executionDate");
        dateForm.setActionDate(executionDate);
        dateForm.setScope(EditablePropertyScope.GLOBAL);
        dateForm.setActionCommentaryField("WFResultDescription");
        dateForm.setActionCommentary("Срок исполнения изменен");
        dateForm.setFormTitle("Укажите срок исполнения документа");
        dateForm.setDateTitle("Срок исполнения");
        localActivity2.setDocument(dateForm);
        activites.add(localActivity2);
        changeDateAction.setLocalActivities(activites);

        noStatusActions.add(changeDateAction);

        //Изменить уровень допуска
        NoStatusAction changeAccessLevelAction = new NoStatusAction(process) {
            @Override
            public boolean isAvailable() {
                try {
                    if (getProcess().getProcessedData().getDocumentStatus().equals(DocumentStatus.NEW))
                        return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        };
        changeAccessLevelAction.setAction(DocumentAction.CHANGE_ACCESS_LEVEL);

        activites = new ArrayList<>();

        localActivity2 = new ParametrizedPropertyLocalActivity();
        localActivity2.setParentAction(changeAccessLevelAction);
        SelectAccessLevelForm accessLevelForm = new SelectAccessLevelForm();
        accessLevelForm.setBeanName(process.getProcessedData().getBeanName());
        accessLevelForm.setSelectedAccessLevelField("userAccessLevel");
        accessLevelForm.setScope(EditablePropertyScope.GLOBAL);
        localActivity2.setDocument(accessLevelForm);
        activites.add(localActivity2);

        changeAccessLevelAction.setLocalActivities(activites);

        noStatusActions.add(changeAccessLevelAction);

        process.setNoStatusActions(noStatusActions);

        System.out.println("new process initialization");

        return statuses.get(t.getDocumentStatus());
    }



}