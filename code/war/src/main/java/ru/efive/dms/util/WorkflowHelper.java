package ru.efive.dms.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.utils.SessionManagementBean;
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
import ru.entity.model.referenceBook.Nomenclature;
import ru.entity.model.referenceBook.Role;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;
import ru.hitsl.sql.dao.interfaces.document.*;
import ru.hitsl.sql.dao.interfaces.referencebook.NomenclatureDao;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;
import ru.util.ApplicationHelper;

import javax.faces.context.FacesContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class WorkflowHelper {
    private static final Logger taskLogger = LoggerFactory.getLogger("TASK");
    private static final int LEFT_PAD_COUNT = 5;
    private static final char LEFT_PAD_CHAR = '0';

    @Autowired
    @Qualifier("roleDao")
    private RoleDao roleDao;
    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;
    @Autowired
    @Qualifier("incomingDocumentDao")
    private IncomingDocumentDao incomingDocumentDao;
    @Autowired
    @Qualifier("internalDocumentDao")
    private InternalDocumentDao internalDocumentDao;
    @Autowired
    @Qualifier("requestDocumentDao")
    private RequestDocumentDao requestDocumentDao;

    @Autowired
    @Qualifier("taskDao")
    private TaskDao taskDao;

    @Autowired
    @Qualifier("nomenclatureDao")
    private NomenclatureDao nomenclatureDao;

    @Autowired
    @Qualifier("officeKeepingFileDao")
    private OfficeKeepingFileDao officeKeepingFileDao;

    public boolean checkInternalPropertiesForArchiving(InternalDocument doc) {
        doc.setWFResultDescription("");
        return true;
    }

    public boolean checkRequestPropertiesForArchiving(RequestDocument doc) {
        doc.setWFResultDescription("");
        return true;
    }

    public boolean setOfficeKeepingVolumeRegistrationNumber(OfficeKeepingVolume doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getParentFile() == null) {
            in_result.append("Необходимо указать корректную номенклатуру дела;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {

                if (doc.getVolumeIndex() == null || doc.getVolumeIndex().isEmpty()) {
                    StringBuilder in_number = new StringBuilder();
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

    public boolean checkOfficeKeepingVolumePropertiesForUnfile(OfficeKeepingVolume doc) {
        StringBuilder in_result = new StringBuilder("");
        if (doc.getCollector() == null) {
            in_result.append("Необходимо указать кому будет выдан том дела;").append(System.getProperty("line.separator"));
        }
        if (doc.getReturnDate() == null) {
            in_result.append("Необходимо указать дату возврата тома дела;").append(System.getProperty("line.separator"));
        }
        if (in_result.toString().equals("")) {
            doc.setWFResultDescription("");
            return true;
        } else {
            doc.setWFResultDescription(in_result.toString());
        }
        return false;
    }

    public boolean setOfficeKeepingVolumeCollectorToEmpty(OfficeKeepingVolume doc) {
        doc.setCollector(null);
        doc.setReturnDate(null);
        doc.setWFResultDescription("");
        return true;
    }

    public boolean changeTaskExecutionDateAction(NoStatusAction changeDateAction, Task task) {
        boolean result = false;
        try {
            LocalDateTime choosenDate = null;
            for (EditableProperty property : changeDateAction.getProperties()) {
                if (property.getName().equals("executionDate")) {
                    choosenDate = (LocalDateTime) property.getValue();
                }
            }
            if (choosenDate != null) {
                final String delegateReason = "Делегирован %s " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(LocalDateTime.now());
                task.setWFResultDescription(String.format(delegateReason, task.getExecutors().iterator().next().getDescription()));
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

    public boolean doTaskDelegateAction(NoStatusAction delegateAction, Task task) {
        boolean result = false;
        try {
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
                        List<String> sendTo = new ArrayList<>();
                        sendTo.add(selectedUser.getEmail());
                        message.setSendTo(sendTo);
                        sendMailActivity.setMessage(message);
                    }
                }
                final String delegateReason = "Делегирован %s " + new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm").format(LocalDateTime.now());
                task.setWFResultDescription(String.format(delegateReason, task.getExecutors().iterator().next().getDescription()));
                final HashSet<User> users = new HashSet<>(1);
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

    public boolean setOutgoingRegistrationNumber(OutgoingDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getController() == null) {
            in_result.append("Необходимо выбрать Руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getExecutor() == null) {
            in_result.append("Необходимо выбрать Ответственного исполнителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getContragent() == null) {
            in_result.append("Необходимо выбрать Адресата;").append(System.getProperty("line.separator"));
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {

                if (doc.getRegistrationNumber() == null || doc.getRegistrationNumber().isEmpty()) {
                    final StringBuilder in_number = new StringBuilder();

                    Nomenclature in_nomenclature = nomenclatureDao.getUserDefaultNomenclature(doc.getController());
                    Set<Role> in_roles = new HashSet<>(1);
                    Role in_office;
                    if (in_nomenclature != null) {
                        in_office = roleDao.findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                        in_number.append(in_nomenclature.getCode()).append("-");
                    } else {
                        in_office = roleDao.findRoleByType(RoleType.valueOf("OFFICE_01"));
                        in_number.append("01-");
                    }
                    if ((in_office != null) && (!in_roles.contains(in_office))) {
                        in_roles.add(in_office);
                    }
                    doc.setRoleEditors(in_roles);
                    final String in_count = StringUtils.leftPad(
                            String.valueOf(
                                    new HashSet<>(
                                            outgoingDocumentDao.findRegistratedDocumentsByCriteria(in_number.toString())
                                    ).size() + 1
                            ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                    );
                    in_number.append(in_count);

                    doc.setRegistrationNumber(in_number.toString());



                    doc.setRegistrationDate(LocalDateTime.now());
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

    public boolean checkOutgoingPropertiesForArchiving(OutgoingDocument doc) {
        doc.setWFResultDescription("");
        return true;
    }

    public boolean setIncomingDocumentParentVolumeUnitsCount(IncomingDocument document) {
        document.setWFResultDescription("");
        return true;
    }

    public boolean setIncomingRegistrationNumber(IncomingDocument doc) {
        boolean result = false;
        final StringBuilder in_result = new StringBuilder("");
        if (doc.getController() == null) {
            in_result.append("Необходимо выбрать Руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getContragent() == null) {
            in_result.append("Необходимо выбрать Корреспондента;").append(System.getProperty("line.separator"));
        }
        if ((doc.getRecipientUsers() == null || doc.getRecipientUsers().isEmpty()) && (doc.getRecipientGroups() == null || doc.getRecipientGroups()
                .isEmpty())) {
            in_result.append("Необходимо выбрать Адресатов;").append(System.getProperty("line.separator"));
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;").append(System.getProperty("line.separator"));
        }

        if (in_result.length() == 0) {
            try {
                if (StringUtils.isEmpty(doc.getRegistrationNumber())) {
                    final StringBuilder in_number = new StringBuilder();
                    Nomenclature in_nomenclature = nomenclatureDao.getUserDefaultNomenclature(doc.getController());
                    Set<Role> in_roles = new HashSet<>();
                    Role in_office;
                    if (in_nomenclature != null) {
                        in_office = roleDao
                                .findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                        in_number.append(in_nomenclature.getCode()).append("-");
                    } else {
                        in_office = roleDao.findRoleByType(RoleType.valueOf("OFFICE_01"));
                        in_number.append("01-");
                    }
                    if ((in_office != null) && (!in_roles.contains(in_office))) {
                        in_roles.add(in_office);
                    }
                    doc.setRoleEditors(in_roles);

                    in_number.append(
                            StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    incomingDocumentDao.findRegistratedDocumentsByCriteria(in_number.toString())
                                            ).size() + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            )
                    );
                    doc.setRegistrationNumber(in_number.toString());

                    doc.setRegistrationDate(LocalDateTime.now());
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

    public boolean checkIncomingPropertiesForArchiving(IncomingDocument doc) {
        doc.setWFResultDescription("");
        return true;
    }

    public boolean setInternalRegistrationNumber(InternalDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getController() == null) {
            in_result.append("Необходимо выбрать Руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getResponsible() == null) {
            in_result.append("Необходимо выбрать Контроль исполнения;").append(System.getProperty("line.separator"));
        }
        if ((doc.getRecipientUsers() == null || doc.getRecipientUsers().size() == 0) && (doc.getRecipientGroups() == null || doc.getRecipientGroups()
                .size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;").append(System.getProperty("line.separator"));
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {


                if (StringUtils.isEmpty(doc.getRegistrationNumber())) {
                    StringBuffer in_number = new StringBuffer();
                    Nomenclature in_nomenclature = nomenclatureDao.getUserDefaultNomenclature(doc.getController());
                    Set<Role> in_roles = new HashSet<>(1);
                    Role in_office;
                    if (in_nomenclature != null) {
                        in_office = roleDao
                                .findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                        in_number.append(in_nomenclature.getCode()).append("-");
                    } else {
                        in_office = roleDao.findRoleByType(RoleType.valueOf("OFFICE_01"));
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


                    Map<String, Object> in_filters = new HashMap<>();
                    String in_count;
                    SimpleDateFormat ydf = new SimpleDateFormat("yyyy");
                    switch (in_form) {
                        case "Распоряжение":
                            //Р/индекс/номер по порядку
                            in_number = new StringBuffer();
                            if (in_nomenclature != null) {
                                in_number.append("Р/").append(in_nomenclature.getCode()).append("/");
                            } else {
                                in_number.append("Р/01/");
                            }

                            in_filters.put("registrationNumber", in_number.toString());
                            in_filters.put("form", doc.getForm());
                            in_count = StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    internalDocumentDao.findDocumentsByCriteria(
                                                            in_filters, true, false
                                                    )
                                            ).size() + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            );
                            in_number.append(in_count);

                            break;
                        case "Методическое пособие":
                            in_filters.put("registrationNumber", "%");
                            in_filters.put("form", doc.getForm());
                            in_count = StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    internalDocumentDao.findDocumentsByCriteria(in_filters, true, false)
                                            ).size() + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            );
                            in_number.append(in_count).append("/Методические пособия/").append(ydf.format(Calendar.getInstance().getTime()));


                            break;
                        case "Инструкция":
                            in_filters.put("registrationNumber", in_number.toString());
                            in_filters.put("form", doc.getForm());
                            in_count = StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    internalDocumentDao.findDocumentsByCriteria(in_filters, true, false)
                                            ).size() + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            );
                            in_number.append(in_count).append("/Инструкции/").append(ydf.format(Calendar.getInstance().getTime()));


                            break;
                        case "Служебная записка":
                            in_number = new StringBuffer();
                            if (in_nomenclature != null) {
                                in_number.append("СЗ/").append(in_nomenclature.getCode()).append("/");
                            } else {
                                in_number.append("СЗ/01/");
                            }
                            in_filters.put("registrationNumber", in_number.toString());
                            in_filters.put("form", doc.getForm());
                            in_count = StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    internalDocumentDao.findDocumentsByCriteria(in_filters, true, false)
                                            ).size() + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            );
                            in_number.append(in_count);

                                /*in_number=new StringBuffer();
                                        in_filters.put("registrationNumber", "%");
                                        in_filters.put("form", document.getForm());
                                        in_count=new  StringBuffer("0000"+String.valueOf(sessionManagement.getDAO(InternalDocumentDAOImpl.class,INTERNAL_DOCUMENT_FORM_DAO).findDocumentsByCriteria(in_filters,true,false).size()+1));
                                        in_number.append("СЗ/"+in_count.substring(in_count.length()-4)+"/"+ydf.format(java.util.Calendar.getInstance ().getTime()));*/

                            break;
                        case "Информационное письмо":
                            in_number = new StringBuffer();
                            in_filters.put("registrationNumber", "%");
                            in_filters.put("form", doc.getForm());
                            in_count = StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    internalDocumentDao
                                                            .findDocumentsByCriteria(in_filters, true, false)
                                            ).size()
                                                    //+sessionManagement.getDAO(InternalDocumentDAOImpl.class,INTERNAL_DOCUMENT_FORM_DAO).findRegistratedDocumentsByForm("Служебная записка").size()
                                                    + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            );
                            in_number.append(in_count).append("/").append(ydf.format(Calendar.getInstance().getTime()));

                            break;
                        case "Гарантийное письмо":
                            in_number = new StringBuffer();
                            in_filters.put("registrationNumber", "%");
                            in_filters.put("form", doc.getForm());
                            in_count = StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    internalDocumentDao
                                                            .findDocumentsByCriteria(in_filters, true, false)
                                            ).size()
                                                    //+sessionManagement.getDAO(InternalDocumentDAOImpl.class,INTERNAL_DOCUMENT_FORM_DAO).findRegistratedDocumentsByForm("Служебная записка").size()
                                                    + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            );
                            in_number.append("ГП/").append(in_count).append("/").append(ydf.format(Calendar.getInstance().getTime()));

                            break;
                        case "Приказ":
                            in_number = new StringBuffer();

                            Map<String, Object> outDateOrder_filters = new HashMap<>();
                            outDateOrder_filters.put("registrationNumber", "%/%");
                            outDateOrder_filters.put("form", doc.getForm());
                            outDateOrder_filters.put("closePeriodRegistrationFlag", "false");
                            int outDateOrderCount = new HashSet<>(
                                    internalDocumentDao.findDocumentsByCriteria(outDateOrder_filters, true, false)
                            ).size();

                            in_filters.put("registrationNumber", "%");
                            in_filters.put("form", doc.getForm());
                            in_filters.put("closePeriodRegistrationFlag", "false");
                            int summaryOrderCount = new HashSet<>(
                                    internalDocumentDao.findDocumentsByCriteria(in_filters, true, false)
                            ).size();

                            in_count = StringUtils.leftPad(String.valueOf(summaryOrderCount - outDateOrderCount + 1), LEFT_PAD_COUNT, LEFT_PAD_CHAR);
                            in_number.append(in_count);
                            break;
                        case "Правила внутреннего распорядка":
                            in_number = new StringBuffer();
                            in_filters.put("registrationNumber", "%");
                            in_filters.put("form", doc.getForm());
                            in_count = StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    internalDocumentDao.findDocumentsByCriteria(in_filters, true, false)
                                            ).size() + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            );
                            in_number.append("ПВР/").append(in_count).append("/").append(ydf.format(Calendar.getInstance().getTime()));
                            break;
                        case "Положение":
                            in_number = new StringBuffer();
                            in_filters.put("registrationNumber", "%");
                            in_filters.put("form", doc.getForm());
                            in_count = StringUtils.leftPad(
                                    String.valueOf(
                                            new HashSet<>(
                                                    internalDocumentDao.findDocumentsByCriteria(in_filters, true, false)
                                            ).size() + 1
                                    ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                            );
                            in_number.append("Положение/").append(in_count).append("/").append(ydf.format(Calendar.getInstance().getTime()));
                            break;
                        default:
                            doc.setWFResultDescription("Данный вид документа не может быть зарегистрирован!");
                            return false;
                    }
                    doc.setRegistrationNumber(in_number.toString());



                    doc.setRegistrationDate(LocalDateTime.now());
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

    public boolean setInternalRegistrationNumberOnOutDate(InternalDocument doc) {
        boolean result = false;
        doc.setClosePeriodRegistrationFlag(true);
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getController() == null) {
            in_result.append("Необходимо выбрать Руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getResponsible() == null) {
            in_result.append("Необходимо выбрать Ответственного;").append(System.getProperty("line.separator"));
        }
        if ((doc.getRecipientUsers() == null || doc.getRecipientUsers().size() == 0) && (doc.getRecipientGroups() == null || doc.getRecipientGroups()
                .size() == 0)) {
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


        SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(
                context, "#{sessionManagement}", SessionManagementBean.class
        );
        Map<String, Object> in_filters = new HashMap<>();
        //TODO вычистить эту ересь при внедрении нумераторов
        in_filters.put("registrationNumber", doc.getRegistrationNumber());
        in_filters.put("DEPRECATED_REGISTRATION_DATE", doc.getRegistrationDate());
        in_filters.put(DocumentSearchMapKeys.FORM_KEY, doc.getForm());
        List<InternalDocument> copyDocuments = internalDocumentDao.findDocumentsByCriteria(in_filters, false, true);
        if (copyDocuments.size() != 0) {
            in_result.append("Документ под таким номером уже существует;").append(System.getProperty("line.separator"));
            for (InternalDocument internalDocument : copyDocuments) {
                in_result.append(internalDocument.getId()).append("-\'").append(internalDocument.getRegistrationNumber()).append("\';");
            }

            doc.setRegistrationNumber(null);
        }

        if (in_result.toString().equals("")) {
            try {

                Nomenclature in_nomenclature = nomenclatureDao.getUserDefaultNomenclature(doc.getController());
                Set<Role> in_roles = new HashSet<>(1);
                Role in_office;
                if (in_nomenclature != null) {
                    in_office = roleDao.findRoleByType(
                            RoleType.valueOf(
                                    "OFFICE_" + in_nomenclature.getCode()
                            )
                    );
                } else {
                    in_office = roleDao.findRoleByType(RoleType.valueOf("OFFICE_01"));
                }
                if ((in_office != null) && (!in_roles.contains(in_office))) {
                    in_roles.add(in_office);
                }
                doc.setRoleEditors(in_roles);


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

    public boolean checkInternalDocumentPropertiesForReview(InternalDocument doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");
        if (doc.getController() == null) {
            in_result.append("Необходимо указать руководителя;").append(System.getProperty("line.separator"));
        }
        if (doc.getResponsible() == null) {
            in_result.append("Необходимо указать ответственного;").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {


                if (StringUtils.isEmpty(doc.getRegistrationNumber())) {
                    Nomenclature in_nomenclature = nomenclatureDao.getUserDefaultNomenclature(doc.getController());
                    Role in_administrationRole = roleDao.findRoleByType(RoleType.ADMINISTRATOR);
                    Set<Role> in_roles = new HashSet<>(2);
                    in_roles.add(in_administrationRole);

                    Role in_office;
                    if (in_nomenclature != null) {
                        in_office = roleDao
                                .findRoleByType(RoleType.valueOf("OFFICE_" + in_nomenclature.getCode()));
                    } else {
                        in_office = roleDao.findRoleByType(RoleType.valueOf("OFFICE_01"));
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

    public boolean setRequestRegistrationNumber(RequestDocument document) {
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
        if ((document.getRecipientUsers() == null || document.getRecipientUsers().size() == 0) && (document.getRecipientGroups() == null || document
                .getRecipientGroups().size() == 0)) {
            in_result.append("Необходимо выбрать Адресатов;").append(System.getProperty("line.separator"));
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            in_result.append("Необходимо заполнить Краткое содержание; \n");
        }

        if (in_result.toString().equals("")) {
            try {
                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(
                        context, "#{sessionManagement}", SessionManagementBean.class
                );


                if (document.getRegistrationNumber() == null || document.getRegistrationNumber().isEmpty()) {
                    final StringBuilder in_number = new StringBuilder();
                    String in_count = StringUtils.leftPad(
                            String.valueOf(
                                    new HashSet<>(
                                            requestDocumentDao.findRegistratedDocuments()
                                    ).size() + 1
                            ), LEFT_PAD_COUNT, LEFT_PAD_CHAR
                    );
                    in_number.append(in_count);
                    document.setRegistrationNumber(in_number.toString());

                    document.setRegistrationDate(LocalDateTime.now());
                    Set<Role> in_roles = new HashSet<>();
                    Role in_office = roleDao.findRoleByType(RoleType.REQUEST_MANAGER);
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

    public boolean setTaskRegistrationNumber(Task doc) {
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
                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(
                        context, "#{sessionManagement}", SessionManagementBean.class
                );
                if (StringUtils.isEmpty(doc.getTaskNumber())) {
                    //Номер не задан
                    StringBuilder in_number = new StringBuilder();
                    StringBuilder in_count = new StringBuilder();
                    Map<String, Object> in_filters = new HashMap<>();

                    final String key = doc.getRootDocumentId();
                    if (StringUtils.isNotEmpty(key)) {
                        in_filters.put("rootDocumentId", key);
                        if (key.contains("_")) {
                            final Integer idInt = ApplicationHelper.getIdFromUniqueIdString(key);
                            if (idInt != null) {
                                if (key.contains("incoming")) {
                                    IncomingDocument in_doc = incomingDocumentDao.getItemBySimpleCriteria(idInt);
                                    in_number.append(in_doc.getRegistrationNumber()).append("/");
                                } else if (key.contains("outgoing")) {
                                    OutgoingDocument out_doc = outgoingDocumentDao.getItemBySimpleCriteria(idInt);
                                    in_number.append(out_doc.getRegistrationNumber()).append("/");
                                } else if (key.contains("internal")) {
                                    InternalDocument internal_doc = internalDocumentDao.getItemBySimpleCriteria(idInt);
                                    in_number.append(internal_doc.getRegistrationNumber()).append("/");
                                } else if (key.contains("request")) {
                                    RequestDocument request_doc = requestDocumentDao.getItemBySimpleCriteria(idInt);
                                    in_number.append(request_doc.getRegistrationNumber()).append("/");
                                } else if (key.contains("task")) {
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

                    in_count.append(
                            String.valueOf(
                                    taskDao.countItems(null, in_filters, false) + 1
                            )
                    );
                    in_number.append(in_count);
                    doc.setTaskNumber(in_number.toString());

                    doc.setRegistrationDate(LocalDateTime.now());
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

    public boolean cloneTasks(Task doc) {
        try {
            final FacesContext context = FacesContext.getCurrentInstance();
            final SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(
                    context, "#{sessionManagement}", SessionManagementBean.class
            );

            //TODO когда-нибудь, когда мир станет вновь светлым и ясным, когда прекратятся войны, исчезнет коррупция,
            //TODO ну или что более вероятно когда перепишут эту фабрику действий - то этот жуткий костыль превратится в красивое и элегантное решение
            //TODO когда-нибудь... но только не сегодня. @27-10-2014
            if (doc.getExecutors().size() > 1) {
                taskLogger.debug("Group task start clone");
                //Групповое поручение
                final Task templateTask = (Task) doc.clone();
                templateTask.setId(null);
                templateTask.getExecutors().clear();
                templateTask.setParent(doc);
                templateTask.getHistory().clear();

                final Map<String, Object> in_filters = new HashMap<>();
                final Matcher matcher = Pattern.compile("(.*)([0-9]+)$").matcher(doc.getTaskNumber());
                if (matcher.find()) {
                    templateTask.setTaskNumber(matcher.group(1));
                    in_filters.put("rootDocumentId", doc.getRootDocumentId());
                } else {
                    in_filters.put("taskDocumentId", "");
                }

                for (User currentExecutor : doc.getExecutors()) {
                    final Task currentTask = (Task) templateTask.clone();
                    final Set<User> executorsSet = new HashSet<>(1);
                    executorsSet.add(currentExecutor);
                    currentTask.setExecutors(executorsSet);
                    //+2 потому что жизнь-боль и первое поручение еще не сохранено в БД с корректным номером
                    int numberOffset = taskDao.countItems(null, in_filters, false) + 2;
                    currentTask.setTaskNumber(currentTask.getTaskNumber().concat(String.valueOf(numberOffset)));
                    Set<HistoryEntry> history = new HashSet<>(1);
                    final HistoryEntry entry = new HistoryEntry();
                    entry.setActionId(DocumentAction.REDIRECT_TO_EXECUTION_1.getId());
                    entry.setCreated(LocalDateTime.now());
                    entry.setCommentary("Создано из группового поручения");
                    entry.setFromStatusId(1);
                    entry.setToStatusId(DocumentStatus.ON_EXECUTION_2.getId());
                    currentTask.setHistory(history);
                    currentTask.setDocumentStatus(DocumentStatus.ON_EXECUTION_2);
                    taskDao.save(currentTask);
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


    public boolean setOfficeKeepingFileRegistrationNumber(OfficeKeepingFile doc) {
        boolean result = false;
        FacesContext context = FacesContext.getCurrentInstance();
        StringBuilder in_result = new StringBuilder("");

        if (doc.getFileIndex() == null || doc.getFileIndex().equals("")) {
            in_result.append("Необходимо заполнить поле Индекс").append(System.getProperty("line.separator"));
        }

        if (in_result.toString().equals("")) {
            try {
                SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(
                        context, "#{sessionManagement}", SessionManagementBean.class
                );
                long checkCount = officeKeepingFileDao.countItemsByNumber(doc.getFileIndex());

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


}