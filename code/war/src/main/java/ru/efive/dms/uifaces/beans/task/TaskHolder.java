package ru.efive.dms.uifaces.beans.task;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
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
import ru.entity.model.enums.DocumentType;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.*;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.util.ApplicationHelper;

import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;

import static ru.efive.dms.util.security.Permissions.Permission.*;

@ViewScopedController("task")
public class TaskHolder extends AbstractDocumentHolderBean<Task, TaskDao> {

    //TODO ACL
    private Permissions permissions;

    //Для проверки прав доступа
    @Autowired
    @Qualifier("permissionChecker")
    private PermissionChecker permissionChecker;

    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;

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

    @Autowired
    public TaskHolder(
            @Qualifier("taskDao") final TaskDao dao,
            @Qualifier("authData") final AuthorizationData authData) {
        super(dao, authData);
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
        log.info("Choose executors: {}", result);
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
        log.info("Choose controller: {}", result);
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
    protected Task newModel(AuthorizationData authData) {
        permissions = Permissions.ALL_PERMISSIONS;
        final LocalDateTime created = LocalDateTime.now();
        final User currentUser = authData.getAuthorized();
        final Task doc = new Task();
        doc.setStatus(DocumentStatus.NEW);
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
            log.warn("RootDocumentId is not set");
        }
        final String formId = getRequestParamByName("formId");
        doc.setForm(getDefaultForm(formId));
        doc.setInitiator(getDefaultInitiator(rootDocumentId));
        return doc;
    }


    private Task initializeParentTask(final String parentId) {
        Integer parentIdentifier = null;
        try {
            parentIdentifier = Integer.valueOf(parentId);
        } catch (NumberFormatException e) {
            log.error("Cant parse parentId[{}] to Integer", parentId, e);
        }
        if (parentIdentifier != null) {
            final Task parentTask = dao.getItemBySimpleCriteria(parentIdentifier);
            if (parentTask == null) {
                log.warn("Not found parentTask by \"{}\"", parentIdentifier);
            }
            return parentTask;
        } else {
            return null;
        }
    }

    @Override
    public boolean afterRead(Task document, AuthorizationData authData) {
        permissions = permissionChecker.getPermissions(authData, document);
        if (isReadPermission()) {
            //Простановка факта просмотра записи
            if (viewFactDao.registerViewFact(document, authData.getAuthorized())) {
                MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
            }
        }
        return true;
    }

    @Override
    public boolean afterCreate(Task document, AuthorizationData authData) {
        try {
            viewFactDao.registerViewFact(document, authData.getAuthorized());
        } catch (Exception e) {
            log.error("createModel: on viewFact register", e);
            MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
        }
        return true;
    }

    @Override
    public boolean beforeCreate(Task document, AuthorizationData authData) {
        final LocalDateTime created = LocalDateTime.now();
        // Сохранение дока в БД и создание записи в истории о создании
        if (document.getCreationDate() == null) {
            log.warn("creationDate not set. Set it to NOW");
            document.setCreationDate(created);
        }
        if (document.getAuthor() == null || !document.getAuthor().equals(authData.getAuthorized())) {
            log.warn("Author[{}] not set or not equals with current {}. Set it to currentUser", document.getAuthor(), authData.getLogString());
            document.setAuthor(authData.getAuthorized());
        }

        final HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created);
        historyEntry.setStartDate(created);
        historyEntry.setOwner(authData.getAuthorized());
        historyEntry.setDocType(document.getType().getName());
        historyEntry.setParentId(document.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created);
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        document.addToHistory(historyEntry);
        return true;
    }


    @Override
    public boolean isCanDelete(final AuthorizationData authData) {
        if (!permissions.hasPermission(WRITE)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanUpdate(final AuthorizationData authData) {
        if (!permissions.hasPermission(WRITE)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanRead(final AuthorizationData authData) {
        if (permissions == null || !permissions.hasPermission(READ)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
            log.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
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
            final List<DocumentForm> forms = documentFormDao.findByDocumentTypeCode(DocumentType.Task.getName());
            if (forms != null && !forms.isEmpty()) {
                return forms.get(0);
            } else {
                return null;
            }
        }
    }


    private User getDefaultInitiator(String key) {
        final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(key);
        if (rootDocumentId != null) {
            if (StringUtils.startsWith(key, DocumentType.IncomingDocument.getName())) {
                IncomingDocument in_doc = incomingDocumentDao.getItemByListCriteria(rootDocumentId);
                return in_doc.getController();
            } else if (StringUtils.startsWith(key, DocumentType.OutgoingDocument.getName())) {
                OutgoingDocument out_doc = outgoingDocumentDao.getItemByListCriteria(rootDocumentId);
                return out_doc.getController();
            } else if (StringUtils.startsWith(key, DocumentType.InternalDocument.getName())) {
                InternalDocument internal_doc = internalDocumentDao.getItemByListCriteria(rootDocumentId);
                return internal_doc.getController();
            } else if (StringUtils.startsWith(key, DocumentType.RequestDocument.getName())) {
                RequestDocument request_doc = requestDocumentDao.getItemByListCriteria(rootDocumentId);
                return request_doc.getController();
            } else if (StringUtils.startsWith(key, DocumentType.Task.getName())) {
                Task parent_task = dao.getItemByListCriteria(rootDocumentId);
                final Set<User> users = parent_task.getExecutors();
                if (users != null && users.size() == 1) {
                    return users.iterator().next();
                } else {
                    log.error("Error to set initiator from executors, cause: {}", users == null ? "executors is null" : "too much executors. size =" + users.size());
                }
            }
        }
        return null;
    }
}