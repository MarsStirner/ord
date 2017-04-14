package ru.efive.dms.uifaces.beans.internal;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.*;
import ru.efive.dms.uifaces.beans.task.DocumentTaskTreeHolder;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.*;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.MSG_ERROR_ON_DELETE;
import static ru.efive.dms.util.security.Permissions.Permission.*;

@ViewScopedController(value = "internal_doc", transactionManager = "ordTransactionManager")
public class InternalDocumentHolder extends AbstractDocumentHolderBean<InternalDocument> {

    //TODO ACL
    private Permissions permissions;

    @Autowired
    @Qualifier("fileManagement")
    private FileManagementBean fileManagement;
    @Autowired
    @Qualifier("documentTaskTree")
    private DocumentTaskTreeHolder taskTreeHolder;
    @Autowired
    @Qualifier("internalDocumentDao")
    private InternalDocumentDao internalDocumentDao;
    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;
    @Autowired
    @Qualifier("incomingDocumentDao")
    private IncomingDocumentDao incomingDocumentDao;
    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;
    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;
    //Для проверки прав доступа
    @Autowired
    @Qualifier("permissionChecker")
    private PermissionChecker permissionChecker;
    private List<Attachment> attachments = new ArrayList<>();
    private ProcessorModalBean processorModal = new ProcessorModalBean() {

        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) {
                saveNewDocument();
            }
        }

        @Override
        protected void doSave() {
            InternalDocument document = getDocument();
            Set<HistoryEntry> history = document.getHistory();
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
                    history = new HashSet<>();
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
    

    public void handleFileUpload(FileUploadEvent event) {
        final UploadedFile file = event.getFile();
        if (file != null) {
            log.info("Upload new file[{}] content-type={} size={}", file.getFileName(), file.getContentType(), file.getSize());
            final Attachment attachment = new Attachment();
            attachment.setFileName(file.getFileName());
            attachment.setCreated(LocalDateTime.now());
            attachment.setAuthorId(authData.getAuthorized().getId());
            attachment.setParentId(getDocument().getUniqueId());
            final boolean result = fileManagement.createFile(attachment, file.getContents());
            log.info("After alfresco call Attachment.id={}", attachment.getId());
            if (result) {
                attachments.add(attachment);
            }
            addMessage(new FacesMessage("Successful! " + file.getFileName() + " is uploaded. Size " + file.getSize()));
        } else {
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addVersionForAttachment(final Attachment attachment) {
        if (attachment != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(AttachmentVersionDialogHolder.DIALOG_SESSION_KEY, attachment);
            final Map<String, List<String>> params = new HashMap<>(1);
            params.put(
                    AttachmentVersionDialogHolder.DIALOG_DOCUMENT_KEY,
                    Collections.singletonList(getDocument().getUniqueId())
            );
            RequestContext.getCurrentInstance().openDialog("/dialogs/addVersionForAttachment.xhtml", AbstractDialog.getViewOptions(), params);
        } else {
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
        }
    }

    public void handleAddVersionDialogResult(final SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Add version dialog: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final FacesMessage msg = (FacesMessage) result.getResult();
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, msg);
        }
    }


    //Выбора руководителя ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
        params.put(UserDialogHolder.DIALOG_GROUP_KEY, Collections.singletonList(Group.RB_CODE_MANAGERS));
        final User preselected = getDocument().getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onControllerChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose controller: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setController(selected);
        }
    }

    //Выбора исполнителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseResponsible() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_RESPONSIBLE));
        final User preselected = getDocument().getResponsible();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onResponsibleChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose responsible: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setResponsible(selected);
        }
    }

    // Выбора адресатов-пользователей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRecipients() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_RECIPIENTS));
        final List<User> preselected = getDocument().getRecipientUserList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onRecipientsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose recipients: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                getDocument().setRecipientUsers(new HashSet<>(selected));
            } else {
                getDocument().getRecipientUsers().clear();
            }
        }
    }

    // Выбора адресатов-групп /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRecipientGroups() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleGroupDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleGroupDialogHolder.DIALOG_TITLE_VALUE_RECIPIENTS));
        final Set<Group> preselected = getDocument().getRecipientGroups();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleGroupDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleGroupDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onRecipientGroupsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose recipientGroups: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Set<Group> selected = (Set<Group>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                getDocument().setRecipientGroups(selected);
            } else {
                getDocument().getRecipientGroups().clear();
            }
        }
    }

    // Выбора пользователей-читателей /////////////////////////////////////////////////////////////////////////////////////////////
    public void choosePersonReaders() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_PERSON_READERS));
        final List<User> preselected = getDocument().getPersonReadersList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onPersonReadersChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose personReaders: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                getDocument().setPersonReaders(new HashSet<>(selected));
            } else {
                getDocument().getPersonReaders().clear();
            }
        }
    }

    // Выбора пользователей-редакторов /////////////////////////////////////////////////////////////////////////////////////////////
    public void choosePersonEditors() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_PERSON_EDITORS));
        final List<User> preselected = getDocument().getPersonEditorsList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onPersonEditorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose personEditors: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                getDocument().setPersonEditors(new HashSet<>(selected));
            } else {
                getDocument().getPersonEditors().clear();
            }
        }
    }

    // Выбора ролей-читателей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRoleReaders() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleRoleDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleRoleDialogHolder.DIALOG_TITLE_VALUE_READERS));
        final Set<Role> preselected = getDocument().getRoleReaders();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleRoleDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleRoleDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onRoleReadersChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose roleReaders: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Set<Role> selected = (Set<Role>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                getDocument().setRoleReaders(selected);
            } else {
                getDocument().getRoleReaders().clear();
            }
        }
    }

    // Выбора ролей-редакторов /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRoleEditors() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleRoleDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleRoleDialogHolder.DIALOG_TITLE_VALUE_EDITORS));
        final Set<Role> preselected = getDocument().getRoleEditors();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleRoleDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleRoleDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onRoleEditorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose roleEditors: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Set<Role> selected = (Set<Role>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                getDocument().setRoleEditors(selected);
            } else {
                getDocument().getRoleEditors().clear();
            }
        }
    }

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
    protected boolean deleteDocument() {
        try {
            final boolean result = internalDocumentDao.delete(getDocument());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_DELETE);
            }
            return result;
        } catch (Exception e) {
            log.error("INTERNAL ERROR ON DELETE_DOCUMENT:", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_DELETE);
            return false;
        }
    }

    @Override
    protected void initNewDocument() {
        permissions = Permissions.ALL_PERMISSIONS;
        final LocalDateTime created = LocalDateTime.now();
        final User currentUser = authData.getAuthorized();
        log.info("Start initialize new document by USER[{}]", currentUser.getId());
        final InternalDocument doc = new InternalDocument();
        doc.setDocumentStatus(DocumentStatus.DOC_PROJECT_1);
        doc.setAuthor(currentUser);
        doc.setCreationDate(created);
        doc.setForm(getDefaultForm());
        doc.setUserAccessLevel(authData.getCurrentAccessLevel());
        setDocument(doc);
    }

    @Override
    protected void initDocument(Integer id) {
        final User currentUser = authData.getAuthorized();
        log.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final InternalDocument document = internalDocumentDao.get(id);
            setDocument(document);
            if (!checkState(document, currentUser)) {
                return;
            }
            //Проверка прав на открытие
            permissions = permissionChecker.getPermissions(authData, document);
            if (isReadPermission()) {
                //Простановка факта просмотра записи
                if (viewFactDao.registerViewFact(document, currentUser)) {
                    addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                //Установка идшника для поиска поручений
                taskTreeHolder.setRootDocumentId(getDocument().getUniqueId());
                //Поиск поручений
                taskTreeHolder.refresh();
                try {
                    updateAttachments();
                } catch (Exception e) {
                    log.warn("Exception while check upload files", e);
                    addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
                }
            }
        } catch (Exception e) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_INITIALIZE);
            log.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    @Override
    public boolean saveNewDocument() {
        final User currentUser = authData.getAuthorized();
        final LocalDateTime created = LocalDateTime.now();
        log.info("Save new document by USER[{}]", currentUser.getId());
        // Сохранение дока в БД и создание записи в истории о создании
        final InternalDocument document = getDocument();
        if (document.getCreationDate() == null) {
            log.warn("creationDate not set. Set it to NOW");
            document.setCreationDate(created);
        }
        if (document.getAuthor() == null || !document.getAuthor().equals(currentUser)) {
            log.warn("Author[{}] not set or not equals with currentUser[{}]. Set it to currentUser", document.getAuthor(), currentUser);
            document.setAuthor(currentUser);
        }

        final HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(currentUser);
        historyEntry.setDocType(document.getDocumentType().getName());
        historyEntry.setParentId(document.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        document.addToHistory(historyEntry);

        try {
            internalDocumentDao.save(document);
        } catch (Exception e) {
            log.error("saveNewDocument: on save document", e);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
        //Простановка факта просмотра записи
        try {
            viewFactDao.registerViewFact(document, currentUser);
        } catch (Exception e) {
            log.error("saveNewDocument: on viewFact register", e);
            addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
        }
        //Установка идшника для поиска поручений
        taskTreeHolder.setRootDocumentId(document.getUniqueId());
        return true;
    }

    @Override
    protected boolean saveDocument() {
        final User currentUser = authData.getAuthorized();
        log.info("Save document by USER[{}]", currentUser.getId());
        final InternalDocument document = getDocument();
        try {
            internalDocumentDao.save(document);
            //Установка идшника для поиска поручений
            taskTreeHolder.setRootDocumentId(document.getUniqueId());
            return true;
        } catch (Exception e) {
            log.error("saveDocument ERROR:", e);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    protected boolean doAfterSave() {
        final UserAccessLevel accessLevel = authData.getCurrentAccessLevel();
        if (getDocument().getUserAccessLevel().getLevel() > accessLevel.getLevel()) {
            setState(State.ERROR);
            final FacesMessage msg = MessageHolder.MSG_TRY_TO_VIEW_WITH_LESSER_ACCESS_LEVEL;
            msg.setSummary(
                    String.format(
                            msg.getSummary(), getDocument().getUserAccessLevel().getValue(), accessLevel.getValue()

                    )
            );
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, msg);
            return false;
        }
        return true;
    }

    @Override
    public boolean isCanDelete() {
        if (!permissions.hasPermission(WRITE)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanView() {
        if (permissions == null || !permissions.hasPermission(READ)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
            log.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
            return false;
        }
        return true;
    }

    private DocumentForm getDefaultForm() {
        final DocumentForm form = documentFormDao.getByCode(DocumentForm.RB_CODE_INTERNAL_OFFICE_MEMO);
        if (form != null) {
            return form;
        } else {
            final List<DocumentForm> formList = documentFormDao.findByDocumentTypeCode(DocumentType.RB_CODE_INTERNAL);
            if (!formList.isEmpty()) {
                return formList.get(0);
            } else {
                return null;
            }
        }
    }


    /**
     * Проверяем является ли документ валидным, и есть ли у пользователя к нему уровень допуска
     *
     * @param document документ для проверки
     * @return true - допуск есть
     */
    private boolean checkState(final InternalDocument document, final User user) {
        if (document == null) {
            setState(State.ERROR);
            log.warn("Document NOT FOUND");
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
            return false;
        }
        if (document.isDeleted()) {
            setState(State.ERROR);
            log.warn("Document[{}] IS DELETED", document.getId());
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
            return false;
        }
        final UserAccessLevel docAccessLevel = document.getUserAccessLevel();
        final UserAccessLevel accessLevel = authData.getCurrentAccessLevel();
        if (docAccessLevel.getLevel() > accessLevel.getLevel()) {
            setState(State.ERROR);
            final FacesMessage msg = MessageHolder.MSG_TRY_TO_VIEW_WITH_LESSER_ACCESS_LEVEL;
            msg.setSummary(
                    String.format(
                            msg.getSummary(), docAccessLevel.getValue(), accessLevel.getValue()

                    )
            );
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, msg);
            log.warn(
                    "Document[{}] has higher accessLevel[{}] then user[{}]", document.getId(), docAccessLevel.getValue(), accessLevel.getValue()
            );
            return false;
        }
        return true;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void updateAttachments() {
        log.debug("Start updating attachments");
        if (getDocument() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId(getDocument().getUniqueId());
        }
        log.debug("Finish updating attachments");
    }

    public void deleteAttachment(Attachment attachment) {
        if (attachment != null && isCanEdit()) {
            final InternalDocument document = getDocument();
            final LocalDateTime created = LocalDateTime.now();;
            HistoryEntry historyEntry = new HistoryEntry();
            historyEntry.setCreated(created);
            historyEntry.setStartDate(created);
            historyEntry.setOwner(authData.getAuthorized());
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
            setDocument(internalDocumentDao.save(document));
        }
    }

    public DocumentTaskTreeHolder getTaskTreeHolder() {
        return taskTreeHolder;
    }

    public void setTaskTreeHolder(DocumentTaskTreeHolder taskTreeHolder) {
        this.taskTreeHolder = taskTreeHolder;
    }

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

}