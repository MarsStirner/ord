package ru.efive.dms.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.efive.crm.data.Contragent;
import ru.efive.dms.data.DocumentForm;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.user.User;

public final class ApplicationHelper {

    private static Locale locale = new Locale("ru", "RU");

    public static String STORE_NAME = "E5 DMS";
    public static String NAMESPACE = "http://www.efive.ru/model/dictionary/1.0";
    public static String NAMESPACE_PREFIX = "e5-dms";
    public static String TYPE_FILE = "File";

    public static String USER_DAO = "userDao";
    public static String GROUP_DAO = "groupDao";
    public static String ROLE_DAO = "roleDao";
    public static String CONTRAGENT_DAO = "contragentDao";
    public static String GROUP_TYPE_DAO = "groupTypeDao";
    public static String SCAN_DAO = "scanDao";
    public static String DOCUMENT_FORM_DAO = "documentFormDao";
    public static String USER_ACCESS_LEVEL_DAO = "userAccessLevelDao";
    public static String PAPER_COPY_DOCUMENT_FORM_DAO = "paperCopyDao";
    public static String INCOMING_DOCUMENT_FORM_DAO = "incomingDao";
    public static String NUMERATOR_DAO = "numeratorDao";
    public static String OUTGOING_DOCUMENT_FORM_DAO = "outgoingDao";
    public static String REQUEST_DOCUMENT_FORM_DAO = "requestDao";
    public static String INTERNAL_DOCUMENT_FORM_DAO = "internalDao";
    public static String TASK_DAO = "taskDao";
    public static String NOMENCLATURE_DAO = "nomenclatureDao";
    public static String DELIVERY_TYPE_DAO = "deliveryTypeDao";
    public static String SENDER_TYPE_DAO = "senderTypeDao";
    public static String REGION_DAO = "regionDao";
    public static String REPORT_DAO = "reportDao";
    public static String OFFICE_KEEPING_RECORD_DAO = "officeKeepingRecordDao";
    public static String OFFICE_KEEPING_FILE_DAO = "officeKeepingFileDao";
    public static String OFFICE_KEEPING_VOLUME_DAO = "officeKeepingVolumeDao";
    public static String RECORD_BOOK_DAO = "recordBookDao";
    public static String ENGINE_DAO = "engineDao";

    public static final Locale getLocale() {
        return locale;
    }

    public static final String getFullTimeFormat() {
        return "dd.MM.yyyy HH:mm:ss z";
    }

    public static String getNotNull(String param) {
        if(param == null) {
            param = new String();
        }
        return param;
    }

    public static Date getNotNull(Date param) {
        if(param == null) {
            Calendar calendar = Calendar.getInstance(getLocale());
            calendar.set(0, 0, 0);
            param = calendar.getTime();
        }
        return param;
    }

    public static DocumentStatus getNotNull(DocumentStatus param) {
        if(param == null) {
            param = DocumentStatus.NEW;
        }
        return param;
    }

    public static DocumentForm getNotNull(DocumentForm param) {
        if(param == null) {
            param = new DocumentForm();
        }
        return param;
    }

    public static User getNotNull(User param) {
        if(param == null) {
            param = new User();
        }
        return param;
    }

    public static Contragent getNotNull(Contragent param) {
        if(param == null) {
            param = new Contragent();
        }
        return param;
    }

}