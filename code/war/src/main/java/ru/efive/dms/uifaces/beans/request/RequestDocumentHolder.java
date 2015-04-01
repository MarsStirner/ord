package ru.efive.dms.uifaces.beans.request;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.contragent.ContragentListHolderBean;
import ru.efive.dms.uifaces.beans.roles.RoleListSelectModalBean;
import ru.efive.dms.uifaces.beans.task.DocumentTaskTreeHolder;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserUnitsSelectModalBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.crm.Contragent;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.*;
import static ru.efive.dms.util.security.Permissions.Permission.*;

@ManagedBean(name="request_doc")
@ViewScoped
public class RequestDocumentHolder extends AbstractDocumentHolderBean<RequestDocument, Integer> implements Serializable {

    //Именованный логгер (REQUEST_DOCUMENT)
    private static final Logger LOGGER = LoggerFactory.getLogger("REQUEST_DOCUMENT");

    public boolean isReadPermission() {
        return permissions.hasPermission(READ);
    }

    public boolean isEditPermission() {
        return permissions.hasPermission(WRITE);
    }

    public boolean isExecutePermission() {
        return permissions.hasPermission(EXECUTE);
    }

    @Override
    public boolean isCanDelete() {
        if (!permissions.hasPermission(WRITE)) {
            setStateComment("Доступ запрещен");
            LOGGER.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            setStateComment("Доступ запрещен");
            LOGGER.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanView() {
        if (!permissions.hasPermission(READ)) {
            setStateComment("Доступ запрещен");
            LOGGER.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(READ);
    }

    //TODO ACL
    private Permissions permissions;

    //Для проверки прав доступа
    @EJB
    private PermissionChecker permissionChecker;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;

    @ManagedProperty("#{contragentList}")
    private transient ContragentListHolderBean contragentList;

    public ContragentListHolderBean getContragentList() {
        return contragentList;
    }

    public void setContragentList(ContragentListHolderBean contragentList) {
        this.contragentList = contragentList;
    }

    @Inject
    @Named("documentTaskTree")
    private transient DocumentTaskTreeHolder taskTreeHolder;


    @Override
    public String delete() {
        String in_result = super.delete();
        if (AbstractDocumentHolderBean.ACTION_RESULT_DELETE.equals(in_result)) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../delete_document.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_DELETE);
                LOGGER.error("INTERNAL ERROR ON DELETE:", e);
            }
        }
        return in_result;
    }

    @Override
    protected boolean deleteDocument() {
        try {
            final boolean result = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_DELETE);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("INTERNAL ERROR ON DELETE_DOCUMENT:", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
            return false;
        }
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
        final User currentUser = sessionManagement.getLoggedUser();
        LOGGER.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final RequestDocument document = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).get(id);
            if (!checkState(document, currentUser)) {
                setDocument(document);
                return;
            }
            setDocument(document);
            //Проверка прав на открытие
            permissions = permissionChecker.getPermissions(sessionManagement, document);
            if(isReadPermission()){
                //Простановка факта просмотра записи
                if(sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).registerViewFact(document, currentUser)){
                    FacesContext.getCurrentInstance().addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                taskTreeHolder.setRootDocumentId(getDocument().getUniqueId());
                taskTreeHolder.refresh();
                updateAttachments();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_INITIALIZE);
            e.printStackTrace();
        }
    }

    /**
     * Проверяем является ли документ валидным, и есть ли у пользователя к нему уровень допуска
     *
     * @param document документ для проверки
     * @return true - допуск есть
     */
    private boolean checkState(final RequestDocument document, final User user) {
        if (document == null) {
            setState(STATE_NOT_FOUND);
            LOGGER.warn("Document NOT FOUND");
            return false;
        }
        if (document.isDeleted()) {
            setState(STATE_DELETED);
            setStateComment("Документ удален");
            LOGGER.warn("Document[{}] IS DELETED", document.getId());
            return false;
        }
        return true;
    }

    @Override
    protected void initNewDocument() {
        permissions = Permissions.ALL_PERMISSIONS;
        RequestDocument doc = new RequestDocument();
        doc.setDocumentStatus(DocumentStatus.NEW);
        final Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        doc.setDeliveryDate(created);
        doc.setCreationDate(created);
        doc.setAuthor(sessionManagement.getLoggedUser());

        String isDocumentTemplate = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("isDocumentTemplate");
        if (StringUtils.isNotEmpty(isDocumentTemplate) && "yes".equals(isDocumentTemplate.toLowerCase())) {
            doc.setTemplateFlag(true);
        } else {
            doc.setTemplateFlag(false);
        }

        DocumentForm form = null;
        List<DocumentForm> list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByCategoryAndValue("Обращения граждан", "Заявка на лечение");
        if (list.size() > 0) {
            form = list.get(0);

        } else {
            list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByCategory("Обращения граждан");
            if (list.size() > 0) {
                form = list.get(0);
            }
        }
        if (form != null) {
            doc.setForm(form);
        }

        DeliveryType deliveryType = null;
        List<DeliveryType> deliveryTypes = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, DELIVERY_TYPE_DAO).findByValue("Электронная почта");
        if (deliveryTypes.size() > 0) {
            deliveryType = deliveryTypes.get(0);

        } else {
            deliveryTypes = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, DELIVERY_TYPE_DAO).findDocuments();
            if (deliveryTypes.size() > 0) {
                deliveryType = deliveryTypes.get(0);
            }
        }
        if (deliveryType != null) {
            doc.setDeliveryType(deliveryType);
        }

        SenderType senderType = null;
        List<SenderType> senderTypes = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, SENDER_TYPE_DAO).findByValue("Физическое лицо");
        if (senderTypes.size() > 0) {
            senderType = senderTypes.get(0);
        } else {
            senderTypes = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, SENDER_TYPE_DAO).findDocuments();
            if (senderTypes.size() > 0) {
                senderType = senderTypes.get(0);
            }
        }
        if (senderType != null) {
            doc.setSenderType(senderType);
        }

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(sessionManagement.getLoggedUser());
        historyEntry.setDocType(doc.getType());
        historyEntry.setParentId(doc.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<HistoryEntry>();
        history.add(historyEntry);
        doc.setHistory(history);
        doc.setShortDescription("Выписка из истории болезни");
        setDocument(doc);
    }

    @Override
    protected boolean saveDocument() {
        try {
            RequestDocument document = getDocument();
            document = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
                return false;
            } else {
                setDocument(document);
                //Установка идшника для поиска поручений
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("saveDocument ERROR:", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            RequestDocument document = getDocument();
            document = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                final User currentUser = sessionManagement.getLoggedUser();
                //Простановка факта просмотра записи
                sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).registerViewFact(document, currentUser);

                Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
                document.setCreationDate(created);
                document.setAuthor(currentUser);

                PaperCopyDocument paperCopy = new PaperCopyDocument();
                paperCopy.setDocumentStatus(DocumentStatus.NEW);
                paperCopy.setCreationDate(created);
                paperCopy.setAuthor(currentUser);

                String parentId = document.getUniqueId();
                if (StringUtils.isNotEmpty(parentId)) {
                    paperCopy.setParentDocumentId(parentId);
                }

                paperCopy.setRegistrationNumber(".../1");
                HistoryEntry historyEntry = new HistoryEntry();
                historyEntry.setCreated(created);
                historyEntry.setStartDate(created);
                historyEntry.setOwner(currentUser);
                historyEntry.setDocType(paperCopy.getDocumentType().getName());
                historyEntry.setParentId(paperCopy.getId());
                historyEntry.setActionId(0);
                historyEntry.setFromStatusId(1);
                historyEntry.setEndDate(created);
                historyEntry.setProcessed(true);
                historyEntry.setCommentary("");
                Set<HistoryEntry> history = new HashSet<HistoryEntry>();
                history.add(historyEntry);
                paperCopy.setHistory(history);

                paperCopy.setParentDocument(document);

                sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);

                LOGGER.debug("uploading newly created files");
                for (int i = 0; i < files.size(); i++) {
                    Attachment tmpAttachment = attachments.get(i);
                    if (tmpAttachment != null) {
                        tmpAttachment.setParentId(new String(("incoming_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                result = true;
                //Установка идшника для поиска поручений
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
            }
        } catch (Exception e) {
            LOGGER.error("saveNewDocument ERROR:", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
        }
        return result;
    }
    // FILES

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void uploadAttachments(FileUploadDetails details) {
        try {
            if (details.getAttachment() != null) {
                Attachment attachment = details.getAttachment();
                //attachment.setFileName(new String(attachment.getFileName().getBytes(), "utf-8"));
                attachment.setFileName(attachment.getFileName());
                if (getDocumentId() == null || getDocumentId() == 0) {
                    attachments.add(attachment);
                    files.add(details.getByteArray());
                } else {
                    attachment.setParentId(new String(("request_" + getDocumentId()).getBytes(), "utf-8"));
                    System.out.println("result of the upload operation - " + fileManagement.createFile(attachment, details.getByteArray()));
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_ATTACH);
            LOGGER.error("uploadAttachments ERROR:", e);
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
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_ATTACH);
            LOGGER.error("uploadAttachments ERROR:", e);
        }
    }

    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("request_" + getDocumentId());
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
            getDocument().setDeliveryType(getValue());
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

    public class RegionSelectModal extends ModalWindowHolderBean {
        /**
         *
         */
        private static final long serialVersionUID = 4875705849883346982L;

        public Region getValue() {
            return value;
        }

        public void setValue(Region value) {
            this.value = value;
        }

        public boolean selected(Region value) {
            return this.value != null && this.value.getValue().equals(value.getValue());
        }

        public void select(Region value) {
            this.value = value;
        }

        public void unselect() {
            this.value = null;
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setRegion(getValue());
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

        private Region value;

    }

    public RegionSelectModal getRegionSelectModal() {
        return regionSelectModal;
    }

      /* =================== */

    public class ContragentSelectModal extends ModalWindowHolderBean {

        public ContragentListHolderBean getContragentList() {
            return contragentList;
        }

        public Contragent getContragent() {
            return contragent;
        }

        public void setContragent(Contragent contragent) {
            this.contragent = contragent;
        }

        public void select(Contragent contragent) {
            this.contragent = contragent;
        }

        public boolean selected(Contragent contragent) {
            return this.contragent != null && this.contragent.getFullName().equals(contragent.getFullName());
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setContragent(getContragent());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getContragentList().setFilter("");
            getContragentList().markNeedRefresh();
            setContragent(null);
        }

        private Contragent contragent;
        private static final long serialVersionUID = -5852388924786285818L;
    }

    public ContragentSelectModal getContragentSelectModal() {
        return contragentSelectModal;
    }
    // END OF MODAL HOLDERS

      /* =================== */

    // END OF MODAL HOLDERS

      /* =================== */

    private RegionSelectModal regionSelectModal = new RegionSelectModal();
    private DeliveryTypeSelectModal deliveryTypeSelectModal = new DeliveryTypeSelectModal();
    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

    public UserSelectModalBean getExecutorSelectModal() {
        return executorSelectModal;
    }

    public UserSelectModalBean getControllerSelectModal() {
        return controllerSelectModal;
    }

    public UserListSelectModalBean getRecipientUsersSelectModal() {
        return recipientUsersSelectModal;
    }

    public UserListSelectModalBean getPersonReadersPickList() {
        return personReadersPickList;
    }

    public UserListSelectModalBean getPersonEditorsPickList() {
        return personEditorsPickList;
    }

    public RoleListSelectModalBean getRoleReadersPickList() {
        return roleReadersPickList;
    }

    public RoleListSelectModalBean getRoleEditorsPickList() {
        return roleEditorsPickList;
    }

    private UserSelectModalBean controllerSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            getDocument().setController(getUser());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserSelectModalBean executorSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            getDocument().setExecutor(getUser());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserListSelectModalBean recipientUsersSelectModal = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            getDocument().setRecipientUsers(getUsers());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUsers(null);
        }

        @Override
        protected void doShow() {
            super.doShow();
            if (getDocument() != null && getDocument().getRecipientUsers() != null) {
                ArrayList<User> tmpList = new ArrayList<User>();
                tmpList.addAll(getDocument().getRecipientUsers());
                setUsers(tmpList);
            }
        }
    };

    private UserUnitsSelectModalBean recipientUnitsSelectModal = new UserUnitsSelectModalBean() {

        @Override
        protected void doSave() {
            getDocument().setRecipientGroups(new HashSet<Group>(getGroups()));
            getDocument().setRecipientUsers(getUsers());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            setUsers(null);
            setGroups(null);
        }

        @Override
        protected void doShow() {
            super.doShow();
            if (getDocument() != null && getDocument().getRecipientGroups() != null) {
                ArrayList<Group> tmpGroupList = new ArrayList<Group>();
                tmpGroupList.addAll(getDocument().getRecipientGroupsList());
                setGroups(tmpGroupList);
            }
            if (getDocument() != null && getDocument().getRecipientUsers() != null) {
                ArrayList<User> tmpUserList = new ArrayList<User>();
                tmpUserList.addAll(getDocument().getRecipientUsers());
                setUsers(tmpUserList);
            }
        }
    };

    private UserListSelectModalBean personReadersPickList = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            getDocument().setPersonReaders(getUsers());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUsers(null);
        }

        @Override
        protected void doShow() {
            super.doShow();
            if (getDocument() != null && getDocument().getPersonReaders() != null) {
                ArrayList<User> tmpList = new ArrayList<User>();
                tmpList.addAll(getDocument().getPersonReaders());
                setUsers(tmpList);
            }
        }
    };

    private UserListSelectModalBean personEditorsPickList = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            getDocument().setPersonEditors(getUsers());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUsers(null);
        }

        @Override
        protected void doShow() {
            super.doShow();
            if (getDocument() != null && getDocument().getPersonEditors() != null) {
                ArrayList<User> tmpList = new ArrayList<User>();
                tmpList.addAll(getDocument().getPersonEditors());
                setUsers(tmpList);
            }
        }
    };

    private RoleListSelectModalBean roleReadersPickList = new RoleListSelectModalBean() {

        @Override
        protected void doSave() {
            getDocument().setRoleReaders(getRoles());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getRoleList().setFilter("");
            getRoleList().markNeedRefresh();
            setRoles(null);
        }

        @Override
        protected void doShow() {
            super.doShow();
            if (getDocument() != null && getDocument().getRoleReaders() != null) {
                ArrayList<Role> tmpList = new ArrayList<Role>();
                tmpList.addAll(getDocument().getRoleReaders());
                setRoles(tmpList);
            }
        }
    };

    private RoleListSelectModalBean roleEditorsPickList = new RoleListSelectModalBean() {

        @Override
        protected void doSave() {
            getDocument().setRoleEditors(getRoles());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getRoleList().setFilter("");
            getRoleList().markNeedRefresh();
            setRoles(null);
        }

        @Override
        protected void doShow() {
            super.doShow();
            if (getDocument() != null && getDocument().getRoleEditors() != null) {
                ArrayList<Role> tmpList = new ArrayList<Role>();
                tmpList.addAll(getDocument().getRoleEditors());
                setRoles(tmpList);
            }
        }
    };

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

    public void setRecipientUnitsSelectModal(UserUnitsSelectModalBean recipientUnitsSelectModal) {
        this.recipientUnitsSelectModal = recipientUnitsSelectModal;
    }

    public UserUnitsSelectModalBean getRecipientUnitsSelectModal() {
        return recipientUnitsSelectModal;
    }

    private ProcessorModalBean processorModal = new ProcessorModalBean() {
        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            RequestDocument document = (RequestDocument) actionResult.getProcessedData();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<HistoryEntry>();
                }
                history.add(getHistoryEntry());
                document.setHistory(history);
            }
            setDocument(document);
            RequestDocumentHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            RequestDocument document = (RequestDocument) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();
            if (StringUtils.isNotEmpty(in_result)) {
                setActionResult(in_result);
            }
        }
    };

    public boolean isUsersDialogSelected() {
        return isUsersDialogSelected;
    }

    public void setUsersDialogSelected(boolean isUsersDialogSelected) {
        if (isUsersDialogSelected) {
            this.isUsersDialogSelected = true;
            this.isGroupsDialogSelected = false;
        }
    }

    public boolean isGroupsDialogSelected() {
        return isGroupsDialogSelected;
    }

    public void setGroupsDialogSelected(boolean isGroupsDialogSelected) {
        if (isGroupsDialogSelected) {
            this.isGroupsDialogSelected = true;
            this.isUsersDialogSelected = false;
        }
    }

    public void setStateComment(String stateComment) {
        this.stateComment = stateComment;
    }

    public String getStateComment() {
        return stateComment;
    }

    private ContragentSelectModal contragentSelectModal = new ContragentSelectModal();

    private boolean isUsersDialogSelected = true;
    private boolean isGroupsDialogSelected = false;

    private transient String stateComment;

    public DocumentTaskTreeHolder getTaskTreeHolder() {
        return taskTreeHolder;
    }

    public void setTaskTreeHolder(DocumentTaskTreeHolder taskTreeHolder) {
        this.taskTreeHolder = taskTreeHolder;
    }


    private static final long serialVersionUID = 4716264614655470705L;
}