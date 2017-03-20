package ru.efive.dms.uifaces.beans;

import org.apache.commons.lang.StringUtils;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.entity.model.document.RecordBookDocument;
import ru.hitsl.sql.dao.RecordBookDocumentDAOImpl;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.RECORD_BOOK_DAO;

@Named("record_book_doc")
@ConversationScoped
public class RecordBookDocumentHolder extends AbstractDocumentHolderBean<RecordBookDocument> implements Serializable {

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            result = sessionManagement.getDAO(RecordBookDocumentDAOImpl.class, RECORD_BOOK_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_DELETE);
        }
        return result;
    }

    @Override
    protected void initDocument(Integer id) {
        try {
            setDocument(sessionManagement.getDAO(RecordBookDocumentDAOImpl.class, RECORD_BOOK_DAO).get(id));
            if (getDocument() == null) {
                setDocumentNotFound();
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
        RecordBookDocument doc = new RecordBookDocument();
        Calendar calendar = Calendar.getInstance(new Locale("ru", "Ru"));
        doc.setCreationDate(calendar.getTime());
        doc.setPlannedDate(calendar.getTime());
        doc.setAuthor(sessionManagement.getLoggedUser());
        setDocument(doc);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            RecordBookDocument document = getDocument();
            document = sessionManagement.getDAO(RecordBookDocumentDAOImpl.class, RECORD_BOOK_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE);
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        if (validateHolder()) {
            try {
                RecordBookDocument document = getDocument();
                document = sessionManagement.getDAO(RecordBookDocumentDAOImpl.class, RECORD_BOOK_DAO).save(document);
                if (document == null) {
                    FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
                } else {
                    System.out.println("uploading newly created files");
                    for (int i = 0; i < files.size(); i++) {
                        Attachment tmpAttachment = attachments.get(i);
                        if (tmpAttachment != null) {
                            tmpAttachment.setParentId(new String(("request_" + getDocumentId()).getBytes(), "utf-8"));
                            fileManagement.createFile(tmpAttachment, files.get(i));
                        }
                    }
                    result = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
            }
        }
        return result;
    }



    protected boolean validateHolder() {
        boolean result = true;
        FacesContext context = FacesContext.getCurrentInstance();
        if (StringUtils.isEmpty(getDocument().getShortDescription())) {
            context.addMessage(null, MSG_SHORT_DESCRIPTION_NOT_SET);
            result = false;
        }

        return result;
    }
       // FILES

    public List<Attachment> getAttachments() {
        return attachments;
    }





    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("request_" + getDocumentId());
            if (attachments == null) attachments = new ArrayList<>();
        }
    }

    public void deleteAttachment(Attachment attachment) {
        if (attachment != null) {
            if (fileManagement.deleteFile(attachment)) {
                updateAttachments();
            }
        }
    }

    private List<Attachment> attachments = new ArrayList<>();
    private List<byte[]> files = new ArrayList<>();

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

        @Override
        protected void doSave() {
            super.doSave();
            /*if (attachment != null) {
                   versionAttachment(fileManagement.getDetails(), attachment, majorVersion);
               }*/
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

    // END OF MODAL HOLDERS

    /* =================== */


    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;

    private static final long serialVersionUID = 4716264614655470705L;
}