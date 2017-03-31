package ru.entity.model.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Nastya Peshekhonova
 *         TODO: Временное решение, следует перевести это в таблицe базы данных и использовать ее
 *         TODO как же вы ска меня достали, руки ваши бы из зада переткнуть повыше, на курсы запишитесь хотя-бы, или книжки почитайте
 */

public enum DocumentType {
    IncomingDocument("IncomingDocument", getIncomingDocumentStatuses(), getIncomingDocumentActions()),
    InternalDocument("InternalDocument", getInternalDocumentStatuses(), getInternalDocumentActions()),
    OutgoingDocument("OutgoingDocument", getOutgoingDocumentStatuses(), getOutgoingDocumentActions()),
    Task("Task", getTaskStatuses(), getTaskActions()),
    OfficeKeepingFile("OfficeKeepingFile", getOfficeKeepingFileStatuses(), getOfficeKeepingFileActions()),
    OfficeKeepingVolume("OfficeKeepingVolume", getOfficeKeepingVolumeStatuses(), getOfficeKeepingVolumeActions()),
    RequestDocument("RequestDocument", getRequestDocumentStatuses(), getRequestDocumentActions());

    private final String name;
    private final List<DocumentStatus> statuses;
    private final List<DocumentAction> actions;

    DocumentType(String name, List<DocumentStatus> statuses, List<DocumentAction> actions) {
        this.name = name;
        this.statuses = statuses;
        this.actions = actions;
    }

    public static synchronized String getStatusName(String documentName, int id) {
        DocumentType documentType = valueOf(documentName);
        for (DocumentStatus status : documentType.getStatuses()) {
            if (status.getId() == id)
                return status.getName();
        }
        return "";
    }

    public static synchronized DocumentStatus getStatus(String documentName, int id) {
        DocumentType documentType = valueOf(documentName);
        for (DocumentStatus status : documentType.getStatuses()) {
            if (status.getId() == id)
                return status;
        }
        return DocumentStatus.NEW;
    }

    public synchronized static String getActionName(String documentName, int id) {
        DocumentType documentType = valueOf(documentName);
        for (DocumentAction action : documentType.getActions()) {
            if (action.getId() == id)
                return action.getName();
        }
        return "";
    }

    public static List<Integer> getStatusIdListByStrKey(String documentName, String strKey) {
        DocumentType documentType = valueOf(documentName);
        List<Integer> result = new ArrayList<>();
        strKey = strKey.toLowerCase();

        for (DocumentStatus status : documentType.getStatuses()) {
            if (status.getName().contains(strKey))
                result.add(status.getId());
        }
        return result;
    }

    public static List<DocumentStatus> getIncomingDocumentStatuses() {
        return Arrays.asList(DocumentStatus.ON_REGISTRATION,
                DocumentStatus.CHECK_IN_2,
                DocumentStatus.ON_EXECUTION_80,
                DocumentStatus.EXECUTE,
                DocumentStatus.IN_ARCHIVE_100,
                DocumentStatus.EXTRACT,
                DocumentStatus.SOURCE_DESTROY,
                DocumentStatus.REDIRECT);
    }

    public static List<DocumentAction> getIncomingDocumentActions() {
        return Arrays.asList(DocumentAction.CHECK_IN_1,
                DocumentAction.REDIRECT_TO_EXECUTION_2,
                DocumentAction.EXECUTED,
                DocumentAction.IN_ARCHIVE_90,
                DocumentAction.OUT_ARCHIVE,
                DocumentAction.RETURN_TO_ARCHIVE,
                DocumentAction.SOURCE_DESTROY_125,
                DocumentAction.REDIRECT_IN_OTHER_ARCHIVE_135,
                DocumentAction.CREATE,
                DocumentAction.CHANGE_EXECUTION_DATE,
                DocumentAction.CHANGE_ACCESS_LEVEL);
    }

    public static List<DocumentStatus> getInternalDocumentStatuses() {
        return Arrays.asList(DocumentStatus.DOC_PROJECT_1,
                DocumentStatus.ON_CONSIDERATION,
                DocumentStatus.AGREEMENT_3,
                DocumentStatus.CHECK_IN_5,
                DocumentStatus.ON_EXECUTION_80,
                DocumentStatus.EXECUTE,
                DocumentStatus.IN_ARCHIVE_100,
                DocumentStatus.EXTRACT,
                DocumentStatus.REDIRECT,
                DocumentStatus.CANCEL_150);
    }

    public static List<DocumentAction> getInternalDocumentActions() {
        return Arrays.asList(DocumentAction.REDIRECT_TO_CONSIDERATION_1,
                DocumentAction.REDIRECT_TO_AGREEMENT,
                DocumentAction.CHECK_IN_5,
                DocumentAction.CHECK_IN_6,
                DocumentAction.CHECK_IN_55,
                DocumentAction.REDIRECT_TO_EXECUTION_80,
                DocumentAction.IN_ARCHIVE_90,
                DocumentAction.OUT_ARCHIVE,
                DocumentAction.RETURN_TO_ARCHIVE,
                DocumentAction.REDIRECT_IN_OTHER_ARCHIVE_135,
                DocumentAction.CANCEL_150,
                DocumentAction.CREATE,
                DocumentAction.RETURN_TO_EDITING,
                DocumentAction.CHANGE_ACCESS_LEVEL);
    }

    public static List<DocumentStatus> getOutgoingDocumentStatuses() {
        return Arrays.asList(DocumentStatus.DOC_PROJECT_1,
                DocumentStatus.ON_CONSIDERATION,
                DocumentStatus.AGREEMENT_3,
                DocumentStatus.CHECK_IN_80,
                DocumentStatus.EXECUTE,
                DocumentStatus.IN_ARCHIVE_100);
    }

    public static List<DocumentAction> getOutgoingDocumentActions() {
        return Arrays.asList(DocumentAction.REDIRECT_TO_CONSIDERATION_2,
                DocumentAction.REDIRECT_TO_AGREEMENT,
                DocumentAction.CHECK_IN_80,
                DocumentAction.CHECK_IN_81,
                DocumentAction.CHECK_IN_82,
                DocumentAction.EXECUTE_90,
                DocumentAction.IN_ARCHIVE_99,
                DocumentAction.CREATE,
                DocumentAction.RETURN_TO_EDITING,
                DocumentAction.CHANGE_ACCESS_LEVEL);
    }

    public static List<DocumentStatus> getTaskStatuses() {
        return Arrays.asList(DocumentStatus.DRAFT,
                DocumentStatus.ON_EXECUTION_2,
                DocumentStatus.EXECUTED,
                DocumentStatus.CANCEL_4);
    }

    public static List<DocumentAction> getTaskActions() {
        return Arrays.asList(DocumentAction.REDIRECT_TO_EXECUTION_1,
                DocumentAction.EXECUTED,
                DocumentAction.SHORT_CANCEL,
                DocumentAction.CREATE);
    }

    public static List<DocumentStatus> getOfficeKeepingFileStatuses() {
        return Arrays.asList(DocumentStatus.ACTION_PROJECT,
                DocumentStatus.REGISTERED);
    }

    public static List<DocumentAction> getOfficeKeepingFileActions() {
        return Collections.singletonList(DocumentAction.CHECK_IN_1);
    }

    public static List<DocumentStatus> getOfficeKeepingVolumeStatuses() {
        return Arrays.asList(DocumentStatus.VOLUME_PROJECT,
                DocumentStatus.OPEN,
                DocumentStatus.CLOSE,
                DocumentStatus.EXTRACT);
    }

    public static List<DocumentAction> getOfficeKeepingVolumeActions() {
        return Arrays.asList(DocumentAction.CHECK_IN_1,
                DocumentAction.CLOSE,
                DocumentAction.EXTRACT,
                DocumentAction.RETURN);
    }

    public static List<DocumentStatus> getRequestDocumentStatuses() {
        return Arrays.asList(DocumentStatus.ON_REGISTRATION,
                DocumentStatus.CHECK_IN_2,
                DocumentStatus.ON_EXECUTION_80,
                DocumentStatus.EXECUTE,
                DocumentStatus.IN_ARCHIVE_100);

    }

    public static List<DocumentAction> getRequestDocumentActions() {
        return Arrays.asList(DocumentAction.CHECK_IN_1,
                DocumentAction.REDIRECT_TO_EXECUTION_2,
                DocumentAction.EXECUTE_80,
                DocumentAction.IN_ARCHIVE_90,
                DocumentAction.OUT_ARCHIVE,
                DocumentAction.RETURN_TO_ARCHIVE,
                DocumentAction.REDIRECT_IN_OTHER_ARCHIVE_135,
                DocumentAction.CREATE);
    }

    public String getName() {
        return name;
    }

    public List<DocumentStatus> getStatuses() {
        return statuses;
    }

    public List<DocumentAction> getActions() {
        return actions;
    }
}
