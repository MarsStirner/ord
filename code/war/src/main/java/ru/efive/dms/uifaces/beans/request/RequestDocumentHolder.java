package ru.efive.dms.uifaces.beans.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import ru.efive.crm.data.Contragent;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.dao.*;
import ru.efive.dms.data.Attachment;
import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.PaperCopyDocument;
import ru.efive.dms.data.Region;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.data.SenderType;
import ru.efive.dms.uifaces.beans.ContragentListHolderBean;
import ru.efive.dms.uifaces.beans.DictionaryManagementBean;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.roles.RoleListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserUnitsSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;

@Named("request_doc")
@ConversationScoped
public class RequestDocumentHolder extends AbstractDocumentHolderBean<RequestDocument, Integer> implements Serializable {

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            result = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно удалить документ. Попробуйте повторить позже.", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при удалении.", ""));
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
            setDocument(sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).get(id));
            if (getDocument() == null) {
                setState(STATE_NOT_FOUND);
            } else {
                Set<Integer> allReadersId = new HashSet<Integer>();

                User currentUser = sessionManagement.getLoggedUser();
                //currentUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(currentUser.getLogin(), currentUser.getPassword());
                int userId = currentUser.getId();
                if (userId > 0) {
                    boolean isAdminRole = false;
                    List<Role> in_roles = currentUser.getRoleList();
                    if (in_roles != null) {
                        for (Role in_role : in_roles) {
                            if (in_role.getRoleType().equals(RoleType.ADMINISTRATOR)) {
                                isAdminRole = true;
                                break;
                            }
                        }
                    }

                    RequestDocument document = getDocument();

                    List<Role> documentRoles = new ArrayList<Role>();
                    documentRoles.addAll(document.getRoleEditors());
                    documentRoles.addAll(document.getRoleReaders());
                    Set<Integer> documentRolesId = new HashSet<Integer>();
                    for (Role role : documentRoles) {
                        documentRolesId.add(role.getId());
                    }

                    Set<Integer> userRolesId = new HashSet<Integer>();
                    for (Role role : currentUser.getRoles()) {
                        userRolesId.add(role.getId());
                    }

                    boolean isUserReaderByRole = false;
                    if (userRolesId.size() > 0 && documentRolesId.size() > 0) {
                        userRolesId.retainAll(documentRolesId);
                        isUserReaderByRole = (userRolesId.size() > 0);
                    }


                    if (!(isAdminRole || isUserReaderByRole)) {
                        if (document.getAuthor() != null) {
                            allReadersId.add(document.getAuthor().getId());
                        }
                        if (document.getExecutor() != null) {
                            allReadersId.add(document.getExecutor().getId());
                        }
                        if (document.getController() != null) {
                            allReadersId.add(document.getController().getId());
                        }

                        List<User> someReaders = new ArrayList<User>();
                        someReaders.addAll(document.getRecipientUsers());
                        someReaders.addAll(document.getPersonReaders());
                        someReaders.addAll(document.getPersonEditors());
                        Set<Group> recipientGroups = currentUser.getGroups();
                        if (recipientGroups.size() != 0) {
                            Iterator itr = recipientGroups.iterator();
                            while (itr.hasNext()) {
                                Group group = (Group) itr.next();
                                someReaders.addAll(group.getMembersList());
                            }
                        }


                        if (someReaders.size() != 0) {
                            Iterator itr = someReaders.iterator();
                            while (itr.hasNext()) {
                                User user = (User) itr.next();
                                allReadersId.add(user.getId());
                            }
                        }

                        if (!allReadersId.contains(currentUser.getId())) {
                            TaskDAOImpl taskDao = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO);
                            if(!taskDao.isAccessGrantedByAssociation(sessionManagement.getLoggedUser(), "request_" + document.getId())) {
                                setState(STATE_FORBIDDEN);
                                setStateComment("Доступ запрещен");
                                return;
                            }
                        }

                    }
                }


                updateAttachments();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при инициализации.", ""));
            e.printStackTrace();
        }
    }

    @Override
    protected void initNewDocument() {
        RequestDocument doc = new RequestDocument();
        doc.setDocumentStatus(DocumentStatus.NEW);
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        doc.setDeliveryDate(created);
        doc.setCreationDate(created);
        doc.setAuthor(sessionManagement.getLoggedUser());

        String isDocumentTemplate = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("isDocumentTemplate");
        if (isDocumentTemplate != null && isDocumentTemplate.toLowerCase().equals("yes")) {
            doc.setTemplateFlag(true);
        } else {
            doc.setTemplateFlag(false);
        }

        DocumentForm form = null;
        List<DocumentForm> list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategoryAndValue("Обращения граждан", "Заявка на лечение");
        if (list.size() > 0) {
            form = list.get(0);

        } else {
            list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategory("Обращения граждан");
            if (list.size() > 0) {
                form = list.get(0);
            }
        }
        if (form != null) {
            doc.setForm(form);
        }

        DeliveryType deliveryType = null;
        List<DeliveryType> deliveryTypes = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, ApplicationHelper.DELIVERY_TYPE_DAO).findByValue("Электронная почта");
        if (deliveryTypes.size() > 0) {
            deliveryType = deliveryTypes.get(0);

        } else {
            deliveryTypes = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, ApplicationHelper.DELIVERY_TYPE_DAO).findDocuments();
            if (deliveryTypes.size() > 0) {
                deliveryType = deliveryTypes.get(0);
            }
        }
        if (deliveryType != null) {
            doc.setDeliveryType(deliveryType);
        }

        SenderType senderType = null;
        List<SenderType> senderTypes = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, ApplicationHelper.SENDER_TYPE_DAO).findByValue("Физическое лицо");
        if (senderTypes.size() > 0) {
            senderType = senderTypes.get(0);
        } else {
            senderTypes = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, ApplicationHelper.SENDER_TYPE_DAO).findDocuments();
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
        boolean result = false;
        try {
            RequestDocument document = (RequestDocument) getDocument();
            document = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
            } else {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при сохранении.", ""));
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        //resultif (validateHolder()) {
        try {
            RequestDocument document = (RequestDocument) getDocument();
            document = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
            } else {
                Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
                document.setCreationDate(created);
                document.setAuthor(sessionManagement.getLoggedUser());

                PaperCopyDocument paperCopy = new PaperCopyDocument();
                paperCopy.setDocumentStatus(DocumentStatus.NEW);
                paperCopy.setCreationDate(created);
                paperCopy.setAuthor(sessionManagement.getLoggedUser());

                String parentId = document.getUniqueId();
                if (parentId != null || !parentId.isEmpty()) {
                    paperCopy.setParentDocumentId(parentId);
                }

                paperCopy.setRegistrationNumber(".../1");
                HistoryEntry historyEntry = new HistoryEntry();
                historyEntry.setCreated(created);
                historyEntry.setStartDate(created);
                historyEntry.setOwner(sessionManagement.getLoggedUser());
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

                sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, ApplicationHelper.PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);

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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при сохранении нового документа.", ""));
        }//}
        return result;
    }


    @Override
    protected String doAfterCreate() {
        requestDocumentList.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterEdit() {
        requestDocumentList.markNeedRefresh();
        return super.doAfterEdit();
    }

    @Override
    protected String doAfterDelete() {
        requestDocumentList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        requestDocumentList.markNeedRefresh();
        return super.doAfterSave();
    }

/*	protected boolean validateHolder() {
		boolean result = true;
		FacesContext context = FacesContext.getCurrentInstance();		
		if (getDocument().getSenderLastName()==null) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо указать Фамилию адресанта", ""));
			result = false;
		}	
		if (getDocument().getRecipientUsers()==null || getDocument().getRecipientUsers().size()==0) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Адресатов", ""));
			result = false;
		}	    		   
		if (getDocument().getShortDescription()==null||getDocument().getShortDescription().equals("")) {
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо заполнить Краткое содержание", ""));
			result = false;
		}	    		   

		return result;
	}
*/

    public boolean isCurrentUserAccessEdit() {
        User inUser = sessionManagement.getLoggedUser();
        //inUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(inUser.getLogin(), inUser.getPassword());
        RequestDocument reqDoc = getDocument();

        List<Integer> recipUsers = new ArrayList<Integer>();
        if (reqDoc.getRecipientUsers() != null) {
            for (User user : reqDoc.getRecipientUsers()) {
                recipUsers.add(user.getId());
            }
        }
        if (reqDoc.getPersonReaders() != null) {
            for (User user : reqDoc.getPersonReaders()) {
                recipUsers.add(user.getId());
            }
        }
        if (recipUsers.contains(inUser.getId())) {
            return true;
        }

        List<Integer> accesGroups = new ArrayList<Integer>();
        if (reqDoc.getRecipientGroups() != null) {
            for (Group group : reqDoc.getRecipientGroups()) {
                accesGroups.add(group.getId());
            }
        }
        for (Group group : inUser.getGroups()) {
            if (accesGroups.contains(group.getId())) {
                return true;
            }
        }

        List<Integer> accessRoles = new ArrayList<Integer>();
        if (reqDoc.getRoleReaders() != null) {
            for (Role role : reqDoc.getRoleReaders()) {
                accessRoles.add(role.getId());
            }
        }
        for (Role role : inUser.getRoles()) {
            if (accessRoles.contains(role.getId())) {
                return true;
            }
        }

        return false;
    }
    
    protected boolean isCurrentUserDocEditor() {
        User in_user = sessionManagement.getLoggedUser();
        //in_user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(in_user.getLogin(), in_user.getPassword());
        if (in_user != null) {
            if (in_user.isAdministrator()) {
                return true;
            }

            RequestDocument request_doc = getDocument();

            List<User> in_editors = new ArrayList<User>();
            if (request_doc.getPersonEditors() != null) in_editors.addAll(request_doc.getPersonEditors());
            if (request_doc.getController() != null) in_editors.add(request_doc.getController());
            if (request_doc.getAuthor() != null) in_editors.add(request_doc.getAuthor());
            if (in_editors.contains(in_user)) {
                return true;
            }

            List<Role> in_roles = request_doc.getRoleEditors();
            if (in_roles != null) {
                for (Role in_role : in_user.getRoleList()) {
                    if (in_roles.contains(in_role)) {
                        return true;
                    }
                }
            }

        }
        return false;
    }

    protected boolean isCurrentUserAdvDocReader() {
        User in_user = sessionManagement.getLoggedUser();
        //in_user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(in_user.getLogin(), in_user.getPassword());

        RequestDocument request_doc = getDocument();

        List<User> in_advReaders = new ArrayList<User>();
        if (request_doc.getPersonReaders() != null) in_advReaders.addAll(request_doc.getPersonReaders());
        if (in_advReaders.contains(in_user)) {
            return true;
        }

        List<Role> in_roles = request_doc.getRoleReaders();
        if (in_roles != null)
            for (Role in_role : in_user.getRoleList()) {
                if (in_roles.contains(in_role)) {
                    return true;
                }
            }

        return false;
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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при вложении файла.", ""));
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
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при вложении файла.", ""));
            e.printStackTrace();
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
            return this.value != null ? this.value.getValue().equals(value.getValue()) : false;
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
            return this.value != null ? this.value.getValue().equals(value.getValue()) : false;
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
            return this.contragent == null ? false : this.contragent.getFullName().equals(contragent.getFullName());
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

    public String getRequisitesTabHeader() {
        return "<span><span>Реквизиты</span></span>";
    }

    public boolean isRequisitesTabSelected() {
        return isRequisitesTabSelected;
    }

    public void setRequisitesTabSelected(boolean isRequisitesTabSelected) {
        this.isRequisitesTabSelected = isRequisitesTabSelected;
    }

    public String getRouteTabHeader() {
        return "<span><span>Движение документа</span></span>";
    }

    public boolean isRouteTabSelected() {
        return isRouteTabSelected;
    }

    public void setRouteTabSelected(boolean isRouteTabSelected) {
        this.isRouteTabSelected = isRouteTabSelected;
    }

    public String getAccessTabHeader() {
        return "<span><span>Доступ</span></span>";
    }

    public boolean isAccessTabSelected() {
        return isAccessTabSelected;
    }

    public void setAccessTabSelected(boolean isAccessTabSelected) {
        this.isAccessTabSelected = isAccessTabSelected;
    }

    public String getRelationTabHeader() {
        return "<span><span>Связи</span></span>";
    }

    public boolean isRelationTabSelected() {
        return isRelationTabSelected;
    }

    public void setRelationTabSelected(boolean isRelationTabSelected) {
        this.isRelationTabSelected = isRelationTabSelected;
    }

    public String getOriginalTabHeader() {
        return "<span><span>Оригинал</span></span>";
    }

    public void setOriginalTabSelected(boolean isOriginalTabSelected) {
        this.isOriginalTabSelected = isOriginalTabSelected;
    }

    public boolean isOriginalTabSelected() {
        return isOriginalTabSelected;
    }

    public String getHistoryTabHeader() {
        return "<span><span>История</span></span>";
    }

    public void setHistoryTabSelected(boolean isHistoryTabSelected) {
        this.isHistoryTabSelected = isHistoryTabSelected;
    }

    public boolean isHistoryTabSelected() {
        return isHistoryTabSelected;
    }


    private boolean isRequisitesTabSelected = true;
    private boolean isRouteTabSelected = false;
    private boolean isRelationTabSelected = false;
    private boolean isOriginalTabSelected = false;
    private boolean isAccessTabSelected = false;
    private boolean isHistoryTabSelected = false;

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
            getDocument().setRecipientGroups(new HashSet(getGroups()));
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
            this.isUsersDialogSelected = isUsersDialogSelected;
            this.isGroupsDialogSelected = false;
        }
    }

    public boolean isGroupsDialogSelected() {
        return isGroupsDialogSelected;
    }

    public void setGroupsDialogSelected(boolean isGroupsDialogSelected) {
        if (isGroupsDialogSelected) {
            this.isGroupsDialogSelected = isGroupsDialogSelected;
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

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("request_documents")
    private transient RequestDocumentListHolder requestDocumentList;
    @Inject
    @Named("dictionaryManagement")
    private transient DictionaryManagementBean dictionaryManagement;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;
    @Inject
    @Named("contragentList")
    private transient ContragentListHolderBean contragentList;

    private static final long serialVersionUID = 4716264614655470705L;
}