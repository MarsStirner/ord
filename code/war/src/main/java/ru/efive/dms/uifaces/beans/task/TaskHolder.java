package ru.efive.dms.uifaces.beans.task;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageKey;
import ru.efive.dms.util.message.MessageUtils;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
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
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;

import static ru.efive.dms.util.security.Permissions.Permission.*;

@ViewScopedController("task")
public class TaskHolder extends AbstractDocumentHolderBean<Task> {

    private static final Logger LOGGER = LoggerFactory.getLogger("TASK");
    //TODO ACL
    private Permissions permissions;
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

    @PreDestroy
    public void destroy() {
        LOGGER.info("Bean destroyed");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
                MessageUtils.addMessage(MessageHolder.MSG_ERROR_ON_DELETE);
                LOGGER.error("After delete operation task hasn't deleted = true");
                return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("Error on delete: ", e);
            MessageUtils.addMessage(MessageHolder.MSG_ERROR_ON_DELETE);
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
        taskTreeHolder.setRootDocumentId(doc.getUniqueId());
        taskTreeHolder.refresh();
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
                    MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                taskTreeHolder.refresh();
            }
        } catch (Exception e) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_ERROR_ON_INITIALIZE);
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
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
        //Простановка факта просмотра записи
        try {
            viewFactDao.registerViewFact(document, currentUser);
        } catch (Exception e) {
            LOGGER.error("saveNewDocument: on viewFact register", e);
            MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
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
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }


    @Override
    public boolean isCanDelete() {
        if (!permissions.hasPermission(WRITE)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            LOGGER.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            LOGGER.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanView() {
        if (permissions == null || !permissions.hasPermission(READ)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
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
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
            LOGGER.warn("Task NOT FOUND");
            return false;
        }
        if (document.isDeleted()) {
            setState(State.ERROR);
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
            LOGGER.warn("TASK[{}] IS DELETED", document.getId());
            return false;
        }
        return true;
    }

    private Task createTask(Task task) {
        try {
            Task currentTask = taskDao.save(task);
            if (currentTask == null) {
MessageUtils.addMessage( MessageHolder.MSG_CANT_SAVE);
            } else {
                return currentTask;
            }
        } catch (Exception e) {
            e.printStackTrace();
MessageUtils.addMessage( MessageHolder.MSG_ERROR_ON_SAVE_NEW);
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


    public DocumentTaskTreeHolder getTaskTreeHolder() {
        return taskTreeHolder;
    }

    public void setTaskTreeHolder(DocumentTaskTreeHolder taskTreeHolder) {
        this.taskTreeHolder = taskTreeHolder;
    }


}