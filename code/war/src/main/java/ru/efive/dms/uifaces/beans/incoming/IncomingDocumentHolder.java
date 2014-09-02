package ru.efive.dms.uifaces.beans.incoming;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateTemplate;
import ru.efive.crm.data.Contragent;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.dao.DocumentFormDAOImpl;
import ru.efive.dms.dao.IncomingDocumentDAOImpl;
import ru.efive.dms.dao.InternalDocumentDAOImpl;
import ru.efive.dms.dao.OutgoingDocumentDAOImpl;
import ru.efive.dms.dao.PaperCopyDocumentDAOImpl;
import ru.efive.dms.dao.RequestDocumentDAOImpl;
import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.data.Attachment;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.HistoryEntry;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.InternalDocument;
import ru.efive.dms.data.OutgoingDocument;
import ru.efive.dms.data.PaperCopyDocument;
import ru.efive.dms.data.RequestDocument;
import ru.efive.dms.data.Task;
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
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.user.UserAccessLevel;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;

@Named("in_doc")
@ConversationScoped
public class IncomingDocumentHolder extends AbstractDocumentHolderBean<IncomingDocument, Integer> implements Serializable {
    private static final long serialVersionUID = 4716264614655470705L;

    //Именованный логгер (INCOMING_DOCUMENT)
    private static final Logger LOGGER = LoggerFactory.getLogger("INCOMING_DOCUMENT");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    //Сообщения
    private static final FacesMessage MSG_CANT_DELETE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Невозможно удалить документ", "");
    private static final FacesMessage MSG_CANT_SAVE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Документ не может быть сохранен. Попробуйте повторить позже.", "");
    private static final FacesMessage MSG_ERROR_ON_DELETE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка при удалении.", "");
    private static final FacesMessage MSG_ERROR_ON_INITIALIZE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка при инициализации.", "");
    private static final FacesMessage MSG_ERROR_ON_SAVE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка при сохранении.", "");
    private static final FacesMessage MSG_ERROR_ON_SAVE_NEW = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка при сохранении нового документа.", "");
    private static final FacesMessage MSG_ERROR_ON_ATTACH = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Внутренняя ошибка при вложении файла.", "");
    private static final FacesMessage MSG_CONTROLLER_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Руководителя", "");
    private static final FacesMessage MSG_CONTRAGENT_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Корреспондента", "");
    private static final FacesMessage MSG_RECIPIENTS_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Адресатов", "");
    private static final FacesMessage MSG_SHORT_DESCRIPTION_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо заполнить Краткое содержание", "");


    private boolean isUsersDialogSelected = true;
    private boolean isGroupsDialogSelected = false;
    //TODO ACL
    private boolean readPermission = false;
    private boolean editPermission = false;
    private boolean executePermission = false;

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
        if (AbstractDocumentHolderBean.ACTION_RESULT_DELETE.equals(in_result)) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../delete_document.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
                LOGGER.error("INTERNAL ERROR ON DELETE:", e);
            }
        }
        return in_result;
    }

    @Override
    protected boolean deleteDocument() {
        try {
            final boolean result = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_DELETE);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("INTERNAL ERROR ON DELETE_DOCUMENT:", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_DELETE);
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
    protected void initDocument(final Integer id) {
        final User currentUser = sessionManagement.getLoggedUser();
        LOGGER.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final IncomingDocument document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).get(id);
            readPermission = false;
            editPermission = false;
            executePermission = false;
            if (!checkState(document, currentUser)) {
                setDocument(document);
                return;
            }
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
            hibernateTemplate.initialize(document.getDeliveryType());
            hibernateTemplate.initialize(document.getExecutors());
            hibernateTemplate.initialize(document.getRecipientGroups());
            hibernateTemplate.initialize(document.getRecipientUsers());
            session.close();
            updateAttachments();
            setDocument(document);
            //Проверка прав на открытие
            if (!checkPermission(currentUser, document)) {
                setState(STATE_FORBIDDEN);
                setStateComment("Доступ запрещен");
                LOGGER.error("USER[{}] ACCESS TO DOCUMENT[{}] FORBIDDEN", currentUser.getId(), document.getId());
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_INITIALIZE);
            LOGGER.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    /**
     * 1) админ
     * 2) автор
     * 3) исполнители
     * 4) список пользователей на редактирование
     * 5) список пользователей на просмотр
     * 6) список ролей на редактирование
     * 7) список ролей на просмотр
     * 8) руководитель
     * 9) пользователи адресаты
     * 10) группы адресаты
     * 11) пользователи, учавствующие в согласованиях <SUMMARY>
     * Просмотр: 1-2-3-4-5-6-7-8-9-10-11
     * Редактирование документа: 1-2-4-6-8
     * Действия: 1-2-3-8-9-10        *
     *
     * @param user
     * @param document
     * @return
     */
    private boolean checkPermission(final User user, final IncomingDocument document) {
        //1) админ
        if (user.isAdministrator()) {
            LOGGER.debug("{}:Permission RWX granted: AdminRole", document.getId());
            readPermission = true;
            editPermission = true;
            executePermission = true;
            return true;
        }
        //2) автор
        if (user.equals(document.getAuthor())) {
            LOGGER.debug("{}:Permission RWX granted: Author", document.getId());
            readPermission = true;
            editPermission = true;
            executePermission = true;
            return true;
        }
        //3) исполнитель
        for (User currentUser : document.getExecutors()) {
            if (user.equals(currentUser)) {
                LOGGER.debug("{}:Permission RX granted: Executor", document.getId());
                readPermission = true;
                executePermission = true;
            }
        }
        //4) список пользователей на редактирование
        for (User currentUser : document.getPersonEditors()) {
            if (user.equals(currentUser)) {
                LOGGER.debug("{}:Permission RW granted: PersonEditor", document.getId());
                readPermission = true;
                editPermission = true;
            }
        }
        //5) список пользователей на просмотр
        for (User currentUser : document.getPersonReaders()) {
            if (user.equals(currentUser)) {
                LOGGER.debug("{}:Permission R granted: PersonReader", document.getId());
                readPermission = true;
            }
        }
        //   6) список ролей на редактирование
        //   7) список ролей на просмотр
        for (Role currentRole : user.getRoles()) {
            if (document.getRoleEditors().contains(currentRole)) {
                LOGGER.debug("{}:Permission  RW granted: RoleEditor [{}] ", document.getId(), currentRole.getName());
                readPermission = true;
                editPermission = true;
            }
            if (!readPermission && document.getRoleReaders().contains(currentRole)) {
                LOGGER.debug("{}:Permission R granted: RoleReader [{}] ", document.getId(), currentRole.getName());
                readPermission = true;
            }
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            LOGGER.debug("{}:Permission RWX granted: Controller", document.getId());
            readPermission = true;
            editPermission = true;
            executePermission = true;
            return true;
        }
        //9) пользователи адресаты
        for (User currentUser : document.getRecipientUsers()) {
            if (user.equals(currentUser)) {
                LOGGER.debug("{}:Permission RX granted: RecipientUser", document.getId());
                readPermission = true;
                executePermission = true;
            }
        }
        //10) группы адресаты
        for (Group currentGroup : user.getGroups()) {
            if (document.getRecipientGroups().contains(currentGroup)) {
                LOGGER.debug("{}:Permission RX granted: RecipientGroup [{}] ", document.getId(), currentGroup.getValue());
                readPermission = true;
                executePermission = true;
            }
        }
        //11) пользователи, учавствующие в согласованиях
        if (sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).isAccessGrantedByAssociation(sessionManagement.getLoggedUser(), "incoming_" + document.getId())) {
            LOGGER.debug("{}:Permission R granted: TASK", document.getId());
            readPermission = true;
        }

        if (!readPermission && !editPermission && !executePermission) {
            LOGGER.error("{}:Permission denied to user[{}]", document.getId(), user.getId());
            return false; //readPermission || editPermission;
        } else {
            return readPermission || editPermission || executePermission;
        }
    }


    /**
     * Проверяем является ли документ валидным, и есть ли у пользователя к нему уровень допуска
     *
     * @param document документ для проверки
     * @return true - допуск есть
     */
    private boolean checkState(final IncomingDocument document, final User user) {
        if (document == null) {
            setState(STATE_NOT_FOUND);
            LOGGER.warn("IncomingDocument NOT FOUND");
            return false;
        }
        if (document.isDeleted()) {
            setState(STATE_DELETED);
            setStateComment("Документ удален");
            LOGGER.warn("IncomingDocument[{}] IS DELETED", document.getId());
            return false;
        }
        final UserAccessLevel docAccessLevel = document.getUserAccessLevel();
        if (docAccessLevel.getLevel() > user.getCurrentUserAccessLevel().getLevel()) {
            setState(STATE_FORBIDDEN);
            setStateComment("Уровень допуска к документу [" + docAccessLevel.getValue() + "] выше вашего уровня допуска.");
            LOGGER.warn("IncomingDocument[{}] has higher accessLevel[{}] then user[{}]",
                    new Object[]{document.getId(), docAccessLevel.getValue(), user.getCurrentUserAccessLevel().getValue()});
            return false;
        }
        return true;
    }

    @Override
    protected void initNewDocument() {
        readPermission = true;
        editPermission = true;
        executePermission = true;
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
        doc.setUserAccessLevel(sessionManagement.getLoggedUser().getCurrentUserAccessLevel());

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

        doc.addToHistory(historyEntry);

        setDocument(doc);
    }

    @Override
    protected boolean saveDocument() {
        try {
            IncomingDocument document = getDocument();
            document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
                return false;
            } else {
                setDocument(document);
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("saveDocument ERROR:", e);
            ;
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        //if (validateHolder()) {
        try {
            IncomingDocument document = getDocument();
            document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
                document.setCreationDate(created);
                document.setAuthor(sessionManagement.getLoggedUser());

                PaperCopyDocument paperCopy = new PaperCopyDocument();
                paperCopy.setDocumentStatus(DocumentStatus.NEW);
                paperCopy.setCreationDate(created);
                paperCopy.setAuthor(sessionManagement.getLoggedUser());

                String parentId = document.getUniqueId();
                if (parentId != null && !parentId.isEmpty()) {
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

                LOGGER.debug("uploading newly created files");
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
            LOGGER.error("saveNewDocument ERROR:", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
        }
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
        UserAccessLevel docAccessLevel = getDocument().getUserAccessLevel();
        if (docAccessLevel.getLevel() > userAccessLevel.getLevel()) {
            setState(STATE_FORBIDDEN);
        }
        return super.doAfterSave();
    }

    protected boolean validateHolder() {
        boolean result = true;
        FacesContext context = FacesContext.getCurrentInstance();
        if (getDocument().getController() == null) {
            context.addMessage(null, MSG_CONTROLLER_NOT_SET);
            result = false;
        }
        if (getDocument().getContragent() == null) {
            context.addMessage(null, MSG_CONTRAGENT_NOT_SET);
            result = false;
        }
        if (getDocument().getRecipientUsers() == null || getDocument().getRecipientUsers().isEmpty()) {
            context.addMessage(null, MSG_RECIPIENTS_NOT_SET);
            result = false;
        }
        if (getDocument().getShortDescription() == null || getDocument().getShortDescription().equals("")) {
            context.addMessage(null, MSG_SHORT_DESCRIPTION_NOT_SET);
            result = false;
        }

        return result;
    }

    public boolean isCurrentUserDocEditor() {
        return editPermission;
    }

    public boolean isCurrentUserDocExecutor() {
        return executePermission;
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
                    final boolean result = fileManagement.createFile(attachment, details.getByteArray());
                    LOGGER.info("result of the upload operation - {}", result);
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_ATTACH);
            LOGGER.error("uploadAttachments ERROR:", e);
            ;
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
                    final boolean result = fileManagement.createVersion(attachment, details.getByteArray(), majorVersion, details.getAttachment().getFileName());
                    LOGGER.info("result of the upload operation - {}", result);
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_ATTACH);
            LOGGER.error("versionAttachment ERROR:", e);
            ;
        }
    }

    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("incoming_" + getDocumentId());
            if (attachments == null) attachments = new ArrayList<Attachment>();
        }
    }

    public void deleteAttachment(Attachment attachment) {
        if (attachment != null) {
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
            document.addToHistory(historyEntry);
            setDocument(document);
            if (fileManagement.deleteFile(attachment)) {
                updateAttachments();
            }
            setDocument(sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).save(document));
        }
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


                if (key.contains("incoming")) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, ApplicationHelper.INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber().equals("") ?
                            "Черновик входщяего документа от " + DATE_FORMAT.format(in_doc.getCreationDate()) :
                            "Входящий документ № " + in_doc.getRegistrationNumber() + " от " + DATE_FORMAT.format(in_doc.getRegistrationDate())
                    );

                } else if (key.contains("outgoing")) {
                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, ApplicationHelper.OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber().equals("") ?
                            "Черновик исходящего документа от " + DATE_FORMAT.format(out_doc.getCreationDate()) :
                            "Исходящий документ № " + out_doc.getRegistrationNumber() + " от " + DATE_FORMAT.format(out_doc.getRegistrationDate())
                    );

                } else if (key.contains("internal")) {
                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, ApplicationHelper.INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber().equals("") ?
                            "Черновик внутреннего документа от " + DATE_FORMAT.format(internal_doc.getCreationDate()) :
                            "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + DATE_FORMAT.format(internal_doc.getRegistrationDate())
                    );

                } else if (key.contains("request")) {
                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, ApplicationHelper.REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber().equals("") ?
                            "Черновик обращения граждан от " + DATE_FORMAT.format(request_doc.getCreationDate()) :
                            "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + DATE_FORMAT.format(request_doc.getRegistrationDate())
                    );

                } else if (key.contains("task")) {
                    Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findDocumentById(id);
                    in_description = new StringBuffer(task_doc.getTaskNumber() == null || task_doc.getTaskNumber().equals("") ?
                            "Черновик поручения от " + DATE_FORMAT.format(task_doc.getCreationDate()) :
                            "Поручение № " + task_doc.getTaskNumber() + " от " + DATE_FORMAT.format(task_doc.getCreationDate())
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

    public boolean isRequisitesTabSelected() {
        return isRequisitesTabSelected;
    }

    public void setRequisitesTabSelected(boolean isRequisitesTabSelected) {
        this.isRequisitesTabSelected = isRequisitesTabSelected;
    }

    public boolean isRouteTabSelected() {
        return isRouteTabSelected;
    }

    public void setRouteTabSelected(boolean isRouteTabSelected) {
        this.isRouteTabSelected = isRouteTabSelected;
    }

    public boolean isRelationTabSelected() {
        return isRelationTabSelected;
    }

    public void setRelationTabSelected(boolean isRelationTabSelected) {
        this.isRelationTabSelected = isRelationTabSelected;
    }

    public boolean isAccessTabSelected() {
        return isAccessTabSelected;
    }

    public void setAccessTabSelected(boolean isAccessTabSelected) {
        this.isAccessTabSelected = isAccessTabSelected;
    }

    public void setOriginalTabSelected(boolean isOriginalTabSelected) {
        this.isOriginalTabSelected = isOriginalTabSelected;
    }

    public boolean isOriginalTabSelected() {
        return isOriginalTabSelected;
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
    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

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


    private UserListSelectModalBean executorsSelectModal = new UserListSelectModalBean() {

        @Override
        protected void doSave() {
            getDocument().setExecutors(getUsers());
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
            if (getDocument() != null && getDocument().getExecutors() != null) {
                ArrayList<User> tmpList = new ArrayList<User>();
                tmpList.addAll(getDocument().getExecutors());
                setUsers(tmpList);
            }
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
                tmpGroupList.addAll(getDocument().getRecipientGroups());
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