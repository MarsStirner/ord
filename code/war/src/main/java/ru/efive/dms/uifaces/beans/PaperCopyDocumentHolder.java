package ru.efive.dms.uifaces.beans;

import org.apache.commons.lang.StringUtils;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.officekeeping.OfficeKeepingVolumeSelectModal;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentStatus;
import ru.util.ApplicationHelper;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.efive.dms.util.ApplicationDAONames.*;

@Named("paper_copy_document")
@ConversationScoped
public class PaperCopyDocumentHolder extends AbstractDocumentHolderBean<PaperCopyDocument, Integer> implements Serializable {
    @Override
    public String delete() {
        String in_result = super.delete();
        if (in_result != null && in_result.equals("delete")) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("delete_document.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
                e.printStackTrace();
            }
            return in_result;
        } else {
            return in_result;
        }
    }

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            result = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,MSG_ERROR_ON_DELETE);
        }
        return result;
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            setDocument(sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).get(id));
            if (getDocument() == null) {
                setState(STATE_NOT_FOUND);
            } else {

                updateAttachments();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_INITIALIZE);
            e.printStackTrace();
        }
    }

    @Override
    protected void initNewDocument() {
        PaperCopyDocument doc = new PaperCopyDocument();
        doc.setDocumentStatus(DocumentStatus.NEW);
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        doc.setCreationDate(created);
        doc.setAuthor(sessionManagement.getLoggedUser());

        String parentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentDocumentId");
        if (StringUtils.isNotEmpty(parentId)) {
            doc.setParentDocumentId(parentId);
        }

        doc.setDocumentStatus(DocumentStatus.NEW);

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(sessionManagement.getLoggedUser());
        historyEntry.setDocType(doc.getDocumentType().getName());
        historyEntry.setParentId(doc.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<HistoryEntry>();
        history.add(historyEntry);
        doc.setHistory(history);

        setDocument(doc);
        initParentDocument();
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            PaperCopyDocument document = (PaperCopyDocument) getDocument();
            document = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,MSG_ERROR_ON_SAVE);
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        //if (validateHolder()) {
        try {
            PaperCopyDocument document = (PaperCopyDocument) getDocument();
            document = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                System.out.println("uploading newly created files");
                for (int i = 0; i < files.size(); i++) {
                    Attachment tmpAttachment = attachments.get(i);
                    if (tmpAttachment != null) {
                        tmpAttachment.setParentId(new String(("paper_copy_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
        }//}
        return result;
    }

    protected void initParentDocument() {
        String key = getDocument().getParentDocumentId();
        if (!key.isEmpty()) {
            int pos = key.indexOf('_');
            if (pos != -1) {
                String id = key.substring(pos + 1, key.length());

                List<PaperCopyDocument> paperCopies = sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).findAllDocumentsByParentId(key);
                if (key.contains("incoming")) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    getDocument().setParentDocument(in_doc);
                    getDocument().setRegistrationNumber((in_doc.getRegistrationNumber() != null ? in_doc.getRegistrationNumber() : "...") + "/" + (paperCopies.size() + 1));
                } else if (key.contains("outgoing")) {
                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    getDocument().setParentDocument(out_doc);
                    getDocument().setRegistrationNumber((out_doc.getRegistrationNumber() != null ? out_doc.getRegistrationNumber() : "...") + "/" + (paperCopies.size() + 1));
                } else if (key.contains("internal")) {
                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                    getDocument().setParentDocument(internal_doc);
                    getDocument().setRegistrationNumber((internal_doc.getRegistrationNumber() != null ? internal_doc.getRegistrationNumber() : "...") + "/" + (paperCopies.size() + 1));
                } else if (key.contains("request")) {
                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                    getDocument().setParentDocument(request_doc);
                    getDocument().setRegistrationNumber((request_doc.getRegistrationNumber() != null ? request_doc.getRegistrationNumber() : "...") + "/" + (paperCopies.size() + 1));
                }
            }
        }
        //this.setDocument(null);
    }

    // FILES

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void uploadAttachments(FileUploadDetails details) {
        try {
            if (details.getAttachment() != null) {
                Attachment attachment = details.getAttachment();
                attachment.setFileName(attachment.getFileName());
                if (getDocumentId() == null || getDocumentId() == 0) {
                    attachments.add(attachment);
                    files.add(details.getByteArray());
                } else {
                    attachment.setParentId(new String(("paper_copy_" + getDocumentId()).getBytes(), "utf-8"));
                    System.out.println("result of the upload operation - " + fileManagement.createFile(attachment, details.getByteArray()));
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_ATTACH);
            e.printStackTrace();
        }
    }

    public void versionAttachment(FileUploadDetails details, Attachment attachment, boolean majorVersion) {
        try {
            if (details.getByteArray() != null) {
                if (getDocumentId() == null || getDocumentId() == 0) {
                    if (attachments.contains(attachment)) {
                        int pos = attachments.indexOf(attachment);
                        if (pos > -1) {
                            files.remove(pos);
                            files.add(pos, details.getByteArray());
                        }
                    } else {
                        attachments.add(attachment);
                        files.add(details.getByteArray());
                    }
                } else {
                    System.out.println("result of the upload operation - " + fileManagement.createVersion(attachment, details.getByteArray(), majorVersion, details.getAttachment().getFileName()));
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_ATTACH);
            e.printStackTrace();
        }
    }

    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("paper_copy_" + getDocumentId());
            if (attachments == null) attachments = new ArrayList<Attachment>();
        }
    }

    public void deleteAttachment(Attachment attachment) {
        if (attachment != null) {
            if (fileManagement.deleteFile(attachment)) {
                updateAttachments();
            }
        }
    }

    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();

    // END OF FILES


    // MODAL HOLDERS

    public class VersionAppenderModal extends ModalWindowHolderBean {

        public Attachment getAttachment() {
            return attachment;
        }

        public void setAttachment(Attachment attachment) {
            this.attachment = attachment;
        }

        public void setMajorVersion(boolean majorVersion) {
            this.majorVersion = majorVersion;
        }

        public boolean isMajorVersion() {
            return majorVersion;
        }

        @Override
        protected void doShow() {
            super.doShow();
            setMajorVersion(false);
        }

        public void saveAttachment() {
            if (attachment != null) {
                versionAttachment(fileManagement.getDetails(), attachment, majorVersion);
            }
            versionAppenderModal.save();
        }

        @Override
        protected void doHide() {
            super.doHide();
            attachment = null;
        }

        private Attachment attachment;
        private boolean majorVersion;
    }

    public class VersionHistoryModal extends ModalWindowHolderBean {

        public Attachment getAttachment() {
            return attachment;
        }

        public void setAttachment(Attachment attachment) {
            this.attachment = attachment;
        }

        public List<Revision> getVersionList() {
            return versionList == null ? new ArrayList<Revision>() : versionList;
        }

        @Override
        protected void doShow() {
            super.doShow();
            versionList = fileManagement.getVersionHistory(attachment);
        }

        @Override
        protected void doHide() {
            super.doHide();
            versionList = null;
            attachment = null;
        }


        private Attachment attachment;
        private List<Revision> versionList;
    }

    public VersionAppenderModal getVersionAppenderModal() {
        return versionAppenderModal;
    }

    public void initializeVersionAppender(Attachment attachment) {
        versionAppenderModal.setAttachment(attachment);
        versionAppenderModal.setModalVisible(true);
    }

    public VersionHistoryModal getVersionHistoryModal() {
        return versionHistoryModal;
    }

    public void initializeVersionHistory(Attachment attachment) {
        versionHistoryModal.setAttachment(attachment);
        versionHistoryModal.show();
    }

    /* =================== */

    public class DeliveryTypeSelectModal extends ModalWindowHolderBean {
        public DeliveryType getValue() {
            return value;
        }

        public void setValue(DeliveryType value) {
            this.value = value;
        }

        public boolean selected(DeliveryType value) {
            return this.value != null && this.value.getValue().equals(value.getValue());
        }

        public void select(DeliveryType value) {
            this.value = value;
        }

        @Override
        protected void doSave() {
            super.doSave();
        }

        @Override
        protected void doShow() {
            super.doShow();
        }

        @Override
        protected void doHide() {
            super.doHide();
            value = null;
        }

        private DeliveryType value;
        private static final long serialVersionUID = 3204083909477490577L;
    }

    public DeliveryTypeSelectModal getDeliveryTypeSelectModal() {
        return deliveryTypeSelectModal;
    }

    /* =================== */

    // END OF MODAL HOLDERS

    /* =================== */


    public String getHomeLink() {
        String key = getDocument().getParentDocumentId();
        if (!key.isEmpty()) {
            int pos = key.indexOf('_');
            if (pos != -1) {
                if (key.contains("incoming")) {
                    return "/component/in/in_documents.xhtml";
                } else if (key.contains("outgoing")) {
                    return "/component/out/out_documents_by_number.xhtml";
                } else if (key.contains("internal")) {
                    return "/component/internal/internal_documents_by_number.xhtml";
                } else if (key.contains("request")) {
                    return "/component/request/request_documents.xhtml";
                }
            }
        }
        return null;
    }

    private DeliveryTypeSelectModal deliveryTypeSelectModal = new DeliveryTypeSelectModal();
    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

    public OfficeKeepingVolumeSelectModal getOfficeKeepingVolumeSelectModal() {
        return officeKeepingVolumeSelectModal;
    }

    public UserSelectModalBean getCollectorSelectModal() {
        return collectorSelectModal;
    }

    private UserSelectModalBean collectorSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setCollector(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private OfficeKeepingVolumeSelectModal officeKeepingVolumeSelectModal = new OfficeKeepingVolumeSelectModal() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setOfficeKeepingVolume(getOfficeKeepingVolume());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getOfficeKeepingVolumes().markNeedRefresh();
        }
    };

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

    private ProcessorModalBean processorModal = new ProcessorModalBean() {

        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            PaperCopyDocument document = (PaperCopyDocument) actionResult.getProcessedData();
            HistoryEntry historyEntry = getHistoryEntry();
            if (document.getDocumentStatus().getId() == 110 && document.getCollector() != null) {
                historyEntry.setCommentary(document.getCollector().getDescriptionShort() + " : на руках до " + (new SimpleDateFormat("dd.MM.yyyy")).format(document.getReturnDate()));
            }
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<HistoryEntry>();
                }
                history.add(historyEntry);
                document.setHistory(history);
            }
            setDocument(document);
            PaperCopyDocumentHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            PaperCopyDocument document = (PaperCopyDocument) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();

            if (StringUtils.isNotEmpty(in_result)) {
                setActionResult(in_result);
            }
        }
    };

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("dictionaryManagement")
    private transient DictionaryManagementBean dictionaryManagement;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;

    private static final long serialVersionUID = -3206695147971430673L;
}