package ru.efive.dms.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.DictionaryManagementBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.NoStatusAction;
import ru.efive.wf.core.activity.SendMailActivity;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.MailMessage;
import ru.efive.wf.core.util.EngineHelper;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentAction;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.RoleType;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.entity.model.wf.HumanTask;
import ru.entity.model.wf.HumanTaskTree;
import ru.entity.model.wf.HumanTaskTreeNode;
import ru.external.AgreementIssue;
import ru.util.ApplicationHelper;

import javax.faces.context.FacesContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.efive.dms.util.ApplicationDAONames.*;

public final class WorkflowHelper {

    private static final Logger taskLogger = LoggerFactory.getLogger("TASK");

    private static final int LEFT_PAD_COUNT = 5;
    private static final char LEFT_PAD_CHAR = '0';

    public static boolean changeTaskExecutionDateAction(NoStatusAction changeDateAction, Task task) {
        boolean result = false;
        try {
            Date currentDate = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();

            Date choosenDate = null;
            for (EditableProperty property : changeDateAction.getProperties()) {
                if (property.getName().equals("executionDate")) {
                    choosenDate = (Date) property.getValue();
                }
            }
            if (choosenDate != null) {
                /*List<IActivity> activities = changeDateAction.getPreActionActivities();
                    for (IActivity activity: activities) {
                        if (activity instanceof SendMailActivity) {
                            SendMailActivity sendMailActivity = (SendMailActivity) activity;
                            MailMessage message = sendMailActivity.getMessage();
                            List<String> sendTo = new ArrayList<String>();
                            sendTo.add(selectedUser.getEmail());
                            message.setSendTo(sendTo);
                            sendMailActivity.setMessage(message);
                        }
                    }*/
                final String delegateReason = "Делегирован %s " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(currentDate);
                task.setWFResultDescription(String.format(delegateReason, task.getExecutors().get(0).getDescription()));
                task.setExecutionDate(choosenDate);
                result = true;
            } else {
                System.out.println("No date fo change execution date");
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public static boolean doTaskDelegateAction(NoStatusAction delegateAction, Task task) {
        boolean result = false;
        try {
            Date currentDate = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
            //List<HumanTask> currentTaskList = delegateAction.getResolver().getCurrentTaskList();
            User selectedUser = null;
            for (EditableProperty property : delegateAction.getProperties()) {
                if (property.getName().equals(EngineHelper.PROP_DELEGATION_USER)) {
                    selectedUser = (User) property.getValue();
                }
            }
            if (selectedUser != null) {
                List<IActivity> activities = delegateAction.getPreActionActivities();
                for (IActivity activity : activities) {
                    if (activity instanceof SendMailActivity) {
                        SendMailActivity sendMailActivity = (SendMailActivity) activity;
                        MailMessage message = sendMailActivity.getMessage();
                        List<String> sendTo = new ArrayList<String>();
                        sendTo.add(selectedUser.getEmail());
                        message.setSendTo(sendTo);
                        sendMailActivity.setMessage(message);
                    }
                }
                final String delegateReason = "Делегирован %s " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(currentDate);
                task.setWFResultDescription(String.format(delegateReason, task.getExecutors().get(0).getDescription()));
                final HashSet<User> users = new HashSet<User>(1);
                users.add(selectedUser);
                task.setExecutors(users);

                result = true;
            } else {
                System.out.println("Not found selected user for delegate action");
            }
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public static boolean addToDocumentAgreementUsers(OutgoingDocument document, ArrayList<User> usersList) {
        boolean result = true;

        if (usersList.size() > 0) {
            Set<User> currentUsers = document.getAgreementUsers();
            if (currentUsers == null) {
                document.setAgreementUsers(new HashSet(usersList));
            } else {
                currentUsers.addAll(new HashSet(usersList));
                document.setAgreementUsers(currentUsers);
            }
        }

        return result;
    }

    public static boolean addToDocumentAgreementUsers(InternalDocument document, ArrayList<User> usersList) {
        boolean result = true;

        if (usersList.size() > 0) {
            Set<User> currentUsers = document.getAgreementUsers();
            if (currentUsers == null) {
                document.setAgreementUsers(new HashSet(usersList));
            } else {
                currentUsers.addAll(new HashSet(usersList));
                document.setAgreementUsers(currentUsers);
            }
        }

        return result;
    }

    public static boolean setOutgoingRegistrationNumber(OutgoingDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getController() == null) {
            in_result.append("Необходимо выбрать Руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getExecutor() == null) {
            in_result.append("Необходимо выбрать Ответственного исполнителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getRecipientContragents() == null || doc.getRecipientContragents().size() == 0) {
            in_result.append("Необходимо выбрать Адресата;").append(System.getProperty("line.separator"));
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);

                if (doc.getRegistrationNumber() == null || doc.getRegistrationNumber().isEmpty()) {
                    final StringBuilder in_number = new StringBuilder();
                    Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUser(doc.getController());
                    List<Role> in_roles = new ArrayList<Role>();
                    Role in_office;
                    if (in_nomenclature != null) {
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                        in_number.append(in_nomenclature.getCode()).append("-");
                    } else {
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                        in_number.append("01-");
                    }
                    if ((in_office != null) && (!in_roles.contains(in_office))) {
                        in_roles.add(in_office);
                    }
                    doc.setRoleEditors(in_roles);
                    final String in_count = StringUtils.leftPad(String.valueOf(new HashSet<OutgoingDocument>(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findRegistratedDocumentsByCriteria(in_number.toString())).size() + 1), LEFT_PAD_COUNT, LEFT_PAD_CHAR);
                    in_number.append(in_count);

                    doc.setRegistrationNumber(in_number.toString());

                    List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(doc.getUniqueId());
                    if (paperCopies.size() > 0) {
                        for (PaperCopyDocument paperCopy : paperCopies) {
                            String copyNumber = paperCopy.getRegistrationNumber();
                            if (copyNumber.contains("...")) {
                                copyNumber = copyNumber.replaceFirst("...", doc.getRegistrationNumber());
                                paperCopy.setRegistrationNumber(copyNumber);
                            } else if (copyNumber.isEmpty()) {
                                copyNumber = doc.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                paperCopy.setRegistrationNumber(copyNumber);
                            }
                            if (paperCopy.getDocumentStatus().getId() < 2) {
                                paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                            }
                            sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                        }
                    }

                    Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                    doc.setRegistrationDate(calendar.getTime());
                    result = true;
                }

            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setPaperCopyRegistrationNumber(PaperCopyDocument doc) {
        if (StringUtils.isEmpty(doc.getRegistrationNumber())) {
            doc.setWFResultDescription("Необходимо указать номер оригинала;" + System.getProperty("line.separator"));
            return false;
        }
        doc.setWFResultDescription("");
        return true;
    }

    public static boolean checkOutgoingPropertiesForArchiving(OutgoingDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(doc.getUniqueId());
        if (paperCopies.size() > 0) {
            for (PaperCopyDocument paperCopy : paperCopies) {
                String copyNumber = paperCopy.getRegistrationNumber();
                if (paperCopy.getDocumentStatus().getId() < 99) {
                    in_result.append("Бумажный экземпляр документа № " + copyNumber + " необходимо перевести в один из архивных статусов;");
                }
            }
        }

        if (in_result.toString().equals("")) {
            try {
                result = true;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setIncomingDocumentParentVolumeUnitsCount(IncomingDocument document) {
        boolean result = false;
        String in_result = "";
        FacesContext context = FacesContext.getCurrentInstance();
        if (in_result.toString().equals("")) {
            try {
                result = true;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                document.setWFResultDescription("");
            }
        } else {
            document.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setPaperCopyParentVolumeUnitsCount(PaperCopyDocument document) {
        FacesContext context = FacesContext.getCurrentInstance();
        SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        OfficeKeepingVolume parentVolume = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO).findDocumentById(String.valueOf(document.getOfficeKeepingVolume().getId()));
        if (parentVolume != null) {
            int units = parentVolume.getUnitsCount() + document.getSheetsCount();
            parentVolume.setUnitsCount(units);
            sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO).save(parentVolume);
            document.setWFResultDescription("");
            return true;
        } else {
            return false;
        }
    }

    public static boolean setIncomingRegistrationNumber(IncomingDocument doc) {
        boolean result = false;
        final FacesContext context = FacesContext.getCurrentInstance();
        final StringBuilder in_result = new StringBuilder("");
        if (doc.getController() == null) {
            in_result.append("Необходимо выбрать Руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getContragent() == null) {
            in_result.append("Необходимо выбрать Корреспондента;").append(System.getProperty("line.separator"));
        }
        if ((doc.getRecipientUsers() == null || doc.getRecipientUsers().size() == 0) &&
                (doc.getRecipientGroups() == null || doc.getRecipientGroups().size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;").append(System.getProperty("line.separator"));
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;").append(System.getProperty("line.separator"));
        }

        if (in_result.length() == 0) {
            try {

                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);
                if (StringUtils.isEmpty(doc.getRegistrationNumber())) {
                    final StringBuilder in_number = new StringBuilder();
                    Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUser(doc.getController());
                    List<Role> in_roles = new ArrayList<Role>();
                    Role in_office;
                    if (in_nomenclature != null) {
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                        in_number.append(in_nomenclature.getCode()).append("-");
                    } else {
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                        in_number.append("01-");
                    }
                    if ((in_office != null) && (!in_roles.contains(in_office))) {
                        in_roles.add(in_office);
                    }
                    doc.setRoleEditors(in_roles);

                    in_number.append(StringUtils.leftPad(String.valueOf(new HashSet<IncomingDocument>(sessionManagement.
                            getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findRegistratedDocumentsByCriteria(in_number.toString())).size() + 1), LEFT_PAD_COUNT, LEFT_PAD_CHAR));
                    doc.setRegistrationNumber(in_number.toString());

                    List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(doc.getUniqueId());
                    if (paperCopies.size() > 0) {
                        for (PaperCopyDocument paperCopy : paperCopies) {
                            String copyNumber = paperCopy.getRegistrationNumber();
                            if (copyNumber.contains("...")) {
                                copyNumber = copyNumber.replaceFirst("...", doc.getRegistrationNumber());
                                paperCopy.setRegistrationNumber(copyNumber);
                            } else if (copyNumber.isEmpty()) {
                                copyNumber = doc.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                paperCopy.setRegistrationNumber(copyNumber);
                            }
                            if (paperCopy.getDocumentStatus().getId() < 2) {
                                paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                            }
                            sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                        }
                    }

                    Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                    doc.setRegistrationDate(calendar.getTime());
                    result = true;
                }
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkIncomingPropertiesForArchiving(IncomingDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(doc.getUniqueId());
        if (paperCopies.size() > 0) {
            for (PaperCopyDocument paperCopy : paperCopies) {
                String copyNumber = paperCopy.getRegistrationNumber();
                if (paperCopy.getDocumentStatus().getId() < 99) {
                    in_result.append("Бумажный экземпляр документа № ").append(copyNumber).append(" необходимо перевести в один из архивных статусов;");
                }
            }
        }

        if (in_result.toString().equals("")) {
            try {
                result = true;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkPaperCopyPropertiesForArchiving(PaperCopyDocument doc) {
        boolean result = false;
        StringBuilder in_result = new StringBuilder("");
        if (doc.getOfficeKeepingVolume() == null) {
            in_result.append("Необходимо указать нужный том дела;").append(System.getProperty("line.separator"));
        } else {
            OfficeKeepingVolume parentVolume = doc.getOfficeKeepingVolume();
            int limitUnitsCount = parentVolume.getLimitUnitsCount();
            int currentUnitsCount = parentVolume.getUnitsCount();
            if (limitUnitsCount < (currentUnitsCount + doc.getAppendixiesCount() * doc.getSheetsCount() * doc.getCopiesCount())) {
                in_result.append("Количество листов в выбраном томе дела превышает предельно допустимое.").append(System.getProperty("line.separator"));
            }
        }

        if (doc.getSheetsCount() == 0) {
            in_result.append("Необходимо указать количество листов;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                result = true;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setInternalRegistrationNumber(InternalDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getSigner() == null) {
            in_result.append("Необходимо выбрать Руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getResponsible() == null) {
            in_result.append("Необходимо выбрать Контроль исполнения;").append(System.getProperty("line.separator"));
        }
        if ((doc.getRecipientUsers() == null || doc.getRecipientUsers().size() == 0) &&
                (doc.getRecipientGroups() == null || doc.getRecipientGroups().size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;").append(System.getProperty("line.separator"));
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);

                if (StringUtils.isEmpty(doc.getRegistrationNumber())) {
                    StringBuffer in_number = new StringBuffer();
                    Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUser(doc.getSigner());
                    List<Role> in_roles = new ArrayList<Role>();
                    Role in_office;
                    if (in_nomenclature != null) {
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                        in_number.append(in_nomenclature.getCode()).append("-");
                    } else {
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                        in_number.append("01-");
                    }
                    if ((in_office != null) && (!in_roles.contains(in_office))) {
                        in_roles.add(in_office);
                    }
                    doc.setRoleEditors(in_roles);

                            /*- Распоряжение (формат номера – индекс руководителя/номер по порядку)
                                   - Методическое пособие (формат номера – номер по порядку/область для которой написан документ/год. Над справочник областей ФНКЦ еще думают)
                                   - Инструкция (формат номера - номер по порядку/область для которой написан документ/год. Над справочник областей ФНКЦ еще думают)
                                   - Служебная записка (формат номера - номер по порядку/год)
                                   - Информационное письмо (формат номера - номер по порядку/год)
                                   - Приказ (номер по порядку)*/

                    String in_form = doc.getForm().getValue();


                    Map<String, Object> in_filters = new HashMap<String, Object>();
                    String in_count;
                    SimpleDateFormat ydf = new SimpleDateFormat("yyyy");
                    if (in_form.equals("Распоряжение")) {
                        //Р/индекс/номер по порядку
                        in_number = new StringBuffer();
                        if (in_nomenclature != null) {
                            in_number.append("Р/").append(in_nomenclature.getCode()).append("/");
                        } else {
                            in_number.append("Р/01/");
                        }

                        in_filters.put("registrationNumber", in_number);
                        in_filters.put("form", doc.getForm());
                        in_count = StringUtils.leftPad(String.valueOf(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size() + 1), LEFT_PAD_COUNT, LEFT_PAD_CHAR);
                        in_number.append(in_count);

                    } else if (in_form.equals("Методическое пособие")) {
                        in_filters.put("registrationNumber", "%");
                        in_filters.put("form", doc.getForm());
                        in_count = StringUtils.leftPad(String.valueOf(new HashSet<InternalDocument>(
                                                sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size()
                                                + 1
                                ),
                                LEFT_PAD_COUNT,
                                LEFT_PAD_CHAR
                        );
                        in_number.append(in_count).append("/Методические пособия/").append(ydf.format(Calendar.getInstance().getTime()));


                    } else if (in_form.equals("Инструкция")) {
                        in_filters.put("registrationNumber", in_number);
                        in_filters.put("form", doc.getForm());
                        in_count = StringUtils.leftPad(
                                String.valueOf(new HashSet<InternalDocument>(
                                                sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size()
                                                + 1
                                ),
                                LEFT_PAD_COUNT,
                                LEFT_PAD_CHAR
                        );
                        in_number.append(in_count).append("/Инструкции/").append(ydf.format(Calendar.getInstance().getTime()));


                    } else if (in_form.equals("Служебная записка")) {
                        in_number = new StringBuffer();
                        if (in_nomenclature != null) {
                            in_number.append("СЗ/" + in_nomenclature.getCode() + "/");
                        } else {
                            in_number.append("СЗ/01/");
                        }
                        in_filters.put("registrationNumber", in_number);
                        in_filters.put("form", doc.getForm());
                        in_count = StringUtils.leftPad(
                                String.valueOf(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size() + 1),
                                LEFT_PAD_COUNT,
                                LEFT_PAD_CHAR
                        );
                        in_number.append(in_count);

                                /*in_number=new StringBuffer();
                                        in_filters.put("registrationNumber", "%");
                                        in_filters.put("form", document.getForm());
                                        in_count=new  StringBuffer("0000"+String.valueOf(sessionManagement.getDAO(InternalDocumentDAOImpl.class,INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters,true,false).size()+1));
                                        in_number.append("СЗ/"+in_count.substring(in_count.length()-4)+"/"+ydf.format(java.util.Calendar.getInstance ().getTime()));*/

                    } else if (in_form.equals("Информационное письмо")) {
                        in_number = new StringBuffer();
                        in_filters.put("registrationNumber", "%");
                        in_filters.put("form", doc.getForm());
                        in_count = StringUtils.leftPad(String.valueOf(new HashSet<InternalDocument>(
                                                sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size()
                                                //+sessionManagement.getDAO(InternalDocumentDAOImpl.class,INTERNAL_DOCUMENT_FORM_DAO).findRegistratedDocumentsByForm("Служебная записка").size()
                                                + 1
                                ),
                                LEFT_PAD_COUNT,
                                LEFT_PAD_CHAR
                        );
                        in_number.append(in_count).append("/").append(ydf.format(Calendar.getInstance().getTime()));

                    } else if (in_form.equals("Гарантийное письмо")) {
                        in_number = new StringBuffer();
                        in_filters.put("registrationNumber", "%");
                        in_filters.put("form", doc.getForm());
                        in_count = StringUtils.leftPad(String.valueOf(new HashSet<InternalDocument>(
                                                sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size()
                                                //+sessionManagement.getDAO(InternalDocumentDAOImpl.class,INTERNAL_DOCUMENT_FORM_DAO).findRegistratedDocumentsByForm("Служебная записка").size()
                                                + 1
                                ),
                                LEFT_PAD_COUNT,
                                LEFT_PAD_CHAR
                        );
                        in_number.append("ГП/").append(in_count).append("/").append(ydf.format(Calendar.getInstance().getTime()));

                    } else if (in_form.equals("Приказ")) {
                        in_number = new StringBuffer();

                        Map<String, Object> outDateOrder_filters = new HashMap<String, Object>();
                        outDateOrder_filters.put("registrationNumber", "%/%");
                        outDateOrder_filters.put("form", doc.getForm());
                        outDateOrder_filters.put("closePeriodRegistrationFlag", "false");
                        int outDateOrderCount = new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(outDateOrder_filters, true, false)).size();

                        in_filters.put("registrationNumber", "%");
                        in_filters.put("form", doc.getForm());
                        in_filters.put("closePeriodRegistrationFlag", "false");
                        int summaryOrderCount = new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size();

                        in_count = StringUtils.leftPad(String.valueOf(summaryOrderCount - outDateOrderCount + 1), LEFT_PAD_COUNT, LEFT_PAD_CHAR);
                        in_number.append(in_count);
                    } else if (in_form.equals("Правила внутреннего распорядка")) {
                        in_number = new StringBuffer();
                        in_filters.put("registrationNumber", "%");
                        in_filters.put("form", doc.getForm());
                        in_count = StringUtils.leftPad(
                                String.valueOf(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size() + 1),
                                LEFT_PAD_COUNT,
                                LEFT_PAD_CHAR
                        );
                        in_number.append("ПВР/").append(in_count).append("/").append(ydf.format(Calendar.getInstance().getTime()));
                    } else if (in_form.equals("Положение")) {
                        in_number = new StringBuffer();
                        in_filters.put("registrationNumber", "%");
                        in_filters.put("form", doc.getForm());
                        in_count = StringUtils.leftPad(String.valueOf(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size() + 1),
                                LEFT_PAD_COUNT,
                                LEFT_PAD_CHAR
                        );
                        in_number.append("Положение/").append(in_count).append("/").append(ydf.format(Calendar.getInstance().getTime()));
                    } else {
                        result = false;
                        doc.setWFResultDescription("Данный вид документа не может быть зарегистрирован!");
                        return result;
                    }
                    doc.setRegistrationNumber(in_number.toString());

                    List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(doc.getUniqueId());
                    if (paperCopies.size() > 0) {
                        for (PaperCopyDocument paperCopy : paperCopies) {
                            String copyNumber = paperCopy.getRegistrationNumber();
                            if (copyNumber.contains("...")) {
                                copyNumber = copyNumber.replaceFirst("...", doc.getRegistrationNumber());
                                paperCopy.setRegistrationNumber(copyNumber);
                            } else if (copyNumber.isEmpty()) {
                                copyNumber = doc.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                paperCopy.setRegistrationNumber(copyNumber);
                            }
                            if (paperCopy.getDocumentStatus().getId() < 2) {
                                paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                            }
                            sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                        }
                    }

                    Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                    doc.setRegistrationDate(calendar.getTime());
                    result = true;
                }

            } catch (Exception e) {
                result = false;
                doc.setWFResultDescription(e.toString());
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setInternalRegistrationNumberOnOutDate(InternalDocument doc) {
        boolean result = false;
        doc.setClosePeriodRegistrationFlag(true);
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getSigner() == null) {
            in_result.append("Необходимо выбрать Руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getResponsible() == null) {
            in_result.append("Необходимо выбрать Ответственного;").append(System.getProperty("line.separator"));
        }
        if ((doc.getRecipientUsers() == null || doc.getRecipientUsers().size() == 0) &&
                (doc.getRecipientGroups() == null || doc.getRecipientGroups().size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;").append(System.getProperty("line.separator"));
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;").append(System.getProperty("line.separator"));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            if (!sdf.parse(sdf.format(doc.getRegistrationDate())).before(sdf.parse(sdf.format(new Date())))) {
                in_result.append("Дата регистрации должна быть указана до текущей даты;").append(System.getProperty("line.separator"));
                doc.setRegistrationNumber(null);
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
            in_result.append(e1.toString()).append(";").append(System.getProperty("line.separator"));
        }


        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        Map<String, Object> in_filters = new HashMap<String, Object>();
        in_filters.put("registrationNumber", doc.getRegistrationNumber());
        List<InternalDocument> copyDocuments = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, false, true);
        if (copyDocuments.size() != 0) {
            in_result.append("Документ под таким номером уже существует;").append(System.getProperty("line.separator"));
            doc.setRegistrationNumber(null);
        }

        if (in_result.toString().equals("")) {
            try {
                DictionaryManagementBean dictionaryManager = context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);
                Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUser(doc.getSigner());
                List<Role> in_roles = new ArrayList<Role>();
                Role in_office;
                if (in_nomenclature != null) {
                    in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                } else {
                    in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                }
                if ((in_office != null) && (!in_roles.contains(in_office))) {
                    in_roles.add(in_office);
                }
                doc.setRoleEditors(in_roles);

                List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(doc.getUniqueId());
                if (paperCopies.size() > 0) {
                    for (PaperCopyDocument paperCopy : paperCopies) {
                        String copyNumber = paperCopy.getRegistrationNumber();
                        if (copyNumber.contains("...")) {
                            copyNumber = copyNumber.replaceFirst("...", doc.getRegistrationNumber());
                            paperCopy.setRegistrationNumber(copyNumber);
                        } else if (copyNumber.isEmpty()) {
                            copyNumber = doc.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                            paperCopy.setRegistrationNumber(copyNumber);
                        }
                        if (paperCopy.getDocumentStatus().getId() < 2) {
                            paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                        }
                        sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                    }
                }
                result = true;

            } catch (Exception e) {
                result = false;
                doc.setWFResultDescription(e.toString());
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkInternalPropertiesForArchiving(InternalDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(doc.getUniqueId());
        if (paperCopies.size() > 0) {
            for (PaperCopyDocument paperCopy : paperCopies) {
                String copyNumber = paperCopy.getRegistrationNumber();
                if (paperCopy.getDocumentStatus().getId() < 99) {
                    in_result.append("Бумажный экземпляр документа № ").append(copyNumber).append(" необходимо перевести в один из архивных статусов;");
                }
            }
        }

        if (in_result.toString().equals("")) {
            try {
                result = true;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkRequestPropertiesForArchiving(RequestDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(doc.getUniqueId());
        if (paperCopies.size() > 0) {
            for (PaperCopyDocument paperCopy : paperCopies) {
                String copyNumber = paperCopy.getRegistrationNumber();
                if (paperCopy.getDocumentStatus().getId() < 99) {
                    in_result.append("Бумажный экземпляр документа № ").append(copyNumber).append(" необходимо перевести в один из архивных статусов;");
                }
            }
        }

        if (in_result.toString().equals("")) {
            try {
                result = true;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkInternalDocumentPropertiesForReview(InternalDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");
        if (doc.getSigner() == null) {
            in_result.append("Необходимо указать руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getResponsible() == null) {
            in_result.append("Необходимо указать ответственного;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {

                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);

                if (StringUtils.isEmpty(doc.getRegistrationNumber())) {
                    Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUser(doc.getSigner());
                    Role in_administrationRole = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.ADMINISTRATOR);
                    List<Role> in_roles = new ArrayList<Role>();
                    in_roles.add(in_administrationRole);

                    Role in_office;
                    if (in_nomenclature != null) {
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                    } else {
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                    }
                    if ((in_office != null) && (!in_roles.contains(in_office))) {
                        in_roles.add(in_office);
                    }
                    doc.setRoleEditors(in_roles);
                    System.out.println("->true");
                    result = true;
                }

            } catch (Exception e) {
                result = false;
                doc.setWFResultDescription(e.toString());
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setRequestRegistrationNumber(RequestDocument document) {
        boolean result = false;
        final FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if ((document.getSenderType() == null)) {
            in_result.append("Необходимо указать Тип отправителя документа; \n");
        }

        if ((document.getSenderType().getValue().equals("Физическое лицо")) && (document.getSenderLastName() == null)) {
            in_result.append("Необходимо указать Фамилию адресанта; \n");
        }
        if ((document.getSenderType().getValue().equals("Юридическое лицо")) && (document.getContragent() == null)) {
            in_result.append("Необходимо указать Корреспондента; \n");
        }
        if ((document.getRecipientUsers() == null || document.getRecipientUsers().size() == 0) &&
                (document.getRecipientGroups() == null || document.getRecipientGroups().size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;").append(System.getProperty("line.separator"));
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание; \n");
        }

        if (in_result.toString().equals("")) {
            try {
                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);


                if (document.getRegistrationNumber() == null || document.getRegistrationNumber().isEmpty()) {
                    final StringBuilder in_number = new StringBuilder();
                    String in_count = StringUtils.leftPad(String.valueOf(new HashSet<RequestDocument>(sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findRegistratedDocuments()).size() + 1), LEFT_PAD_COUNT, LEFT_PAD_CHAR);
                    in_number.append(in_count);
                    document.setRegistrationNumber(in_number.toString());

                    List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
                    if (paperCopies.size() > 0) {
                        for (PaperCopyDocument paperCopy : paperCopies) {
                            String copyNumber = paperCopy.getRegistrationNumber();
                            if (copyNumber.contains("...")) {
                                copyNumber = copyNumber.replaceFirst("...", document.getRegistrationNumber());
                                paperCopy.setRegistrationNumber(copyNumber);
                            } else if (copyNumber.isEmpty()) {
                                copyNumber = document.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                paperCopy.setRegistrationNumber(copyNumber);
                            }
                            if (paperCopy.getDocumentStatus().getId() < 2) {
                                paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                            }
                            sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                        }
                    }

                    Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                    document.setRegistrationDate(calendar.getTime());
                    List<Role> in_roles = new ArrayList<Role>();
                    Role in_office;
                    in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoleByType(RoleType.REQUEST_MANAGER);
                    in_roles.add(in_office);

                    document.setRoleEditors(in_roles);

                    result = true;
                } else {
                    result = true;
                }
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                document.setWFResultDescription("");
            }
        } else {
            document.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setTaskRegistrationNumber(Task doc) {
        System.out.println("start task registration");
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getExecutors() == null || doc.getExecutors().isEmpty()) {
            in_result.append("Необходимо выбрать Исполнителя;").append(System.getProperty("line.separator"));
        } else {
            for (User user : doc.getExecutors()) {
                if (StringUtils.isEmpty(user.getEmail())) {
                    in_result.append("У исполнителя по поручению отсутствует адрес электронной почты;").append(System.getProperty("line.separator"));
                }
            }
        }
        if (StringUtils.isEmpty(doc.getShortDescription())) {
            in_result.append("Необходимо заполнить Текст поручения;").append(System.getProperty("line.separator"));
        }
        //TODO OMG проверяем валидность по комментарию )
        if (in_result.toString().isEmpty()) {
            try {
                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                if (StringUtils.isEmpty(doc.getTaskNumber())) {
                    //Номер не задан
                    StringBuilder in_number = new StringBuilder();
                    StringBuilder in_count = new StringBuilder();
                    Map<String, Object> in_filters = new HashMap<String, Object>();

                    final String key = doc.getRootDocumentId();
                    if (StringUtils.isNotEmpty(key)) {
                        in_filters.put("rootDocumentId", key);
                        if (key.contains("_")) {
                            //TODO лень переписывать поиск документов по строковому идентификатору, когда он является целочисленным = )
                            final Integer idInt = ApplicationHelper.getIdFromUniqueIdString(key);
                            if (idInt != null) {
                                final String id = idInt.toString();
                                if (key.contains("incoming")) {
                                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class,
                                            INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                                    in_number.append(in_doc.getRegistrationNumber()).append("/");
                                } else if (key.contains("outgoing")) {
                                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                                    in_number.append(out_doc.getRegistrationNumber()).append("/");
                                } else if (key.contains("internal")) {
                                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                                    in_number.append(internal_doc.getRegistrationNumber()).append("/");
                                } else if (key.contains("request")) {
                                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                                    in_number.append(request_doc.getRegistrationNumber()).append("/");
                                } else if (key.contains("task")) {
                                    Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findDocumentById(id);
                                    in_number.append("");
                                    in_filters.clear();
                                    in_filters.put("taskDocumentId", "");
                                }
                            }
                        }
                    } else {
                        in_number.append("");
                        in_filters.clear();
                        in_filters.put("taskDocumentId", "");
                    }

                    in_count.append(String.valueOf(new HashSet<Task>(sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findAllDocuments(in_filters, false, false)).size() + 1));
                    in_number.append(in_count);
                    doc.setTaskNumber(in_number.toString());

                    Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                    doc.setRegistrationDate(calendar.getTime());
                    result = true;
                } else {
                    //Номер задан - > Нихера не делаем?!
                    result = true;
                }
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }

            if (result) {
                doc.setWFResultDescription("");
            }
        } else

        {
            doc.setWFResultDescription(in_result.toString());
        }

        return result;
    }

    public static boolean cloneTasks(Task doc) {
        try {
            final FacesContext context = FacesContext.getCurrentInstance();
            final SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);

            //TODO когда-нибудь, когда мир станет вновь светлым и ясным, когда прекратятся войны, исчезнет коррупция,
            //TODO ну или что более вероятно когда перепишут эту фабрику действий - то этот жуткий костыль превратится в красивое и элегантное решение
            //TODO когда-нибудь... но только не сегодня. @27-10-2014
            if (doc.getExecutors().size() > 1) {
                taskLogger.debug("Group task start clone");
                //Групповое поручение
                final Task templateTask = (Task) doc.clone();
                templateTask.setId(0);
                templateTask.getExecutors().clear();
                templateTask.setParent(doc);
                templateTask.getHistory().clear();

                final Map<String, Object> in_filters = new HashMap<String, Object>();
                final Matcher matcher = Pattern.compile("(.*)([0-9]+)$").matcher(doc.getTaskNumber());
                if (matcher.find()) {
                    templateTask.setTaskNumber(matcher.group(1));
                    in_filters.put("rootDocumentId", doc.getRootDocumentId());
                } else {
                    in_filters.put("taskDocumentId", "");
                }

                for (User currentExecutor : doc.getExecutors()) {
                    final Task currentTask = (Task) templateTask.clone();
                    final Set<User> executorsSet = new HashSet<User>(1);
                    executorsSet.add(currentExecutor);
                    currentTask.setExecutors(executorsSet);
                    //+2 потому что жизнь-боль и первое поручение еще не сохранено в БД с корректным номером
                    int numberOffset = (new HashSet<Task>(sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findAllDocuments(in_filters, false, false)).size() + 2);
                    currentTask.setTaskNumber(currentTask.getTaskNumber().concat(String.valueOf(numberOffset)));
                    Set<HistoryEntry> history = new HashSet<HistoryEntry>(1);
                    final HistoryEntry entry = new HistoryEntry();
                    entry.setActionId(DocumentAction.REDIRECT_TO_EXECUTION_1.getId());
                    entry.setCreated(new Date());
                    entry.setCommentary("Создано из группового поручения");
                    entry.setFromStatusId(1);
                    entry.setToStatusId(DocumentStatus.ON_EXECUTION_2.getId());
                    currentTask.setHistory(history);
                    currentTask.setDocumentStatus(DocumentStatus.ON_EXECUTION_2);
                    sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(currentTask);
                    taskLogger.debug("Sub-task[{}] {}", currentTask.getId(), currentTask.getTaskNumber());
                    if (taskLogger.isTraceEnabled()) {
                        taskLogger.trace("Sub-task Info: {}", currentTask);
                    }
                }
                return true;
            } else {
                taskLogger.debug("NOT need to clone task");
                return true;
            }
        } catch (Exception e) {
            taskLogger.error("Error while cloning tasks:", e);
            return false;
        }
    }

    public static boolean formAgreementTree(AgreementIssue doc, HumanTaskTree template) {
        try {
            SessionManagementBean sessionManagement = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(
                    FacesContext.getCurrentInstance(), "#{sessionManagement}", SessionManagementBean.class);
            Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
            User author = sessionManagement.getLoggedUser();

            HumanTaskTree tree = new HumanTaskTree();
            tree.setAuthor(author);
            tree.setCreated(created);
            tree.setDeleted(false);

            List<HumanTaskTreeNode> newRootNodes = new ArrayList<HumanTaskTreeNode>();

            if (template.getRootNodes() != null) {
                for (HumanTaskTreeNode rootNodeTemplate : template.getRootNodeList()) {
                    HumanTaskTreeNode newRootNode = new HumanTaskTreeNode();
                    newRootNode.setAuthor(author);
                    newRootNode.setCreated(created);
                    newRootNode.setDeleted(false);
                    List<HumanTask> newTasks = new ArrayList<HumanTask>();
                    for (HumanTask taskTemplate : rootNodeTemplate.getTaskList()) {
                        HumanTask newTask = new HumanTask();
                        newTask.setAuthor(author);
                        newTask.setCreated(created);
                        newTask.setDeleted(false);
                        newTask.setExecutor(taskTemplate.getExecutor());
                        newTask.setCommentary(taskTemplate.getCommentary());
                        newTask.setStatusId(DocumentStatus.NEW.getId());
                        newTasks.add(newTask);
                    }
                    newRootNode.setTasks(newTasks);
                    newRootNode.setChildNodes(getChildNodeListFromTemplate(rootNodeTemplate));
                    newRootNodes.add(newRootNode);
                }
            }
            tree.setRootNodes(newRootNodes);

            doc.setAgreementTree(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static List<HumanTaskTreeNode> getChildNodeListFromTemplate(HumanTaskTreeNode rootNodeTemplate) throws Exception {
        List<HumanTaskTreeNode> result = new ArrayList<HumanTaskTreeNode>();
        try {
            SessionManagementBean sessionManagement = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(
                    FacesContext.getCurrentInstance(), "#{sessionManagement}", SessionManagementBean.class);
            Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
            User author = sessionManagement.getLoggedUser();

            if (rootNodeTemplate.getChildNodes() != null) {
                for (HumanTaskTreeNode childNodeTemplate : rootNodeTemplate.getChildNodeList()) {
                    HumanTaskTreeNode newChildNode = new HumanTaskTreeNode();
                    newChildNode.setAuthor(author);
                    newChildNode.setCreated(created);
                    newChildNode.setDeleted(false);
                    List<HumanTask> newTasks = new ArrayList<HumanTask>();
                    for (HumanTask taskTemplate : childNodeTemplate.getTaskList()) {
                        HumanTask newTask = new HumanTask();
                        newTask.setAuthor(author);
                        newTask.setCreated(created);
                        newTask.setDeleted(false);
                        newTask.setExecutor(taskTemplate.getExecutor());
                        newTask.setCommentary(taskTemplate.getCommentary());
                        newTask.setStatusId(DocumentStatus.NEW.getId());
                        newTasks.add(newTask);
                    }
                    newChildNode.setTasks(newTasks);
                    newChildNode.setChildNodes(getChildNodeListFromTemplate(childNodeTemplate));

                    result.add(newChildNode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean setOfficeKeepingFileRegistrationNumber(OfficeKeepingFile doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getFileIndex() == null || doc.getFileIndex().equals("")) {
            in_result.append("Необходимо заполнить поле Индекс").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                long checkCount = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, OFFICE_KEEPING_FILE_DAO).countDocumentsByNumber(doc.getFileIndex());

                if (checkCount > 0) {
                    result = false;
                    in_result.append("Номенклатура дел с таким индексом существует. Укажите другой индекс.");
                } else {
                    result = true;
                }
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }

            if (result) {
                doc.setWFResultDescription("");
            } else {
                doc.setWFResultDescription(in_result.toString());
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setOfficeKeepingVolumeRegistrationNumber(OfficeKeepingVolume doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getParentFile() == null) {
            in_result.append("Необходимо указать корректную номенклатуру дела;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                //FacesContext context=FacesContext.getCurrentInstance();
                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);

                if (doc.getVolumeIndex() == null || doc.getVolumeIndex().isEmpty()) {
                    StringBuffer in_number = new StringBuffer();
                    //StringBuffer in_count=new  StringBuffer("0000"+String.valueOf(sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class,OFFICE_KEEPING_FILE_DAO).findRegistratedDocuments().size()+1));
                    int in_count = 0;
                    OfficeKeepingFile parentFile = doc.getParentFile();
                    Set<OfficeKeepingVolume> in_volumes = parentFile.getVolumes();
                    if (in_volumes != null) {
                        for (OfficeKeepingVolume in_volume : in_volumes) {
                            if (in_volume.getDocumentStatus().getId() >= 2) {
                                in_count++;
                            }
                        }
                    }
                    in_count++;
                    in_number.append(parentFile.getFileIndex()).append("/").append(in_count);
                    doc.setVolumeIndex(in_number.toString());
                    //Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                    //document.setRegistrationDate(calendar.getTime());
                    //Role in_administrationRole=sessionManagement.getDAO(RoleDAOHibernate.class, "roleDao").findRoleByType(RoleType.ENTERPRISE_ADMINISTRATION);
                    //List<Role> in_roles=new ArrayList<Role>();
                    //if((in_administrationRole!=null)&&(!in_roles.contains(in_administrationRole))){
                    //in_roles.add(in_administrationRole);
                    //}

                    //Role in_office;
                    //in_office=sessionManagement.getDAO(RoleDAOHibernate.class, "roleDao").findRoleByType(RoleType.OFFICE_REQUESTS);
                    //in_roles.add(in_office);

                    //document.setRoleEditors(in_roles);
                    result = true;

                }
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkPaperCopyPropertiesForUnfile(PaperCopyDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getCollector() == null) {
            in_result.append("Необходимо указать кому будет выдан том дела;").append(System.getProperty("line.separator"));
        }
        if (doc.getReturnDate() == null) {
            in_result.append("Необходимо указать дату возврата тома дела;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                result = true;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkOfficeKeepingVolumePropertiesForUnfile(OfficeKeepingVolume doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getCollector() == null) {
            in_result.append("Необходимо указать кому будет выдан том дела;").append(System.getProperty("line.separator"));
        }
        if (doc.getReturnDate() == null) {
            in_result.append("Необходимо указать дату возврата тома дела;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                result = true;
            } catch (Exception e) {
                result = false;
                e.printStackTrace();
            }
            if (result) {
                doc.setWFResultDescription("");
            }
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return result;
    }


    public static boolean setOfficeKeepingVolumeCollectorToEmpty(OfficeKeepingVolume doc) {
        boolean result = false;

        doc.setCollector(null);
        doc.setReturnDate(null);
        try {
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        if (result) {
            doc.setWFResultDescription("");
        }
        return result;
    }

    public static boolean setPaperCopyCollectorToEmpty(PaperCopyDocument doc) {
        boolean result = false;

        doc.setCollector(null);
        doc.setReturnDate(null);
        try {
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        if (result) {
            doc.setWFResultDescription("");
        }
        return result;
    }
}