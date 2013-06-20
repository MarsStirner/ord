package ru.efive.dms.uifaces.beans.internal;

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

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.uifaces.beans.roles.RoleListSelectModalBean;
import ru.efive.sql.dao.user.UserAccessLevelDAO;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.user.UserAccessLevel;
import ru.efive.dms.dao.DocumentFormDAOImpl;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.dao.PaperCopyDocumentDAOImpl;
import ru.efive.dms.data.Attachment;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.data.PaperCopyDocument;
import ru.efive.dms.uifaces.beans.*;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserUnitsSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;

@Named("internal_doc")
@ConversationScoped
public class InternalDocumentHolder extends AbstractDocumentHolderBean<InternalDocument, Integer> implements Serializable {

    @Override
    public String save() {
        return super.save();
    }

    @Override
    protected boolean deleteDocument() {
        boolean result = false;
        try {
            result = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).delete(getDocumentId());
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
            setDocument(sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).get(id));
            if (getDocument() == null) {
                setState(STATE_NOT_FOUND);
            } else {
                UserAccessLevel userAccessLevel = sessionManagement.getLoggedUser().getCurrentUserAccessLevel();
                if (userAccessLevel == null) {
                    userAccessLevel = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByLevel(1);
                }

                UserAccessLevel docAccessLevel = getDocument().getUserAccessLevel();
                if (docAccessLevel == null) {
                    docAccessLevel = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByLevel(1);
                    getDocument().setUserAccessLevel(docAccessLevel);
                }

                if (docAccessLevel.getLevel() > userAccessLevel.getLevel()) {
                    /*FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                                 FacesMessage.SEVERITY_ERROR,
                                 "Уровень допуска к документу выше вашего уровня допуска.", ""));*/

                    setState(STATE_FORBIDDEN);
                    setStateComment("Уровень допуска к документу выше вашего уровня допуска.");
                    return;
                }

                Set<Integer> allReadersId = new HashSet<Integer>();

                User currentUser = sessionManagement.getLoggedUser();
                currentUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(currentUser.getLogin(), currentUser.getPassword());
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

                    InternalDocument document = getDocument();

                    HibernateTemplate hibernateTemplate = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).getHibernateTemplate();
                    Session session = hibernateTemplate.getSessionFactory().openSession();
                    session.beginTransaction();
                    session.update(document);
                    session.getTransaction().commit();

                    hibernateTemplate.initialize(document.getPersonReaders());
                    hibernateTemplate.initialize(document.getPersonEditors());
                    hibernateTemplate.initialize(document.getHistory());
                    hibernateTemplate.initialize(document.getSigner());
                    hibernateTemplate.initialize(document.getRoleEditors());
                    hibernateTemplate.initialize(document.getRoleReaders());
                    hibernateTemplate.initialize(document.getOfficeKeepingVolume());

                    hibernateTemplate.initialize(document.getRecipientGroups());
                    hibernateTemplate.initialize(document.getRecipientUsers());

                    session.close();


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
                        if (document.getInitiator() != null) {
                            allReadersId.add(document.getInitiator().getId());
                        }
                        if (document.getSigner() != null) {
                            allReadersId.add(document.getSigner().getId());
                        }

                        List<User> someReaders = new ArrayList<User>();
                        someReaders.addAll(document.getRecipientUsersList());
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
                            /*FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                                           FacesMessage.SEVERITY_ERROR,
                                           "Уровень допуска к документу выше вашего уровня допуска.", ""));*/

                            setState(STATE_FORBIDDEN);
                            setStateComment("");
                            return;
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
        InternalDocument doc = new InternalDocument();
        doc.setDocumentStatus(DocumentStatus.NEW);
        doc.setInitiator(sessionManagement.getLoggedUser());
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        doc.setCreationDate(created);

        String isDocumentTemplate = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("isDocumentTemplate");
        if (isDocumentTemplate != null && StringUtils.isNotEmpty(isDocumentTemplate) && isDocumentTemplate.toLowerCase().equals("yes")) {
            doc.setTemplateFlag(true);
        } else {
            doc.setTemplateFlag(false);
        }

        DocumentForm form = null;
        List<DocumentForm> list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategoryAndValue("Внутренние документы", "Служебная записка");
        if (list.size() > 0) {
            form = list.get(0);

        } else {
            list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategory("Внутренние документы");
            if (list.size() > 0) {
                form = list.get(0);
            }
        }
        if (form != null) {
            doc.setForm(form);
        }
        UserAccessLevel accessLevel = sessionManagement.getLoggedUser().getCurrentUserAccessLevel();
        if (accessLevel == null) {
            accessLevel = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByLevel(1);
        }
        doc.setUserAccessLevel(accessLevel);

        doc.setRoleReaders(new ArrayList<Role>());
        doc.setRoleEditors(new ArrayList<Role>());
        doc.setRecipientUsers(new ArrayList<User>());

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
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            InternalDocument document = (InternalDocument) getDocument();
            document = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).save(document);

            //document = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).save(document);
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
        try {
            InternalDocument document = (InternalDocument) getDocument();
            document = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
            } else {
                Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
                document.setCreationDate(created);
                document.setInitiator(sessionManagement.getLoggedUser());

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
                        tmpAttachment.setParentId(new String(("internal_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                result = true;
                if (result) {
                    setDocument(document);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка при сохранении нового документа.", ""));
        }
        return result;
    }

    @Override
    protected String doAfterCreate() {
        InternalDocumentList.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterEdit() {
        InternalDocumentList.markNeedRefresh();
        return super.doAfterEdit();
    }

    @Override
    protected String doAfterDelete() {
        InternalDocumentList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        InternalDocumentList.markNeedRefresh();
        UserAccessLevel userAccessLevel = sessionManagement.getLoggedUser().getCurrentUserAccessLevel();
        if (userAccessLevel == null) {
            userAccessLevel = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByLevel(1);
        }

        UserAccessLevel docAccessLevel = getDocument().getUserAccessLevel();
        if (docAccessLevel == null) {
            docAccessLevel = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByLevel(1);
            getDocument().setUserAccessLevel(docAccessLevel);
        }
        if (docAccessLevel.getLevel() > userAccessLevel.getLevel()) {
            setState(STATE_FORBIDDEN);
        }

        return super.doAfterSave();
    }

    protected boolean isCurrentUserDocEditor() {
        User in_user = sessionManagement.getLoggedUser();
        in_user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(in_user.getLogin(), in_user.getPassword());
        InternalDocument in_doc = getDocument();

        if (in_user.isAdministrator()) {
            return true;
        }
        List<User> in_editors = new ArrayList<User>();
        List<Integer> in_editorsId = new ArrayList<Integer>();
        if (in_doc.getPersonEditors() != null) {
            for (User user : in_doc.getPersonEditors()) {
                in_editorsId.add(user.getId());
            }
        }
        if (in_doc.getController() != null) {
            in_editorsId.add(in_doc.getController().getId());
        }
        if (in_doc.getInitiator() != null) {
            in_editorsId.add(in_doc.getInitiator().getId());
        }

        if (in_doc.getSigner() != null) {
            in_editorsId.add(in_doc.getSigner().getId());
        }
        if (in_editorsId.contains(in_user.getId())) {
            return true;
        }

        List<Integer> in_rolesId = new ArrayList<Integer>();
        for (Role role : in_doc.getRoleEditors()) {
            in_rolesId.add(role.getId());
        }

        if (in_rolesId.size() != 0) {
            for (Role in_role : in_user.getRoleList()) {
                if (in_rolesId.contains(in_role.getId())) {
                    return true;
                }
            }
        }

        return false;
    }

    protected boolean isCurrentUserAdvDocReader() {
        User in_user = sessionManagement.getLoggedUser();
        in_user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(in_user.getLogin(), in_user.getPassword());
        ;
        InternalDocument internal_doc = getDocument();

        List<User> in_advReaders = new ArrayList<User>();
        if (internal_doc.getPersonReaders() != null) in_advReaders.addAll(internal_doc.getPersonReaders());
        if (in_advReaders.contains(in_user)) {
            return true;
        }

        List<Role> in_roles = internal_doc.getRoleReaders();
        if (in_roles != null) {
            for (Role in_role : in_user.getRoleList()) {
                if (in_roles.contains(in_role)) {
                    return true;
                }
            }
        }

        return false;
    }

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
                    attachment.setParentId(new String(("internal_" + getDocumentId()).getBytes(), "utf-8"));
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
            attachments = fileManagement.getFilesByParentId("internal_" + getDocumentId());
            if (attachments == null) attachments = new ArrayList<Attachment>();
        }
    }

    public void deleteAttachment(Attachment attachment) {
        InternalDocument document = getDocument();
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(sessionManagement.getLoggedUser());
        historyEntry.setDocType(document.getDocumentType().getName());
        historyEntry.setParentId(document.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("Файл " + attachment.getFileName() + " был удален");
        Set<HistoryEntry> history = document.getHistory();
        history.add(historyEntry);
        document.setHistory(history);

        setDocument(document);

        if (attachment != null) {
            if (fileManagement.deleteFile(attachment)) {
                updateAttachments();
            }
        }

        document = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).save(document);
    }

    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();

    // END OF FILES

    public boolean isAgreementTreeViewAvailable() {
        boolean result = true;
        try {
            int loggedUserId = sessionManagement.getLoggedUser().getId();
            if (getDocument().getAgreementTree() != null && (isViewState() || getDocument().getDocumentStatus().getId() != 1)) {
                if (loggedUserId == getDocument().getInitiator().getId() || loggedUserId == getDocument().getSigner().getId()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isAgreementTreeEditAvailable() {
        return getDocument().getDocumentStatus().getId() == 1 && (isEditState() || isCreateState()) && (getDocument().getInitiator().getId() == sessionManagement.getLoggedUser().getId());
    }

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

    public String getAgreementTabHeader() {
        return "<span><span>Согласование</span></span>";
    }

    public void setAgreementTabSelected(boolean agreementTabSelected) {
        this.agreementTabSelected = agreementTabSelected;
    }

    public boolean isAgreementTabSelected() {
        return agreementTabSelected;
    }


    private boolean isRequisitesTabSelected = true;
    private boolean isRouteTabSelected = false;
    private boolean isRelationTabSelected = false;
    private boolean isOriginalTabSelected = false;
    private boolean isAccessTabSelected = false;
    private boolean isHistoryTabSelected = false;
    private boolean agreementTabSelected = false;

    private boolean isUsersDialogSelected = true;
    private boolean isGroupsDialogSelected = false;

    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();


    public UserSelectModalBean getControllerSelectModal() {
        return controllerSelectModal;
    }

    public UserSelectModalBean getSignerSelectModal() {
        return signerSelectModal;
    }

    public UserSelectModalBean getResponsibleSelectModal() {
        return responsibleSelectModal;
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
            super.doSave();
            getDocument().setController(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserSelectModalBean responsibleSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setResponsible(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            setUser(null);
        }
    };

    private UserSelectModalBean signerSelectModal = new UserSelectModalBean() {
        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setSigner(getUser());
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
            super.doSave();
            getDocument().setRecipientUsers(getUsers());
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
            if (getDocument() != null && getDocument().getRecipientUsersList() != null) {
                setUsers(getDocument().getRecipientUsersList());
            }
        }
    };

    private GroupsSelectModalBean recipientGroupsSelectModal = new GroupsSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setRecipientGroups(new HashSet(getGroups()));
        }

        @Override
        protected void doHide() {
            super.doHide();
            setGroups(null);
        }

        @Override
        protected void doShow() {
            super.doShow();
            if (getDocument() != null && getDocument().getRecipientGroups() != null) {
                setGroups(getDocument().getRecipientGroupsList());
            }
        }
    };

    private UserUnitsSelectModalBean recipientUnitsSelectModal = new UserUnitsSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setRecipientGroups(new HashSet(getGroups()));
            getDocument().setRecipientUsers(getUsers());
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
                setGroups(getDocument().getRecipientGroupsList());
            }
            if (getDocument() != null && getDocument().getRecipientUsers() != null) {
                setUsers(new ArrayList<User>(getDocument().getRecipientUsers()));
            }
        }
    };


    private UserListSelectModalBean personReadersPickList = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setPersonReaders(getUsers());
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
                setUsers(getDocument().getPersonReaders());
            }
        }
    };

    private UserListSelectModalBean personEditorsPickList = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setPersonEditors(getUsers());
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
                setUsers(getDocument().getPersonEditors());
            }
        }
    };

    private RoleListSelectModalBean roleReadersPickList = new RoleListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setRoleReaders(getRoles());
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
                setRoles(getDocument().getRoleReaders());
            }
        }
    };

    private RoleListSelectModalBean roleEditorsPickList = new RoleListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setRoleEditors(getRoles());
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
                setRoles(getDocument().getRoleEditors());
            }
        }
    };

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

    public GroupsSelectModalBean getRecipientGroupsSelectModal() {
        return recipientGroupsSelectModal;
    }

    public void setRecipientsUnitsSelectModal(UserUnitsSelectModalBean recipientUnitsSelectModal) {
        this.recipientUnitsSelectModal = recipientUnitsSelectModal;
    }

    public UserUnitsSelectModalBean getRecipientUnitsSelectModal() {
        return recipientUnitsSelectModal;
    }

    public void setStateComment(String stateComment) {
        this.stateComment = stateComment;
    }

    public String getStateComment() {
        return stateComment;
    }

    private ProcessorModalBean processorModal = new ProcessorModalBean() {

        private static final long serialVersionUID = 8492303192143994286L;

        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            InternalDocument doc = getDocument();
            if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
        }

        @Override
        protected void doSave() {
            InternalDocument document = getDocument();
            Set<HistoryEntry> history = document.getHistory();

            //history.add(getHistoryEntry());

            super.doSave();

            document.setHistory(history);
            setDocument(document);
            InternalDocumentHolder.this.save();
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            InternalDocument document = (InternalDocument) actionResult.getProcessedData();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<HistoryEntry>();
                }
                history.add(getHistoryEntry());
                document.setHistory(history);
            }
            setDocument(document);
            InternalDocumentHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            InternalDocument document = (InternalDocument) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();
            if (StringUtils.isNotEmpty(in_result)) {
                setActionResult(in_result);
            }
        }
    };

    private transient String stateComment;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("internal_documents")
    private transient InternalDocumentsListHolder InternalDocumentList;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;


}