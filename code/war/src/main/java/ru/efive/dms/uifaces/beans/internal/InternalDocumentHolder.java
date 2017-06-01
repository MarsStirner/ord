package ru.efive.dms.uifaces.beans.internal;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.*;
import ru.efive.dms.uifaces.beans.task.DocumentTaskTreeHolder;
import ru.efive.dms.uifaces.beans.utils.ReferenceBookHelper;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageKey;
import ru.efive.dms.util.message.MessageUtils;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.*;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;

import static ru.efive.dms.util.message.MessageHolder.MSG_ERROR_ON_DELETE;
import static ru.efive.dms.util.security.Permissions.Permission.*;

@ViewScopedController(value = "internal_doc", transactionManager = "ordTransactionManager")
public class InternalDocumentHolder extends AbstractDocumentHolderBean<InternalDocument, InternalDocumentDao> {

    //TODO ACL
    private Permissions permissions;

    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;

    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;
    //Для проверки прав доступа
    @Autowired
    @Qualifier("permissionChecker")
    private PermissionChecker permissionChecker;

    @Autowired
    @Qualifier("taskDao")
    private TaskDao taskDao;

    @Autowired
    private ReferenceBookHelper referenceBookHelper;

    @Autowired
    public InternalDocumentHolder(@Qualifier("internalDocumentDao") InternalDocumentDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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
    protected InternalDocument newModel(AuthorizationData authData) {
        final LocalDateTime created = LocalDateTime.now();
        final InternalDocument doc = new InternalDocument();
        doc.setStatus(DocumentStatus.DOC_PROJECT_1);
        doc.setAuthor(authData.getAuthorized());
        doc.setCreationDate(created);
        doc.setForm(getDefaultForm());
        doc.setUserAccessLevel(authData.getCurrentAccessLevel());
        permissions = Permissions.ALL_PERMISSIONS;
        return doc;
    }

    @Override
    public boolean afterRead(InternalDocument document, AuthorizationData authData) {
        final UserAccessLevel userUAL = authData.getCurrentAccessLevel();
        final UserAccessLevel documentUAL = document.getUserAccessLevel();
        if (documentUAL.getLevel() > userUAL.getLevel()) {
            setState(State.ERROR);
            final FacesMessage msg = MessageHolder.MSG_TRY_TO_VIEW_WITH_LESSER_ACCESS_LEVEL;
            msg.setSummary(String.format(msg.getSummary(), documentUAL.getValue(), userUAL.getValue()));
            MessageUtils.addMessage(MessageKey.ERROR, msg);
            return false;
        }
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
    public boolean beforeDelete(InternalDocument document, AuthorizationData authData) {
        boolean result = true;
        List<Task> taskList = taskDao.getTaskListByRootDocumentId(document.getUniqueId(), false);
        for (Task task : taskList) {
            MessageUtils.addMessage(
                    MessageHolder.MSG_CANT_DELETE_EXISTS_LINK_WITH_OTHER_DOCUMENT,
                    referenceBookHelper.getLinkDescriptionByUniqueId(task.getUniqueId())
            );
            result = false;
        }
        return result;
    }

    @Override
    public boolean beforeCreate(InternalDocument document, AuthorizationData authData) {
        final LocalDateTime created = LocalDateTime.now();
        // Сохранение дока в БД и создание записи в истории о создании
        if (document.getCreationDate() == null) {
            log.warn("creationDate not set. Set it to NOW");
            document.setCreationDate(created);
        }
        if (document.getAuthor() == null || !document.getAuthor().equals(authData.getAuthorized())) {
            log.warn("Author[{}] not set or not equals with currentUser[{}]. Set it to currentUser", document.getAuthor(), authData.getAuthorized());
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
    public boolean afterCreate(InternalDocument document, AuthorizationData authData) {
        //Простановка факта просмотра записи
        try {
            viewFactDao.registerViewFact(document, authData.getAuthorized());
        } catch (Exception e) {
            log.error("createModel: on viewFact register", e);
            MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
        }
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
}