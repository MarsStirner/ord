package ru.efive.dms.uifaces.beans.outgoing;

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

import ru.efive.dao.alfresco.Revision;
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
import ru.efive.dms.uifaces.beans.ContragentListSelectModalBean;
import ru.efive.dms.uifaces.beans.DictionaryManagementBean;
import ru.efive.dms.uifaces.beans.DocumentSelectModal;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.incoming.IncomingDocumentSelectModal;
import ru.efive.dms.uifaces.beans.officekeeping.OfficeKeepingVolumeSelectModal;
import ru.efive.dms.uifaces.beans.roles.RoleListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserAccessLevelDAO;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.user.UserAccessLevel;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;

@Named("out_doc")
@ConversationScoped
public class OutgoingDocumentHolder extends AbstractDocumentHolderBean<OutgoingDocument, Integer> implements Serializable {

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
            result = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Невозможно удалить документ. Попробуйте повторить позже.", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
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
            setDocument(sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).get(id));
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

                    OutgoingDocument document = getDocument();

                    HibernateTemplate hibernateTemplate = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).getHibernateTemplate();
                    Session session = hibernateTemplate.getSessionFactory().openSession();
                    session.beginTransaction();
                    session.update(document);
                    session.getTransaction().commit();

                    hibernateTemplate.initialize(document.getExecutor());
                    hibernateTemplate.initialize(document.getSigner());
                    hibernateTemplate.initialize(document.getAuthor());
                    hibernateTemplate.initialize(document.getNomenclature());
                    hibernateTemplate.initialize(document.getCauseIncomingDocument());
                    hibernateTemplate.initialize(document.getPersonReaders());
                    hibernateTemplate.initialize(document.getPersonEditors());
                    hibernateTemplate.initialize(document.getAgreementUsers());
                    hibernateTemplate.initialize(document.getHistory());
                    hibernateTemplate.initialize(document.getRoleEditors());
                    hibernateTemplate.initialize(document.getRoleReaders());


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
                        if (document.getAuthor() != null) {
                            allReadersId.add(document.getAuthor().getId());
                        }
                        if (document.getExecutor() != null) {
                            allReadersId.add(document.getExecutor().getId());
                        }
                        if (document.getSigner() != null) {
                            allReadersId.add(document.getSigner().getId());
                        }

                        List<User> someReaders = new ArrayList<User>();
                        someReaders.addAll(document.getAgreementUsers());
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
                            if(!taskDao.isAccessGrantedByAssociation(sessionManagement.getLoggedUser(), "outgoing_" + document.getId())) {
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
                    "Внутренняя ошибка.", ""));
            e.printStackTrace();
        }
    }

    @Override
    protected void initNewDocument() {
        OutgoingDocument document = new OutgoingDocument();
        document.setDocumentStatus(DocumentStatus.NEW);
        Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        document.setCreationDate(created);
        document.setAuthor(sessionManagement.getLoggedUser());

        String isDocumentTemplate = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("isDocumentTemplate");
        if (isDocumentTemplate != null && isDocumentTemplate.toLowerCase().equals("yes")) {
            document.setTemplateFlag(true);
        } else {
            document.setTemplateFlag(false);
        }

        DocumentForm form = null;
        List<DocumentForm> list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategoryAndValue("Исходящие документы", "Письмо");
        if (list.size() > 0) {
            form = list.get(0);

        } else {
            list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, ApplicationHelper.DOCUMENT_FORM_DAO).findByCategory("Исходящие документы");
            if (list.size() > 0) {
                form = list.get(0);
            }
        }
        if (form != null) {
            document.setForm(form);
        }
        UserAccessLevel accessLevel = sessionManagement.getLoggedUser().getCurrentUserAccessLevel();
        if (accessLevel == null) {
            accessLevel = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByLevel(1);
        }
        document.setUserAccessLevel(accessLevel);

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
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<HistoryEntry>();
        history.add(historyEntry);
        document.setHistory(history);

        setDocument(document);
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            OutgoingDocument document = (OutgoingDocument) getDocument();
            document = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).save(document);
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
                    "Внутренняя ошибка.", ""));
        }
        return result;
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            OutgoingDocument document = (OutgoingDocument) getDocument();
            document = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).save(getDocument());
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
                        tmpAttachment.setParentId(new String(("outgoing_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Внутренняя ошибка.", ""));
        }
        return result;
    }


    @Override
    protected String doAfterCreate() {
        outgoingDocumentList.markNeedRefresh();
        return super.doAfterCreate();
    }

    @Override
    protected String doAfterEdit() {
        outgoingDocumentList.markNeedRefresh();
        return super.doAfterEdit();
    }

    @Override
    protected String doAfterDelete() {
        outgoingDocumentList.markNeedRefresh();
        return super.doAfterDelete();
    }

    @Override
    protected String doAfterSave() {
        outgoingDocumentList.markNeedRefresh();
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

    protected String getLinkDescriptionById(String key) {
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

    protected boolean validateHolder() {
        boolean result = true;
        FacesContext context = FacesContext.getCurrentInstance();
        if (getDocument().getSigner() == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Руководителя", ""));
            result = false;
        }
        if (getDocument().getExecutor() == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Ответственного исполнителя", ""));
            result = false;
        }
        if (getDocument().getRecipientContragents() == null || getDocument().getRecipientContragents().size() == 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Адресата", ""));
            result = false;
        }
        if (getDocument().getShortDescription() == null || getDocument().getShortDescription().equals("")) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо заполнить Краткое содержание", ""));
            result = false;
        }
        return result;
    }
    
    public boolean isCurrentUserAccessEdit() {
        User inUser = sessionManagement.getLoggedUser();
        //inUser = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(  inUser.getLogin(), inUser.getPassword());
        OutgoingDocument outDoc = getDocument();

        List<Integer> recipUsers = new ArrayList<Integer>();
        if (outDoc.getPersonReaders() != null) {
            for (User user : outDoc.getPersonReaders()) {
                recipUsers.add(user.getId());
            }
        }
        if (recipUsers.contains(inUser.getId())) {
            return true;
        }

        List<Integer> accessRoles = new ArrayList<Integer>();
        if (outDoc.getRoleReaders() != null) {
            for (Role role : outDoc.getRoleReaders()) {
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
        OutgoingDocument out_doc = getDocument();

        if (in_user.isAdministrator()) {
            return true;
        }

        List<Integer> in_editorsId = new ArrayList<Integer>();
        if (out_doc.getPersonEditors() != null) {
            for (User user : out_doc.getPersonEditors()) {
                in_editorsId.add(user.getId());
            }
        }
        if (out_doc.getSigner() != null) {
            in_editorsId.add(out_doc.getSigner().getId());
        }
        if (out_doc.getAuthor() != null) {
            in_editorsId.add(out_doc.getAuthor().getId());
        }

        if (in_editorsId.contains(in_user.getId())) {
            return true;
        }


        List<Integer> in_rolesId = new ArrayList<Integer>();
        for (Role role : out_doc.getRoleEditors()) {
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
        //in_user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(in_user.getLogin(), in_user.getPassword());

        OutgoingDocument out_doc = getDocument();

        List<User> in_advReaders = new ArrayList<User>();
        if (out_doc.getPersonReaders() != null) in_advReaders.addAll(out_doc.getPersonReaders());
        if (in_advReaders.contains(in_user)) {
            return true;
        }

        List<Role> in_roles = out_doc.getRoleReaders();
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
                    attachment.setParentId(new String(("outgoing_" + getDocumentId()).getBytes(), "utf-8"));
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
            attachments = fileManagement.getFilesByParentId("outgoing_" + getDocumentId());
            if (attachments == null) attachments = new ArrayList<Attachment>();
        }
    }

    public void deleteAttachment(Attachment attachment) {
        OutgoingDocument document = getDocument();
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

        document = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).save(document);
    }

    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();

    // END OF FILES

    public boolean isAgreementTreeViewAvailable() {
        boolean result = true;
        try {
            int loggedUserId = sessionManagement.getLoggedUser().getId();
            if (getDocument().getAgreementTree() != null && (isViewState() || getDocument().getDocumentStatus().getId() != 1)) {
                if (loggedUserId == getDocument().getAuthor().getId() || loggedUserId == getDocument().getSigner().getId()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean isAgreementTreeEditAvailable() {
        return getDocument().getDocumentStatus().getId() == 1 && (isEditState() ||
                isCreateState()) && (getDocument().getAuthor().getId() == sessionManagement.getLoggedUser().getId());
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

    // END OF MODAL HOLDERS

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

    public String getAgreementTabHeader() {
        return "<span><span>Согласование</span></span>";
    }

    public void setAgreementTabSelected(boolean agreementTabSelected) {
        this.agreementTabSelected = agreementTabSelected;
    }

    public boolean isAgreementTabSelected() {
        return agreementTabSelected;
    }

    public ContragentListSelectModalBean getRecipientContragentsSelectModal() {
        return recipientContragentsSelectModal;
    }

    private ContragentListSelectModalBean recipientContragentsSelectModal = new ContragentListSelectModalBean() {
        @Override
        protected void doSave() {
            getDocument().setRecipientContragents(getContragents());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getContragentList().setFilter("");
            getContragentList().markNeedRefresh();
            setContragents(null);
        }
    };

    public UserSelectModalBean getExecutorSelectModal() {
        return executorSelectModal;
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

    public UserSelectModalBean getSignerSelectModal() {
        return signerSelectModal;
    }

    private UserSelectModalBean signerSelectModal = new UserSelectModalBean() {

        @Override
        protected void doSave() {
            getDocument().setSigner(getUser());
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

    public IncomingDocumentSelectModal getCauseIncomingDocumentSelectModal() {
        return incomingDocumentSelectModal;
    }

    private IncomingDocumentSelectModal incomingDocumentSelectModal = new IncomingDocumentSelectModal() {

        @Override
        protected void doSave() {
            getDocument().setCauseIncomingDocument(getIncomingDocument());
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            getIncomingDocumentList().setFilter("");
            getIncomingDocumentList().markNeedRefresh();
        }
    };

    public DocumentSelectModal getReasonDocumentSelectModal() {
        return reasonDocumentSelectModal;
    }

    private DocumentSelectModal reasonDocumentSelectModal = new DocumentSelectModal() {

        @Override
        protected void doSave() {
            super.doSave();
            String viewType = this.getViewType();
            if (viewType.equals("Входящие документы")) {
                getDocument().setReasonDocumentId("incoming_" + String.valueOf(this.getIncomingDocument().getId()));
            } else if (viewType.equals("Обращения граждан")) {
                getDocument().setReasonDocumentId("request_" + String.valueOf(this.getRequestDocument().getId()));
            }
        }

        @Override
        protected void doHide() {
            super.doHide();
            this.getIncomingDocuments().setFilter("");
            this.getIncomingDocuments().markNeedRefresh();
            this.getRequestDocuments().setFilter("");
            this.getRequestDocuments().markNeedRefresh();
            this.setViewTypesAlreadySelected(false);
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
            OutgoingDocument document = (OutgoingDocument) actionResult.getProcessedData();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<HistoryEntry>();
                }
                history.add(getHistoryEntry());
                document.setHistory(history);
            }
            setDocument(document);
            OutgoingDocumentHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            OutgoingDocument document = (OutgoingDocument) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();
            if (StringUtils.isNotEmpty(in_result)) {
                setActionResult(in_result);
            }
        }
    };

    private boolean isRequisitesTabSelected = true;
    private boolean isRouteTabSelected = false;
    private boolean isRelationTabSelected = false;
    private boolean isOriginalTabSelected = false;
    private boolean isAccessTabSelected = false;
    private boolean isHistoryTabSelected = false;
    private boolean agreementTabSelected = false;

    private DeliveryTypeSelectModal deliveryTypeSelectModal = new DeliveryTypeSelectModal();
    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

    private transient String stateComment;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("dictionaryManagement")
    private transient DictionaryManagementBean dictionaryManagement;
    @Inject
    @Named("out_documents")
    private transient OutgoingDocumentListHolder outgoingDocumentList;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;


    private static final long serialVersionUID = 4716264614655470705L;
}