package ru.entity.model.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nastya Peshekhonova
 *         TODO: Временное решение, следует перевести это в таблицe базы данных и использовать ее
 * @see ru.entity.model.document.Type
 */

public enum DocumentType {
    PaperCopyDocument("PaperCopyDocument", getPaperCopyDocumentStatuses(), getPaperCopyDocumentActions()),
    IncomingDocument("IncomingDocument", getIncomingDocumentStatuses(), getIncomingDocumentActions()),
    InternalDocument("InternalDocument", getInternalDocumentStatuses(), getInternalDocumentActions()),
    OutgoingDocument("OutgoingDocument", getOutgoingDocumentStatuses(), getOutgoingDocumentActions()),
    Task("Task", getTaskStatuses(), getTaskActions()),
    OfficeKeepingFile("OfficeKeepingFile", getOfficeKeepingFileStatuses(), getOfficeKeepingFileActions()),
    OfficeKeepingVolume("OfficeKeepingVolume", getOfficeKeepingVolumeStatuses(), getOfficeKeepingVolumeActions()),
    RequestDocument("RequestDocument", getRequestDocumentStatuses(), getRequestDocumentActions()),
    Numerator("Numerator", getNumeratorStatuses(), getNumeratorActions()),
    ScanCopyDocument("ScanCopyDocument", getScanCopyDocumentStatuses(), getScanCopyDocumentActions());

    private final String name;
    private final List<DocumentStatus> statuses;
    private final List<DocumentAction> actions;

    public String getName() {
        return name;
    }

    public List<DocumentStatus> getStatuses() {
        return statuses;
    }

    public List<DocumentAction> getActions() {
        return actions;
    }

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

    public synchronized static List<Integer> getStatusIdListByStrKey(String documentName, String strKey) {
        DocumentType documentType = valueOf(documentName);
        List<Integer> result = new ArrayList<Integer>();
        strKey = strKey.toLowerCase();

        for (DocumentStatus status : documentType.getStatuses()) {
            if (status.getName().contains(strKey))
                result.add(status.getId());
        }
        return result;
    }


    private static List<DocumentStatus> getPaperCopyDocumentStatuses() {
        return Arrays.asList(DocumentStatus.PROJECT,
                DocumentStatus.CHECK_IN_2,
                DocumentStatus.IN_ARCHIVE_99,
                DocumentStatus.EXTRACT,
                DocumentStatus.SOURCE_DESTROY,
                DocumentStatus.REDIRECT,
                DocumentStatus.SEND,
                DocumentStatus.RECIVE);
    }

    private static List<DocumentAction> getPaperCopyDocumentActions() {
        return Arrays.asList(DocumentAction.CHECK_IN_1,
                DocumentAction.IN_ARCHIVE_99,
                DocumentAction.EXTRACT,
                DocumentAction.RETURN_TO_ARCHIVE,
                DocumentAction.SOURCE_DESTROY_120,
                DocumentAction.REDIRECT_IN_OTHER_ARCHIVE_130,
                DocumentAction.SEND,
                DocumentAction.RECIVE,
                DocumentAction.CREATE);
    }

    private static List<DocumentStatus> getIncomingDocumentStatuses() {
        return Arrays.asList(DocumentStatus.ON_REGISTRATION,
                DocumentStatus.CHECK_IN_2,
                DocumentStatus.ON_EXECUTION_80,
                DocumentStatus.EXECUTE,
                DocumentStatus.IN_ARCHIVE_100,
                DocumentStatus.EXTRACT,
                DocumentStatus.SOURCE_DESTROY,
                DocumentStatus.REDIRECT);
    }

    private static List<DocumentAction> getIncomingDocumentActions() {
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

    private static List<DocumentStatus> getInternalDocumentStatuses() {
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

    private static List<DocumentAction> getInternalDocumentActions() {
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

    private static List<DocumentStatus> getOutgoingDocumentStatuses() {
        return Arrays.asList(DocumentStatus.DOC_PROJECT_1,
                DocumentStatus.ON_CONSIDERATION,
                DocumentStatus.AGREEMENT_3,
                DocumentStatus.CHECK_IN_80,
                DocumentStatus.EXECUTE,
                DocumentStatus.IN_ARCHIVE_100);
    }

    private static List<DocumentAction> getOutgoingDocumentActions() {
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

    private static List<DocumentStatus> getTaskStatuses() {
        return Arrays.asList(DocumentStatus.DRAFT,
                DocumentStatus.ON_EXECUTION_2,
                DocumentStatus.EXECUTED,
                DocumentStatus.CANCEL_4);
    }

    private static List<DocumentAction> getTaskActions() {
        return Arrays.asList(DocumentAction.REDIRECT_TO_EXECUTION_1,
                DocumentAction.EXECUTED,
                DocumentAction.SHORT_CANCEL,
                DocumentAction.CREATE);
    }

    private static List<DocumentStatus> getOfficeKeepingFileStatuses() {
        return Arrays.asList(DocumentStatus.ACTION_PROJECT,
                DocumentStatus.REGISTERED);
    }

    private static List<DocumentAction> getOfficeKeepingFileActions() {
        return Arrays.asList(DocumentAction.CHECK_IN_1);
    }

    private static List<DocumentStatus> getOfficeKeepingVolumeStatuses() {
        return Arrays.asList(DocumentStatus.VOLUME_PROJECT,
                DocumentStatus.OPEN,
                DocumentStatus.CLOSE,
                DocumentStatus.EXTRACT);
    }

    private static List<DocumentAction> getOfficeKeepingVolumeActions() {
        return Arrays.asList(DocumentAction.CHECK_IN_1,
                DocumentAction.CLOSE,
                DocumentAction.EXTRACT,
                DocumentAction.RETURN);
    }

    private static List<DocumentStatus> getRequestDocumentStatuses() {
        return Arrays.asList(DocumentStatus.ON_REGISTRATION,
                DocumentStatus.CHECK_IN_2,
                DocumentStatus.ON_EXECUTION_80,
                DocumentStatus.EXECUTE,
                DocumentStatus.IN_ARCHIVE_100);

    }

    private static List<DocumentAction> getRequestDocumentActions() {
        return Arrays.asList(DocumentAction.CHECK_IN_1,
                DocumentAction.REDIRECT_TO_EXECUTION_2,
                DocumentAction.EXECUTE_80,
                DocumentAction.IN_ARCHIVE_90,
                DocumentAction.OUT_ARCHIVE,
                DocumentAction.RETURN_TO_ARCHIVE,
                DocumentAction.REDIRECT_IN_OTHER_ARCHIVE_135,
                DocumentAction.CREATE);
    }

    private static List<DocumentStatus> getNumeratorStatuses() {
        return Arrays.asList(DocumentStatus.NEW);
    }

    private static List<DocumentAction> getNumeratorActions() {
        return new ArrayList<DocumentAction>();
    }

    private static List<DocumentStatus> getScanCopyDocumentStatuses() {
        return Arrays.asList(DocumentStatus.NEW);
    }

    private static List<DocumentAction> getScanCopyDocumentActions() {
        return new ArrayList<DocumentAction>();
    }
}
