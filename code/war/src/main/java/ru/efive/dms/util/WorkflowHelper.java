package ru.efive.dms.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.dao.OfficeKeepingFileDAOImpl;
import ru.efive.dms.dao.OfficeKeepingVolumeDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.PaperCopyDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.data.Nomenclature;
import ru.efive.dms.data.OfficeKeepingFile;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.data.PaperCopyDocument;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.data.Task;
import ru.efive.dms.uifaces.beans.DictionaryManagementBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.wf.core.AgreementIssue;
import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.NoStatusAction;
import ru.efive.wf.core.activity.SendMailActivity;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.HumanTask;
import ru.efive.wf.core.data.HumanTaskTree;
import ru.efive.wf.core.data.HumanTaskTreeNode;
import ru.efive.wf.core.data.MailMessage;
import ru.efive.wf.core.util.EngineHelper;

public final class WorkflowHelper {

    public static boolean changeTaskExecutionDateAction(NoStatusAction changeDateAction, Task task) {
        boolean result = false;
        try {
            Date currentDate = Calendar.getInstance(new Locale("ru", "RU")).getTime();

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
                String delegateReasin = "Делегирован " + task.getExecutor().getDescriptionShort() + " " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(currentDate);
                task.setWFResultDescription(delegateReasin);
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
            Date currentDate = Calendar.getInstance(new Locale("ru", "RU")).getTime();
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
                String delegateReasin = "Делегирован " + task.getExecutor().getDescriptionShort() + " " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(currentDate);
                task.setWFResultDescription(delegateReasin);
                task.setExecutor(selectedUser);

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
        OutgoingDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getSigner() == null) {
            in_result.append("Необходимо выбрать Руководителя;" + System.getProperty("line.separator"));
        }
        if (document.getExecutor() == null) {
            in_result.append("Необходимо выбрать Ответственного исполнителя;" + System.getProperty("line.separator"));
        }
        if (document.getRecipientContragents() == null || document.getRecipientContragents().size() == 0) {
            in_result.append("Необходимо выбрать Адресата;" + System.getProperty("line.separator"));
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;" + System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = (DictionaryManagementBean) context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);
                if (document == null) {
                    result = false;
                } else {
                    if (document != null) {
                        if (document.getRegistrationNumber() == null || document.getRegistrationNumber().isEmpty()) {
                            StringBuffer in_number = new StringBuffer();
                            Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUserUNID(document.getSigner().getUNID());
                            //Role in_administrationRole=sessionManagement.getDAO(RoleDAOHibernate.class, "roleDao").findRoleByType(RoleType.ENTERPRISE_ADMINISTRATION);
                            List<Role> in_roles = new ArrayList<Role>();
                            //in_roles.add(in_administrationRole);

                            Role in_office;
                            if (in_nomenclature != null) {
                                in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCategory()));
                                in_number.append(in_nomenclature.getCategory() + "-");
                            } else {
                                in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                                in_number.append("01-");
                            }
                            if ((in_office != null) && (!in_roles.contains(in_office))) {
                                in_roles.add(in_office);
                            }
                            document.setRoleEditors(in_roles);

                            StringBuffer in_count = new StringBuffer("0000" + String.valueOf(new HashSet<OutgoingDocument>(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findRegistratedDocumentsByCriteria(in_number.toString())).size() + 1));
                            in_number.append(in_count.substring(in_count.length() - 4));

                            document.setRegistrationNumber(in_number.toString());

                            List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
                            if (paperCopies.size() > 0) {
                                for (PaperCopyDocument paperCopy : paperCopies) {
                                    String copyNumber = paperCopy.getRegistrationNumber();
                                    if (copyNumber.indexOf("...") > -1) {
                                        copyNumber = copyNumber.replaceFirst("...", document.getRegistrationNumber());
                                        paperCopy.setRegistrationNumber(copyNumber);
                                    } else if (copyNumber.isEmpty()) {
                                        copyNumber = document.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                        paperCopy.setRegistrationNumber(copyNumber);
                                    }
                                    if (paperCopy.getDocumentStatus().getId() < 2) {
                                        paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                                    }
                                    sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                                }
                            }

                            Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                            document.setRegistrationDate(calendar.getTime());
                            result = true;
                        }
                    }
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

    public static boolean setPaperCopyRegistrationNumber(PaperCopyDocument doc) {
        boolean result = false;
        PaperCopyDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getRegistrationNumber() == null || document.getRegistrationNumber().equals("")) {
            in_result.append("Необходимо указать номер оригинала;" + System.getProperty("line.separator"));
        }

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

    public static boolean checkOutgoingPropertiesForArchiving(OutgoingDocument doc) {
        boolean result = false;
        OutgoingDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
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
                document.setWFResultDescription("");
            }
        } else {
            document.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setIncomingDocumentParentVolumeUnitsCount(IncomingDocument document) {
        boolean result = false;
        String in_result = "";
        FacesContext context = FacesContext.getCurrentInstance();
        OfficeKeepingVolume parentVolume = document.getOfficeKeepingVolume();
        if (parentVolume != null) {
            parentVolume.setUnitsCount(parentVolume.getUnitsCount() +
                    document.getAppendixiesCount()
                            * document.getSheetsCount()
                            * document.getCopiesCount());
            SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
            sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).save(parentVolume);
            result = true;
        }
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
        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        boolean result = false;
        String in_result = "";
        OfficeKeepingVolume parentVolume = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).findDocumentById(String.valueOf(document.getOfficeKeepingVolume().getId()));
        if (parentVolume != null) {
            int units = parentVolume.getUnitsCount() + document.getSheetsCount();
            parentVolume.setUnitsCount(units);
            sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).save(parentVolume);
            result = true;
        }
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

    public static boolean setIncomingRegistrationNumber(IncomingDocument doc) {
        boolean result = false;
        IncomingDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");
        if (document.getController() == null) {
            in_result.append("Необходимо выбрать Руководителя;" + System.getProperty("line.separator"));
        }
        if (document.getContragent() == null) {
            in_result.append("Необходимо выбрать Корреспондента;" + System.getProperty("line.separator"));
        }
        if ((document.getRecipientUsers() == null || document.getRecipientUsers().size() == 0) &&
                (document.getRecipientGroups() == null || document.getRecipientGroups().size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;" + System.getProperty("line.separator"));
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;" + System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {

                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = (DictionaryManagementBean) context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);
                if (document == null) {
                    result = false;
                } else {
                    if (document != null) {
                        if (document.getRegistrationNumber() == null || document.getRegistrationNumber().isEmpty()) {
                            StringBuffer in_number = new StringBuffer();
                            Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUserUNID(document.getController().getUNID());
                            //Role in_administrationRole=sessionManagement.getDAO(RoleDAOHibernate.class, "roleDao").findRoleByType(RoleType.ENTERPRISE_ADMINISTRATION);
                            List<Role> in_roles = new ArrayList<Role>();
                            //if((in_administrationRole!=null)&&()&&(!in_roles.contains(in_administrationRole))){
                            //in_roles.add(in_administrationRole);
                            //}

                            Role in_office;
                            if (in_nomenclature != null) {
                                in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCategory()));
                                in_number.append(in_nomenclature.getCategory() + "-");
                            } else {
                                in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                                in_number.append("01-");
                            }
                            if ((in_office != null) && (!in_roles.contains(in_office))) {
                                in_roles.add(in_office);
                            }
                            document.setRoleEditors(in_roles);

                            StringBuffer in_count = new StringBuffer("0000" + String.valueOf(new HashSet<IncomingDocument>(sessionManagement.
                                    getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findRegistratedDocumentsByCriteria(in_number.toString())).size() + 1));
                            in_number.append(in_count.substring(in_count.length() - 4));
                            document.setRegistrationNumber(in_number.toString());

                            List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
                            if (paperCopies.size() > 0) {
                                for (PaperCopyDocument paperCopy : paperCopies) {
                                    String copyNumber = paperCopy.getRegistrationNumber();
                                    if (copyNumber.indexOf("...") > -1) {
                                        copyNumber = copyNumber.replaceFirst("...", document.getRegistrationNumber());
                                        paperCopy.setRegistrationNumber(copyNumber);
                                    } else if (copyNumber.isEmpty()) {
                                        copyNumber = document.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                        paperCopy.setRegistrationNumber(copyNumber);
                                    }
                                    if (paperCopy.getDocumentStatus().getId() < 2) {
                                        paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                                    }
                                    sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                                }
                            }

                            Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                            document.setRegistrationDate(calendar.getTime());
                            result = true;
                        }
                    }
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

    public static boolean checkIncomingPropertiesForArchiving(IncomingDocument doc) {
        boolean result = false;
        IncomingDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
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
                document.setWFResultDescription("");
            }
        } else {
            document.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkPaperCopyPropertiesForArchiving(PaperCopyDocument doc) {
        boolean result = false;
        PaperCopyDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");
        if (document.getOfficeKeepingVolume() == null) {
            in_result.append("Необходимо указать нужный том дела;" + System.getProperty("line.separator"));
        } else {
            OfficeKeepingVolume parentVolume = document.getOfficeKeepingVolume();
            int limitUnitsCount = parentVolume.getLimitUnitsCount();
            int currentUnitsCount = parentVolume.getUnitsCount();
            if (limitUnitsCount < (currentUnitsCount + document.getAppendixiesCount() * document.getSheetsCount() * document.getCopiesCount())) {
                in_result.append("Количество листов в выбраном томе дела превышает предельно допустимое." + System.getProperty("line.separator"));
            }
        }

        if (document.getSheetsCount() == 0) {
            in_result.append("Необходимо указать количество листов;" + System.getProperty("line.separator"));
        }

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

    public static boolean setInternalRegistrationNumber(InternalDocument doc) {
        boolean result = false;
        InternalDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getSigner() == null) {
            in_result.append("Необходимо выбрать Руководителя;" + System.getProperty("line.separator"));
        }
        if (document.getResponsible() == null) {
            in_result.append("Необходимо выбрать Контроль исполнения;" + System.getProperty("line.separator"));
        }
        if ((document.getRecipientUsers() == null || document.getRecipientUsers().size() == 0) &&
                (document.getRecipientGroups() == null || document.getRecipientGroups().size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;" + System.getProperty("line.separator"));
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;" + System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                //FacesContext context=FacesContext.getCurrentInstance();
                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = (DictionaryManagementBean) context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);
                //OutgoingDocument document=doc;
                if (document == null) {
                    result = false;
                } else {
                    if (document != null) {
                        if (document.getRegistrationNumber() == null || document.getRegistrationNumber().isEmpty()) {
                            StringBuffer in_number = new StringBuffer();
                            Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUserUNID(document.getSigner().getUNID());
                            //Role in_administrationRole=sessionManagement.getDAO(RoleDAOHibernate.class, "roleDao").findRoleByType(RoleType.ENTERPRISE_ADMINISTRATION);
                            List<Role> in_roles = new ArrayList<Role>();
                            //if((in_administrationRole!=null)&&(!in_roles.contains(in_administrationRole))){
                            //in_roles.add(in_administrationRole);
                            //}

                            Role in_office;
                            if (in_nomenclature != null) {
                                in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCategory()));
                                in_number.append(in_nomenclature.getCategory() + "-");
                            } else {
                                in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                                in_number.append("01-");
                            }
                            if ((in_office != null) && (!in_roles.contains(in_office))) {
                                in_roles.add(in_office);
                            }
                            document.setRoleEditors(in_roles);

                            /*- Распоряжение (формат номера – индекс руководителя/номер по порядку)
                                   - Методическое пособие (формат номера – номер по порядку/область для которой написан документ/год. Над справочник областей ФНКЦ еще думают)
                                   - Инструкция (формат номера - номер по порядку/область для которой написан документ/год. Над справочник областей ФНКЦ еще думают)
                                   - Служебная записка (формат номера - номер по порядку/год)
                                   - Информационное письмо (формат номера - номер по порядку/год)
                                   - Приказ (номер по порядку)*/

                            String in_form = document.getForm().getValue();


                            Map<String, Object> in_filters = new HashMap<String, Object>();
                            StringBuffer in_count;
                            SimpleDateFormat ydf = new SimpleDateFormat("yyyy");
                            if (in_form.equals("Распоряжение")) {
                                //Р/индекс/номер по порядку
                                in_number = new StringBuffer();
                                if (in_nomenclature != null) {
                                    in_number.append("Р/" + in_nomenclature.getCategory() + "/");
                                } else {
                                    in_number.append("Р/01/");
                                }

                                in_filters.put("registrationNumber", in_number);
                                in_filters.put("form", document.getForm());
                                in_count = new StringBuffer("0000" + String.valueOf(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size() + 1));
                                in_number.append(in_count.substring(in_count.length() - 4));

                            } else if (in_form.equals("Методическое пособие")) {
                                in_filters.put("registrationNumber", "%");
                                in_filters.put("form", document.getForm());
                                in_count = new StringBuffer("0000" +
                                        String.valueOf(new HashSet<InternalDocument>(
                                                sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size()
                                                + 1
                                        )
                                );
                                in_number.append(in_count.substring(in_count.length() - 4) + "/Методические пособия/" + ydf.format(java.util.Calendar.getInstance().getTime()));


                            } else if (in_form.equals("Инструкция")) {
                                in_filters.put("registrationNumber", in_number);
                                in_filters.put("form", document.getForm());
                                in_count = new StringBuffer("0000" +
                                        String.valueOf(new HashSet<InternalDocument>(
                                                sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size()
                                                + 1
                                        )
                                );
                                in_number.append(in_count.substring(in_count.length() - 4) + "/Инструкции/" + ydf.format(java.util.Calendar.getInstance().getTime()));


                            } else if (in_form.equals("Служебная записка")) {
                                in_number = new StringBuffer();
                                if (in_nomenclature != null) {
                                    in_number.append("СЗ/" + in_nomenclature.getCategory() + "/");
                                } else {
                                    in_number.append("СЗ/01/");
                                }
                                in_filters.put("registrationNumber", in_number);
                                in_filters.put("form", document.getForm());
                                in_count = new StringBuffer("0000" + String.valueOf(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size() + 1));
                                in_number.append(in_count.substring(in_count.length() - 4));

                                /*in_number=new StringBuffer();
                                        in_filters.put("registrationNumber", "%");
                                        in_filters.put("form", document.getForm());
                                        in_count=new  StringBuffer("0000"+String.valueOf(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters,true,false).size()+1));
                                        in_number.append("СЗ/"+in_count.substring(in_count.length()-4)+"/"+ydf.format(java.util.Calendar.getInstance ().getTime()));*/

                            } else if (in_form.equals("Информационное письмо")) {
                                in_number = new StringBuffer();
                                in_filters.put("registrationNumber", "%");
                                in_filters.put("form", document.getForm());
                                in_count = new StringBuffer("0000" +
                                        String.valueOf(new HashSet<InternalDocument>(
                                                sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size()
                                                //+sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findRegistratedDocumentsByForm("Служебная записка").size()
                                                + 1
                                        )
                                );
                                in_number.append(in_count.substring(in_count.length() - 4) + "/" + ydf.format(java.util.Calendar.getInstance().getTime()));

                            } else if (in_form.equals("Гарантийное письмо")) {
                                in_number = new StringBuffer();
                                in_filters.put("registrationNumber", "%");
                                in_filters.put("form", document.getForm());
                                in_count = new StringBuffer("0000" +
                                        String.valueOf(new HashSet<InternalDocument>(
                                                sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size()
                                                //+sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findRegistratedDocumentsByForm("Служебная записка").size()
                                                + 1
                                        )
                                );
                                in_number.append("ГП/" + in_count.substring(in_count.length() - 4) + "/" + ydf.format(java.util.Calendar.getInstance().getTime()));

                            } else if (in_form.equals("Приказ")) {
                                in_number = new StringBuffer();

                                Map<String, Object> outDateOrder_filters = new HashMap<String, Object>();
                                outDateOrder_filters.put("registrationNumber", "%/%");
                                outDateOrder_filters.put("form", document.getForm());
                                outDateOrder_filters.put("closePeriodRegistrationFlag", "false");
                                int outDateOrderCount =  new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(outDateOrder_filters, true, false)).size();

                                in_filters.put("registrationNumber", "%");
                                in_filters.put("form", document.getForm());
                                in_filters.put("closePeriodRegistrationFlag", "false");
                                int summaryOrderCount = new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size();

                                in_count = new StringBuffer("0000" + String.valueOf(summaryOrderCount - outDateOrderCount + 1));
                                in_number.append(in_count.substring(in_count.length() - 4));
                            } else if (in_form.equals("Правила внутреннего распорядка")) {
                                in_number = new StringBuffer();
                                in_filters.put("registrationNumber", "%");
                                in_filters.put("form", document.getForm());
                                in_count = new StringBuffer("0000" + String.valueOf(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size() + 1));
                                in_number.append("ПВР/" + in_count.substring(in_count.length() - 4) + "/" + ydf.format(java.util.Calendar.getInstance().getTime()));
                            } else if (in_form.equals("Положение")) {
                                in_number = new StringBuffer();
                                in_filters.put("registrationNumber", "%");
                                in_filters.put("form", document.getForm());
                                in_count = new StringBuffer("0000" + String.valueOf(new HashSet<InternalDocument>(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, true, false)).size() + 1));
                                in_number.append("Положение/" + in_count.substring(in_count.length() - 4) + "/" + ydf.format(java.util.Calendar.getInstance().getTime()));
                            } else {
                                result = false;
                                document.setWFResultDescription("Данный вид документа не может быть зарегистрирован!");
                                return result;
                            }
                            document.setRegistrationNumber(in_number.toString());

                            List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
                            if (paperCopies.size() > 0) {
                                for (PaperCopyDocument paperCopy : paperCopies) {
                                    String copyNumber = paperCopy.getRegistrationNumber();
                                    if (copyNumber.indexOf("...") > -1) {
                                        copyNumber = copyNumber.replaceFirst("...", document.getRegistrationNumber());
                                        paperCopy.setRegistrationNumber(copyNumber);
                                    } else if (copyNumber.isEmpty()) {
                                        copyNumber = document.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                        paperCopy.setRegistrationNumber(copyNumber);
                                    }
                                    if (paperCopy.getDocumentStatus().getId() < 2) {
                                        paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                                    }
                                    sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                                }
                            }

                            Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                            document.setRegistrationDate(calendar.getTime());
                            result = true;
                        }
                    }
                }
            } catch (Exception e) {
                result = false;
                document.setWFResultDescription(e.toString());
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

    public static boolean setInternalRegistrationNumberOnOutDate(InternalDocument doc) {
        boolean result = false;
        InternalDocument document = doc;
        document.setClosePeriodRegistrationFlag(true);
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getSigner() == null) {
            in_result.append("Необходимо выбрать Руководителя;" + System.getProperty("line.separator"));
        }
        if (document.getResponsible() == null) {
            in_result.append("Необходимо выбрать Ответственного;" + System.getProperty("line.separator"));
        }
        if ((document.getRecipientUsers() == null || document.getRecipientUsers().size() == 0) &&
                (document.getRecipientGroups() == null || document.getRecipientGroups().size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;" + System.getProperty("line.separator"));
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;" + System.getProperty("line.separator"));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        try {
            if (!sdf.parse(sdf.format(document.getRegistrationDate())).before(sdf.parse(sdf.format(new Date())))) {
                in_result.append("Дата регистрации должна быть указана до текущей даты;" + System.getProperty("line.separator"));
                document.setRegistrationNumber(null);
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
            in_result.append(e1.toString() + ";" + System.getProperty("line.separator"));
        }


        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        Map<String, Object> in_filters = new HashMap<String, Object>();
        in_filters.put("registrationNumber", document.getRegistrationNumber());
        List<InternalDocument> copyDocuments = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters, false, true);
        if (copyDocuments.size() != 0) {
            in_result.append("Документ под таким номером уже существует;" + System.getProperty("line.separator"));
            document.setRegistrationNumber(null);
        }

        if (in_result.toString().equals("")) {
            try {
                DictionaryManagementBean dictionaryManager = (DictionaryManagementBean) context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);
                if (document == null) {
                    result = false;
                } else {
                    if (document != null) {
                        Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUserUNID(document.getSigner().getUNID());
                        List<Role> in_roles = new ArrayList<Role>();
                        Role in_office;
                        if (in_nomenclature != null) {
                            in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCategory()));
                        } else {
                            in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                        }
                        if ((in_office != null) && (!in_roles.contains(in_office))) {
                            in_roles.add(in_office);
                        }
                        document.setRoleEditors(in_roles);

                        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
                        if (paperCopies.size() > 0) {
                            for (PaperCopyDocument paperCopy : paperCopies) {
                                String copyNumber = paperCopy.getRegistrationNumber();
                                if (copyNumber.indexOf("...") > -1) {
                                    copyNumber = copyNumber.replaceFirst("...", document.getRegistrationNumber());
                                    paperCopy.setRegistrationNumber(copyNumber);
                                } else if (copyNumber.isEmpty()) {
                                    copyNumber = document.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                    paperCopy.setRegistrationNumber(copyNumber);
                                }
                                if (paperCopy.getDocumentStatus().getId() < 2) {
                                    paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                                }
                                sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                            }
                        }

                        Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                        //							document.setRegistrationDate(calendar.getTime());
                        result = true;
                    }
                }
            } catch (Exception e) {
                result = false;
                document.setWFResultDescription(e.toString());
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

    public static boolean checkInternalPropertiesForArchiving(InternalDocument doc) {
        boolean result = false;
        InternalDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
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
                document.setWFResultDescription("");
            }
        } else {
            document.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkRequestPropertiesForArchiving(RequestDocument doc) {
        boolean result = false;
        RequestDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
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
                document.setWFResultDescription("");
            }
        } else {
            document.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean checkInternalDocumentPropertiesForReview(InternalDocument doc) {
        boolean result = false;
        InternalDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");
        if (document.getSigner() == null) {
            in_result.append("Необходимо указать руководителя;" + System.getProperty("line.separator"));
        }
        if (document.getResponsible() == null) {
            in_result.append("Необходимо указать ответственного;" + System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {

                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = (DictionaryManagementBean) context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);
                if (document == null) {
                    result = false;
                } else {
                    if (document != null) {
                        if (document.getRegistrationNumber() == null || document.getRegistrationNumber().isEmpty()) {
                            Nomenclature in_nomenclature = dictionaryManager.getNomenclatureByUserUNID(document.getSigner().getUNID());
                            Role in_administrationRole = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.ADMINISTRATOR);
                            List<Role> in_roles = new ArrayList<Role>();
                            in_roles.add(in_administrationRole);

                            Role in_office;
                            if (in_nomenclature != null) {
                                in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCategory()));
                            } else {
                                in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.valueOf("OFFICE_01"));
                            }
                            if ((in_office != null) && (!in_roles.contains(in_office))) {
                                in_roles.add(in_office);
                            }
                            document.setRoleEditors(in_roles);
                            System.out.println("->true");
                            result = true;
                        }
                    }
                }
            } catch (Exception e) {
                result = false;
                document.setWFResultDescription(e.toString());
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

    public static boolean setRequestRegistrationNumber(RequestDocument document) {
        boolean result = false;
        //RequestDocument document=doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

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
            in_result.append("Необходимо выбрать Адресатов;" + System.getProperty("line.separator"));
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание; \n");
        }

        if (in_result.toString().equals("")) {
            try {
                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = (DictionaryManagementBean) context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);
                if (document == null) {
                    result = false;
                } else {
                    //if (document != null) {
                    if (document.getRegistrationNumber() == null || document.getRegistrationNumber().isEmpty()) {
                        StringBuffer in_number = new StringBuffer();
                        StringBuffer in_count = new StringBuffer("0000" + String.valueOf(new HashSet<RequestDocument>(sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findRegistratedDocuments()).size() + 1));
                        in_number.append(in_count.substring(in_count.length() - 5));
                        document.setRegistrationNumber(in_number.toString());

                        List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(document.getUniqueId());
                        if (paperCopies.size() > 0) {
                            for (PaperCopyDocument paperCopy : paperCopies) {
                                String copyNumber = paperCopy.getRegistrationNumber();
                                if (copyNumber.indexOf("...") > -1) {
                                    copyNumber = copyNumber.replaceFirst("...", document.getRegistrationNumber());
                                    paperCopy.setRegistrationNumber(copyNumber);
                                } else if (copyNumber.isEmpty()) {
                                    copyNumber = document.getRegistrationNumber() + "/" + (paperCopies.size() + 1);
                                    paperCopy.setRegistrationNumber(copyNumber);
                                }
                                if (paperCopy.getDocumentStatus().getId() < 2) {
                                    paperCopy.setDocumentStatus(DocumentStatus.CHECK_IN_2);
                                }
                                sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);
                            }
                        }

                        Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                        document.setRegistrationDate(calendar.getTime());
                        //Role in_administrationRole=sessionManagement.getDAO(RoleDAOHibernate.class, "roleDao").findRoleByType(RoleType.ENTERPRISE_ADMINISTRATION);
                        List<Role> in_roles = new ArrayList<Role>();
                        //if((in_administrationRole!=null)&&(!in_roles.contains(in_administrationRole))){
                        //in_roles.add(in_administrationRole);
                        //}

                        Role in_office;
                        in_office = sessionManagement.getDAO(RoleDAOHibernate.class, ApplicationHelper.ROLE_DAO).findRoleByType(RoleType.OFFICE_REQUESTS);
                        in_roles.add(in_office);

                        document.setRoleEditors(in_roles);

                        result = true;
                    } else {
                        result = true;
                    }
                }
            }
            //}
            catch (Exception e) {
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
        Task document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getExecutor() == null) {
            in_result.append("Необходимо выбрать Исполнителя;" + System.getProperty("line.separator"));
        } else {
            if (document.getExecutor().getEmail().isEmpty()) {
                in_result.append("У исполнителя по поручению отсутствует адрес электронной почты;" + System.getProperty("line.separator"));
            }
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Текст поручения;" + System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {

            try {
                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                DictionaryManagementBean dictionaryManager = (DictionaryManagementBean) context.getApplication().evaluateExpressionGet(context, "#{dictionaryManagement}", DictionaryManagementBean.class);

                if (document == null) {
                    result = false;
                } else {
                    if (document != null) {
                        if (document.getTaskNumber() == null || document.getTaskNumber().isEmpty()) {
                            StringBuffer in_number = new StringBuffer();
                            StringBuffer in_count = new StringBuffer();
                            Map<String, Object> in_filters = new HashMap<String, Object>();

                            String key = document.getRootDocumentId();
                            if (key != null && !key.isEmpty()) {
                                in_filters.put("rootDocumentId", key);
                                int pos = key.indexOf('_');
                                if (pos != -1) {
                                    String id = key.substring(pos + 1, key.length());
                                    StringBuffer in_description = new StringBuffer("");

                                    if (key.indexOf("incoming") != -1) {
                                        IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class,
                                                ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                                        in_number.append(in_doc.getRegistrationNumber() + "/");
                                    } else if (key.indexOf("outgoing") != -1) {
                                        OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                                        in_number.append(out_doc.getRegistrationNumber() + "/");
                                    } else if (key.indexOf("internal") != -1) {
                                        InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                                        in_number.append(internal_doc.getRegistrationNumber() + "/");
                                    } else if (key.indexOf("request") != -1) {
                                        RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                                        in_number.append(request_doc.getRegistrationNumber() + "/");
                                    } else if (key.indexOf("task") != -1) {
                                        Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findDocumentById(id);
                                        in_number.append("");
                                        in_filters.clear();
                                        in_filters.put("taskDocumentId", "");
                                    }
                                }
                            } else {
                                in_number.append("");
                                in_filters.clear();
                                in_filters.put("taskDocumentId", "");
                            }

                            in_count.append(String.valueOf(new HashSet<Task>(sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findAllDocuments(in_filters, false, false)).size() + 1));
                            in_number.append(in_count);
                            document.setTaskNumber(in_number.toString());

                            Calendar calendar = Calendar.getInstance(ApplicationHelper.getLocale());
                            document.setRegistrationDate(calendar.getTime());

                            result = true;
                        } else {
                            result = true;
                        }
                    }
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

    public static boolean formAgreementTree(AgreementIssue doc, HumanTaskTree template) {
        boolean result = false;
        try {
            SessionManagementBean sessionManagement = (SessionManagementBean) FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(
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
        return result;
    }

    private static List<HumanTaskTreeNode> getChildNodeListFromTemplate(HumanTaskTreeNode rootNodeTemplate) throws Exception {
        List<HumanTaskTreeNode> result = new ArrayList<HumanTaskTreeNode>();
        try {
            SessionManagementBean sessionManagement = (SessionManagementBean) FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(
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
        OfficeKeepingFile document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getFileIndex() == null || document.getFileIndex().equals("")) {
            in_result.append("Необходимо заполнить поле Индекс" + System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                //FacesContext context=FacesContext.getCurrentInstance();
                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                long checkCount = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).countDocumentsByNumber(document.getFileIndex());

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
                document.setWFResultDescription("");
            } else {
                document.setWFResultDescription(in_result.toString());
            }
        } else {
            document.setWFResultDescription(in_result.toString());
        }
        return result;
    }

    public static boolean setOfficeKeepingVolumeRegistrationNumber(OfficeKeepingVolume doc) {
        boolean result = false;
        OfficeKeepingVolume document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getParentFile() == null) {
            in_result.append("Необходимо указать корректную номенклатуру дела;" + System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                //FacesContext context=FacesContext.getCurrentInstance();
                SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
                if (document == null) {
                    result = false;
                } else {
                    if (document != null) {
                        if (document.getVolumeIndex() == null || document.getVolumeIndex().isEmpty()) {
                            StringBuffer in_number = new StringBuffer();
                            //StringBuffer in_count=new  StringBuffer("0000"+String.valueOf(sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_FILE_DAO).findRegistratedDocuments().size()+1));
                            int in_count = 0;
                            OfficeKeepingFile parentFile = document.getParentFile();
                            Set<OfficeKeepingVolume> in_volumes = parentFile.getVolumes();
                            if (in_volumes != null) {
                                for (OfficeKeepingVolume in_volume : in_volumes) {
                                    if (in_volume.getDocumentStatus().getId() >= 2) {
                                        in_count++;
                                    }
                                }
                            }
                            in_count++;
                            in_number.append(parentFile.getFileIndex() + "/" + in_count);
                            document.setVolumeIndex(in_number.toString());
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
                    }
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

    public static boolean checkPaperCopyPropertiesForUnfile(PaperCopyDocument doc) {
        boolean result = false;
        PaperCopyDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getCollector() == null) {
            in_result.append("Необходимо указать кому будет выдан том дела;" + System.getProperty("line.separator"));
        }
        if (document.getReturnDate() == null) {
            in_result.append("Необходимо указать дату возврата тома дела;" + System.getProperty("line.separator"));
        }

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

    public static boolean checkIncomingDocumentPropertiesForUnfile(IncomingDocument doc) {
        boolean result = false;
        IncomingDocument document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getCollector() == null) {
            in_result.append("Необходимо указать кому будет выдан том дела;" + System.getProperty("line.separator"));
        }
        if (document.getReturnDate() == null) {
            in_result.append("Необходимо указать дату возврата тома дела;" + System.getProperty("line.separator"));
        }

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

    public static boolean checkOfficeKeepingVolumePropertiesForUnfile(OfficeKeepingVolume doc) {
        boolean result = false;
        OfficeKeepingVolume document = doc;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuffer in_result = new StringBuffer("");

        if (document.getCollector() == null) {
            in_result.append("Необходимо указать кому будет выдан том дела;" + System.getProperty("line.separator"));
        }
        if (document.getReturnDate() == null) {
            in_result.append("Необходимо указать дату возврата тома дела;" + System.getProperty("line.separator"));
        }

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


    public static boolean setOfficeKeepingVolumeCollectorToEmpty(OfficeKeepingVolume doc) {
        boolean result = false;
        OfficeKeepingVolume document = doc;

        document.setCollector(null);
        document.setReturnDate(null);
        try {
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        if (result) {
            document.setWFResultDescription("");
        }
        return result;
    }

    public static boolean setPaperCopyCollectorToEmpty(PaperCopyDocument doc) {
        boolean result = false;
        PaperCopyDocument document = doc;

        document.setCollector(null);
        document.setReturnDate(null);
        try {
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        if (result) {
            document.setWFResultDescription("");
        }
        return result;
    }

    public static boolean setIncomingDocumentCollectorToEmpty(IncomingDocument doc) {
        boolean result = false;
        IncomingDocument document = doc;

        document.setCollector(null);
        document.setReturnDate(null);
        try {
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        if (result) {
            document.setWFResultDescription("");
        }
        return result;
    }

}