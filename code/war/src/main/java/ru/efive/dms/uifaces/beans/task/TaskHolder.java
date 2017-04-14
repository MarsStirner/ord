package ru.efive.dms.uifaces.beans.task;

import com.github.javaplugs.jsf.SpringScopeView;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.AttachmentVersionDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.*;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.util.ApplicationHelper;

import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.efive.dms.util.security.Permissions.Permission.*;

@Controller("task")
@SpringScopeView
public class TaskHolder extends AbstractDocumentHolderBean<Task> {

    private static final Logger LOGGER = LoggerFactory.getLogger("TASK");
    //TODO ACL
    private Permissions permissions;
    @Autowired
    @Qualifier("fileManagement")
    private FileManagementBean fileManagement;
    @Autowired
    @Qualifier("documentTaskTree")
    private DocumentTaskTreeHolder taskTreeHolder;
    //Для проверки прав доступа
    @Autowired
    @Qualifier("permissionChecker")
    private PermissionChecker permissionChecker;
    @Autowired
    @Qualifier("taskDao")
    private TaskDao taskDao;
    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;
    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;
    @Autowired
    @Qualifier("incomingDocumentDao")
    private IncomingDocumentDao incomingDocumentDao;
    @Autowired
    @Qualifier("internalDocumentDao")
    private InternalDocumentDao internalDocumentDao;
    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;
    @Autowired
    @Qualifier("requestDocumentDao")
    private RequestDocumentDao requestDocumentDao;
    private List<Attachment> attachments = new ArrayList<>();
    private ProcessorModalBean processorModal = new ProcessorModalBean() {
        @Override
        protected void doSave() {
            Task task = getDocument();
            Set<HistoryEntry> history = task.getHistory();
            super.doSave();
            task.setHistory(history);
            setDocument(task);
            TaskHolder.this.save();
        }

        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) {
                saveNewDocument();
            }
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            Task task = (Task) actionResult.getProcessedData();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = task.getHistory();
                if (history == null) {
                    history = new HashSet<>();
                }
                history.add(getHistoryEntry());
                task.setHistory(history);
            }
            setDocument(task);
            TaskHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            Task document = (Task) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();
            if (StringUtils.isNotEmpty(in_result)) {
                setActionResult(in_result);
            }
        }
    };

    @PreDestroy
    public void destroy() {
        LOGGER.info("Bean destroyed");
    }

    public void handleFileUpload(FileUploadEvent event) {
        final UploadedFile file = event.getFile();
        if (file != null) {
            LOGGER.info("Upload new file[{}] content-type={} size={}", file.getFileName(), file.getContentType(), file.getSize());
            final Attachment attachment = new Attachment();
            attachment.setFileName(file.getFileName());
            attachment.setCreated(LocalDateTime.now());
            attachment.setAuthorId(authData.getAuthorized().getId());
            attachment.setParentId(getDocument().getUniqueId());
            final boolean result = fileManagement.createFile(attachment, file.getContents());
            LOGGER.info("After alfresco call Attachment.id={}", attachment.getId());
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
            final Map<String, List<String>> params = new HashMap<>();
            params.put(AttachmentVersionDialogHolder.DIALOG_DOCUMENT_KEY, Stream.of(getDocument().getUniqueId()).collect(Collectors.toList()));
            RequestContext.getCurrentInstance().openDialog("/dialogs/addVersionForAttachment.xhtml", AbstractDialog.getViewOptions(), params);
        } else {
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
        }
    }

    public void handleAddVersionDialogResult(final SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Add version dialog: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final FacesMessage msg = (FacesMessage) result.getResult();
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, msg);
        }
    }

    // Выбора исполнителей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseExecutors() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_EXECUTORS));
        final List<User> preselected = getDocument().getExecutorsList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onExecutorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose executors: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final List<User> selected = (List<User>) result.getResult();
            if (selected != null && !selected.isEmpty()) {
                getDocument().setExecutors(new HashSet<>(selected));
            } else {
                getDocument().getExecutors().clear();
            }
        }
    }

    //Выбора руководителя ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_RESPONSIBLE));
        final User preselected = getDocument().getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onControllerChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose controller: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setController(selected);
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
        final Task document = getDocument();
        document.setDeleted(true);
        try {
            taskDao.update(document);
            if (!document.isDeleted()) {
                addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
                LOGGER.error("After delete operation task hasn't deleted = true");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Error on delete: ", e);
            addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
        }
        return false;
    }


    // FILES

    @Override
    protected void initNewDocument() {
        permissions = Permissions.ALL_PERMISSIONS;
        final LocalDateTime created = LocalDateTime.now();
        final User currentUser = authData.getAuthorized();
        LOGGER.info("Start initialize new document by USER[{}]", currentUser.getId());
        final Task doc = new Task();
        doc.setDocumentStatus(DocumentStatus.NEW);
        doc.setCreationDate(created);
        doc.setAuthor(currentUser);
        final String parentId = getRequestParamByName("parentId");
        if (StringUtils.isNotEmpty(parentId)) {
            doc.setParent(initializeParentTask(parentId));
        }
        final String rootDocumentId = getRequestParamByName("rootDocumentId");
        if (StringUtils.isNotEmpty(rootDocumentId)) {
            doc.setRootDocumentId(rootDocumentId);
        } else {
            LOGGER.warn("RootDocumentId is not set");
        }
        final String formId = getRequestParamByName("formId");
        doc.setForm(getDefaultForm(formId));
        setDocument(doc);
        initDefaultInitiator();
    }

    private Task initializeParentTask(final String parentId) {
        Integer parentIdentifier = null;
        try {
            parentIdentifier = Integer.valueOf(parentId);
        } catch (NumberFormatException e) {
            LOGGER.error("Cant parse parentId[{}] to Integer", parentId, e);
        }
        if (parentIdentifier != null) {
            final Task parentTask = taskDao.getItemBySimpleCriteria(parentIdentifier);
            if (parentTask == null) {
                LOGGER.warn("Not found parentTask by \"{}\"", parentIdentifier);
            }
            return parentTask;
        } else {
            return null;
        }
    }

    @Override
    protected void initDocument(Integer id) {
        final User currentUser = authData.getAuthorized();
        LOGGER.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final Task document = taskDao.get(id);
            setDocument(document);
            if (!checkState(document, currentUser)) {
                return;
            }
            permissions = permissionChecker.getPermissions(authData, document);
            if (isReadPermission()) {
                //Простановка факта просмотра записи
                if (viewFactDao.registerViewFact(document, currentUser)) {
                    FacesContext.getCurrentInstance().addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                taskTreeHolder.refresh();
                try {
                    updateAttachments();
                } catch (Exception e) {
                    LOGGER.warn("Exception while check upload files", e);
                    addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
                }
            }
        } catch (Exception e) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_INITIALIZE);
            LOGGER.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    @Override
    public boolean saveNewDocument() {
        final User currentUser = authData.getAuthorized();
        final LocalDateTime created = LocalDateTime.now();
        LOGGER.info("Save new document by USER[{}]", currentUser.getId());
        // Сохранение дока в БД и создание записи в истории о создании
        final Task document = getDocument();
        if (document.getCreationDate() == null) {
            LOGGER.warn("creationDate not set. Set it to NOW");
            document.setCreationDate(created);
        }
        if (document.getAuthor() == null || !document.getAuthor().equals(currentUser)) {
            LOGGER.warn("Author[{}] not set or not equals with currentUser[{}]. Set it to currentUser", document.getAuthor(), currentUser);
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
            taskDao.save(document);
        } catch (Exception e) {
            LOGGER.error("saveNewDocument: on save document", e);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
        //Простановка факта просмотра записи
        try {
            viewFactDao.registerViewFact(document, currentUser);
        } catch (Exception e) {
            LOGGER.error("saveNewDocument: on viewFact register", e);
            addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
        }
        //Установка идшника для поиска поручений
        taskTreeHolder.setRootDocumentId(document.getUniqueId());
        return true;
    }

    @Override
    protected boolean saveDocument() {
        final User currentUser = authData.getAuthorized();
        LOGGER.info("Save document by USER[{}]", currentUser.getId());
        final Task document = getDocument();
        try {
            taskDao.save(document);
            //Установка идшника для поиска поручений
            taskTreeHolder.setRootDocumentId(document.getUniqueId());
            return true;
        } catch (Exception e) {
            LOGGER.error("saveDocument ERROR:", e);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }


    @Override
    public boolean isCanDelete() {
        if (!permissions.hasPermission(WRITE)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            LOGGER.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            LOGGER.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanView() {
        if (permissions == null || !permissions.hasPermission(READ)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
            LOGGER.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
            return false;
        }
        return true;
    }

    public DocumentForm getDefaultForm(final String formId) {
        final String defaultForm = StringUtils.isNotEmpty(formId) ? formId : DocumentForm.RB_CODE_TASK_ASSIGMENT;
        final DocumentForm form = documentFormDao.getByCode(defaultForm);
        if (form != null) {
            return form;
        } else {
            final List<DocumentForm> forms = documentFormDao.findByDocumentTypeCode(DocumentType.RB_CODE_TASK);
            if (forms != null && !forms.isEmpty()) {
                return forms.get(0);
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
    private boolean checkState(final Task document, final User user) {
        if (document == null) {
            setState(State.ERROR);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
            LOGGER.warn("Task NOT FOUND");
            return false;
        }
        if (document.isDeleted()) {
            setState(State.ERROR);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
            LOGGER.warn("TASK[{}] IS DELETED", document.getId());
            return false;
        }
        return true;
    }

    private Task createTask(Task task) {
        try {
            Task currentTask = taskDao.save(task);
            if (currentTask == null) {
                addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                return currentTask;
            }
        } catch (Exception e) {
            e.printStackTrace();
            addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
        }
        return null;
    }

    protected void initDefaultInitiator() {
        Task task = getDocument();
        User initiator = null;
        String key = task.getRootDocumentId();
        final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(key);
        if (rootDocumentId != null) {
            if (key.contains("incoming")) {
                IncomingDocument in_doc = incomingDocumentDao.getItemBySimpleCriteria(rootDocumentId);
                initiator = in_doc.getController();
            } else if (key.contains("outgoing")) {
                OutgoingDocument out_doc = outgoingDocumentDao.getItemBySimpleCriteria(rootDocumentId);
                initiator = out_doc.getController();
            } else if (key.contains("internal")) {
                InternalDocument internal_doc = internalDocumentDao.getItemBySimpleCriteria(rootDocumentId);
                initiator = internal_doc.getController();
            } else if (key.contains("request")) {
                RequestDocument request_doc = requestDocumentDao.getItemBySimpleCriteria(rootDocumentId);
                initiator = request_doc.getController();
            } else if (key.contains("task")) {
                Task parent_task = taskDao.getItemByListCriteria(rootDocumentId);
                final Set<User> users = parent_task.getExecutors();
                if (users != null && users.size() == 1) {
                    initiator = users.iterator().next();
                } else {
                    LOGGER.error(
                            "TASK[{}] can not set initiator cause: {}",
                            task.getId(),
                            users == null ? "executors is null" : "too much executors. size =" + users.size()
                    );
                }
            }
            if (initiator != null) {
                task.setInitiator(initiator);
            } else {
                LOGGER.warn("TASK[{}] Initiator not founded", task.getId());
            }
        }

    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void updateAttachments() {
        LOGGER.debug("Start updating attachments");
        if (getDocument() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId(getDocument().getUniqueId());
        }
        LOGGER.debug("Finish updating attachments");
    }

    public void deleteAttachment(Attachment attachment) {
        if (attachment != null) {
            Task document = getDocument();
            LocalDateTime created = LocalDateTime.now();
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
            setDocument(taskDao.save(document));
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