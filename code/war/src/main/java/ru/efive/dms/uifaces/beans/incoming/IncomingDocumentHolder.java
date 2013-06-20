package ru.efive.dms.uifaces.beans.incoming;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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

import ru.efive.crm.data.Contragent;
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
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.PaperCopyDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.data.Attachment;
import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.data.PaperCopyDocument;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.data.Task;
import ru.efive.dms.uifaces.beans.*;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.officekeeping.OfficeKeepingVolumeSelectModal;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserUnitsSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;

@Named("in_doc")
@ConversationScoped
public class IncomingDocumentHolder extends AbstractDocumentHolderBean<IncomingDocument, Integer> implements Serializable {
    private static final long serialVersionUID = 4716264614655470705L;
    private boolean isUsersDialogSelected = true;
    private boolean isGroupsDialogSelected = false;

    private transient String stateComment;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("in_documents")
    private transient IncomingDocumentListHolder incomingDocumentList;
    @Inject
    @Named("dictionaryManagement")
    private transient DictionaryManagementBean dictionaryManagement;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;
    @Inject
    @Named("contragentList")
    private transient ContragentListHolderBean contragentList;

    @Override
    public String delete() {
        String in_result = super.delete();
        if (in_result != null && in_result.equals("delete")) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../delete_document.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Невозможно удалить документ", ""));
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
            result = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).delete(getDocumentId());
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
            setDocument(sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).get(id));
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

                IncomingDocument document = getDocument();

                HibernateTemplate hibernateTemplate = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).getHibernateTemplate();
                Session session = hibernateTemplate.getSessionFactory().openSession();
                session.beginTransaction();
                session.update(document);
                session.getTransaction().commit();

                hibernateTemplate.initialize(document.getAuthor());
                hibernateTemplate.initialize(document.getNomenclature());
                hibernateTemplate.initialize(document.getPersonReaders());
                hibernateTemplate.initialize(document.getPersonEditors());
                hibernateTemplate.initialize(document.getHistory());
                hibernateTemplate.initialize(document.getRoleEditors());
                hibernateTemplate.initialize(document.getRoleReaders());
                hibernateTemplate.initialize(document.getOfficeKeepingVolume());
                hibernateTemplate.initialize(document.getCollector());
                hibernateTemplate.initialize(document.getDeliveryType());
                hibernateTemplate.initialize(document.getExecutors());
                hibernateTemplate.initialize(document.getRecipientGroups());
                hibernateTemplate.initialize(document.getRecipientUsers());

                session.close();

                Set<Integer> allReadersId = new HashSet<Integer>();

                User currentUser = sessionManagement.getLoggedUser();
                currentUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(currentUser.getLogin(), currentUser.getPassword());
                int userId = currentUser.getId();
                if (userId > 0) {

                    // check for administration arm
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
                        if (document.getController() != null) {
                            allReadersId.add(document.getController().getId());
                        }

                        List<User> someReaders = new ArrayList<User>();
                        someReaders.addAll(document.getRecipientUsers());
                        someReaders.addAll(document.getPersonReaders());
                        someReaders.addAll(document.getPersonEditors());
                        someReaders.addAll(document.getExecutors());
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
                            //FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                            //FacesMessage.SEVERITY_ERROR,
                            //"Уровень допуска к документу выше вашего уровня допуска.", ""));

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
        IncomingDocument doc = new IncomingDocument();
        doc.setDocumentStatus(DocumentStatus.NEW);
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        doc.setDeliveryDate(created);
        doc.setCreationDate(created);
        doc.setAuthor(sessionManagement.getLoggedUser());

        String parentNumeratorId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
        if ((parentNumeratorId != null) && (StringUtils.isNotEmpty(parentNumeratorId))) {
            //doc.setTemplateFlag(true);
            //String parentNumeratorId=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
            doc.setParentNumeratorId(parentNumeratorId);
            //Numerator parentNumerator=sessionManagement.getDAO(NumeratorDAOImpl.class, ApplicationHelper.NUMERATOR_DAO).findDocumentById(parentNumeratorId);
        } else {
            //doc.setTemplateFlag(false);
        }

        DocumentForm form = null;
        List<DocumentForm> list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategoryAndValue("Входящие документы", "Письмо");
        if (list.size() > 0) {
            form = list.get(0);

        } else {
            list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategory("Входящие документы");
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
            IncomingDocument document = (IncomingDocument) getDocument();
            document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Документ не может быть сохранен. Попробуйте повторить позже.", ""));
            } else {
                /*UserAccessLevel userAccessLevel=sessionManagement.getLoggedUser().getCurrentUserAccessLevel();
                    if(userAccessLevel==null){
                        userAccessLevel=sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByLevel(1);
                    }

                    UserAccessLevel docAccessLevel=getDocument().getUserAccessLevel();
                    if(docAccessLevel==null){
                        docAccessLevel=sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByLevel(1);
                        getDocument().setUserAccessLevel(docAccessLevel);
                    }*/
                /*if(docAccessLevel.getLevel()<userAccessLevel.getLevel()){
                        getDocument().setUserAccessLevel(userAccessLevel);
                        result=true;
                    }else if(docAccessLevel.getLevel()>userAccessLevel.getLevel()){
                        result=false;
                        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                                FacesMessage.SEVERITY_ERROR,
                                "Уровень допуска к документу выше вашего уровня допуска. Вы не имеете права сохранять изменения в данном документе.", ""));
                    }else{*/
                setDocument(document);
                result = true;
                //}

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
        //if (validateHolder()) {
        try {
            IncomingDocument document = (IncomingDocument) getDocument();
            document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).save(document);
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
                        tmpAttachment.setParentId(new String(("incoming_" + getDocumentId()).getBytes(), "utf-8"));
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
        incomingDocumentList.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterEdit() {
        incomingDocumentList.markNeedRefresh();
        return super.doAfterEdit();
    }

    @Override
    protected String doAfterDelete() {
        incomingDocumentList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        incomingDocumentList.markNeedRefresh();
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

    protected boolean validateHolder() {
        boolean result = true;
        FacesContext context = FacesContext.getCurrentInstance();
        if (getDocument().getController() == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Руководителя", ""));
            result = false;
        }
        if (getDocument().getContragent() == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Корреспондента", ""));
            result = false;
        }
        if (getDocument().getRecipientUsers() == null || getDocument().getRecipientUsers().size() == 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Адресатов", ""));
            result = false;
        }
        if (getDocument().getShortDescription() == null || getDocument().getShortDescription().equals("")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо заполнить Краткое содержание", ""));
            result = false;
        }

        return result;
    }

    protected boolean isCurrentUserDocEditor() {
        User in_user = sessionManagement.getLoggedUser();
        in_user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(in_user.getLogin(), in_user.getPassword());
        ;
        IncomingDocument in_doc = getDocument();

        if (in_user.isAdministrator()) {
            return true;
        }

        List<Integer> in_editorsId = new ArrayList<Integer>();
        if (in_doc.getPersonEditors() != null) {
            for (User user : in_doc.getPersonEditors()) {
                in_editorsId.add(user.getId());
            }
        }
        if (in_doc.getController() != null) {
            in_editorsId.add(in_doc.getController().getId());
        }
        if (in_doc.getAuthor() != null) {
            in_editorsId.add(in_doc.getAuthor().getId());
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
        IncomingDocument in_doc = getDocument();

        List<User> in_advReaders = new ArrayList<User>();
        if (in_doc.getPersonReaders() != null) in_advReaders.addAll(in_doc.getPersonReaders());
        if (in_advReaders.contains(in_user)) {
            return true;
        }

        List<Role> in_roles = in_doc.getRoleReaders();
        if (in_roles != null) {
            for (Role in_role : in_user.getRoleList()) {
                if (in_roles.contains(in_role)) {
                    return true;
                }
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
                    attachment.setParentId(new String(("incoming_" + getDocumentId()).getBytes(), "utf-8"));
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
            attachments = fileManagement.getFilesByParentId("incoming_" + getDocumentId());
            if (attachments == null) attachments = new ArrayList<Attachment>();
        }
    }

    public void deleteAttachment(Attachment attachment) {
        IncomingDocument document = getDocument();
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

        document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).save(document);
        //this.edit();
    }

    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();

    protected List<String> getRelatedDocumentsUniqueId() {
        List<String> ids = new ArrayList<String>();
        String key = "incoming_" + getDocument().getId();
        if (!key.isEmpty()) {
            List<OutgoingDocument> out_docs = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findAllDocumentsByReasonDocumentId(key);
            for (OutgoingDocument out_doc : out_docs) {
                ids.add(out_doc.getUniqueId());
            }
        }

        return ids;
    }

    protected String getLinkDescriptionByUniqueId(String key) {
        if (!key.isEmpty()) {
            int pos = key.indexOf('_');
            if (pos != -1) {
                String id = key.substring(pos + 1, key.length());
                //String in_type=key.substring(0,pos);
                StringBuffer in_description = new StringBuffer("");
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                if (key.indexOf("incoming") != -1) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber().equals("") ?
                            "Черновик входщяего документа от " + sdf.format(in_doc.getCreationDate()) :
                            "Входящий документ № " + in_doc.getRegistrationNumber() + " от " + sdf.format(in_doc.getRegistrationDate())
                    );

                } else if (key.indexOf("outgoing") != -1) {
                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber().equals("") ?
                            "Черновик исходящего документа от " + sdf.format(out_doc.getCreationDate()) :
                            "Исходящий документ № " + out_doc.getRegistrationNumber() + " от " + sdf.format(out_doc.getRegistrationDate())
                    );

                } else if (key.indexOf("internal") != -1) {
                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber().equals("") ?
                            "Черновик внутреннего документа от " + sdf.format(internal_doc.getCreationDate()) :
                            "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + sdf.format(internal_doc.getRegistrationDate())
                    );

                } else if (key.indexOf("request") != -1) {
                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber().equals("") ?
                            "Черновик обращения граждан от " + sdf.format(request_doc.getCreationDate()) :
                            "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + sdf.format(request_doc.getRegistrationDate())
                    );

                } else if (key.indexOf("task") != -1) {
                    Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findDocumentById(id);
                    in_description = new StringBuffer(task_doc.getTaskNumber() == null || task_doc.getTaskNumber().equals("") ?
                            "Черновик поручения от " + sdf.format(task_doc.getCreationDate()) :
                            "Поручение № " + task_doc.getTaskNumber() + " от " + sdf.format(task_doc.getCreationDate())
                    );
                }
                return in_description.toString();
            }
        }
        return "";
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

    public String getRelationTabHeader() {
        return "<span><span>Связи</span></span>";
    }

    public boolean isRelationTabSelected() {
        return isRelationTabSelected;
    }

    public void setRelationTabSelected(boolean isRelationTabSelected) {
        this.isRelationTabSelected = isRelationTabSelected;
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

    private ContragentSelectModal contragentSelectModal = new ContragentSelectModal();
    private DeliveryTypeSelectModal deliveryTypeSelectModal = new DeliveryTypeSelectModal();
    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

    /*public UserSelectModalBean getExecutorSelectModal() {
         return executorSelectModal;
     }*/

    public UserListSelectModalBean getExecutorsSelectModal() {
        return executorsSelectModal;
    }

    public UserSelectModalBean getControllerSelectModal() {
        return controllerSelectModal;
    }

    public UserSelectModalBean getControllerSelectModalByGroupName(String groupName) {
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

    public OfficeKeepingVolumeSelectModal getOfficeKeepingVolumeSelectModal() {
        return officeKeepingVolumeSelectModal;
    }

    public UserSelectModalBean getCollectorSelectModal() {
        return collectorSelectModal;
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

    private UserListSelectModalBean executorsSelectModal = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setExecutors(getUsers());
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
            if (getDocument() != null && getDocument().getExecutors() != null) {
                setUsers(getDocument().getExecutors());
            }
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
            if (getDocument() != null && getDocument().getRecipientUsers() != null) {
                setUsers(getDocument().getRecipientUsers());
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
                setUsers(getDocument().getRecipientUsers());
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

    public void setRecipientUnitsSelectModal(UserUnitsSelectModalBean recipientUnitsSelectModal) {
        this.recipientUnitsSelectModal = recipientUnitsSelectModal;
    }

    public UserUnitsSelectModalBean getRecipientUnitsSelectModal() {
        return recipientUnitsSelectModal;
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

    public void setStateComment(String stateComment) {
        this.stateComment = stateComment;
    }

    public String getStateComment() {
        return stateComment;
    }

    private ProcessorModalBean processorModal = new ProcessorModalBean() {

        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            IncomingDocument document = (IncomingDocument) actionResult.getProcessedData();
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
            IncomingDocumentHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            IncomingDocument document = (IncomingDocument) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();

            if (StringUtils.isNotEmpty(in_result)) {
                setActionResult(in_result);
            }
        }
    };
}