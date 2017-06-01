package ru.efive.dms.uifaces.beans.incoming;


import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
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
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.Task;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.*;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.util.ApplicationHelper;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;

import static ru.efive.dms.util.security.Permissions.Permission.*;

@ViewScopedController(value = "in_doc", transactionManager = "ordTransactionManager")
public class IncomingDocumentHolder extends AbstractDocumentHolderBean<IncomingDocument, IncomingDocumentDao> {
    //TODO ACL
    private Permissions permissions;

    //Для проверки прав доступа
    @Autowired
    @Qualifier("permissionChecker")
    private PermissionChecker permissionChecker;

    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;

    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;

    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;

    @Autowired
    @Qualifier("taskDao")
    private TaskDao taskDao;

    @Autowired
    private ReferenceBookHelper referenceBookHelper;


    /**
     * Связанные с этим входящим письмом исходящие документы
     */
    private List<OutgoingDocument> relatedDocuments;

    @Autowired
    public IncomingDocumentHolder(@Qualifier("incomingDocumentDao") IncomingDocumentDao dao, @Qualifier("authData") AuthorizationData authData) {
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

    //Выбора контрагента ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseContragent() {
        final Contragent preselected = getDocument().getContragent();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ContragentDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectContragentDialog.xhtml", AbstractDialog.getViewOptions(), null);
    }

    public void onContragentChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose contragent: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Contragent selected = (Contragent) result.getResult();
            getDocument().setContragent(selected);
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
    protected IncomingDocument newModel(AuthorizationData authData) {
        final LocalDateTime created = LocalDateTime.now();
        final IncomingDocument doc = new IncomingDocument();
        doc.setStatus(DocumentStatus.NEW);
        doc.setDeliveryDate(created);
        doc.setCreationDate(created);
        doc.setAuthor(authData.getAuthorized());
        doc.setForm(getDefaultForm());
        doc.setUserAccessLevel(authData.getCurrentAccessLevel());
        relatedDocuments = new ArrayList<>(0);
        permissions = Permissions.ALL_PERMISSIONS;
        return doc;
    }

    @Override
    public boolean afterRead(IncomingDocument document, AuthorizationData authData) {
        final UserAccessLevel userUAL = authData.getCurrentAccessLevel();
        final UserAccessLevel documentUAL = document.getUserAccessLevel();
        if (documentUAL.getLevel() > userUAL.getLevel()) {
            setState(State.ERROR);
            final FacesMessage msg = MessageHolder.MSG_TRY_TO_VIEW_WITH_LESSER_ACCESS_LEVEL;
            msg.setSummary(String.format(msg.getSummary(), documentUAL.getValue(), userUAL.getValue()));
            MessageUtils.addMessage(MessageKey.ERROR, msg);
            return false;
        }

        relatedDocuments = loadRelatedDocuments();
        //Проверка прав на открытие
        permissions = permissionChecker.getPermissions(authData, document);
        if (!isReadPermission()) {
            //Проверяем права на связанные доки, если есть, то прокидываем на чтение
            for (OutgoingDocument relatedDocument : relatedDocuments) {
                final Permissions relatedPermissions = permissionChecker.getPermissions(authData, relatedDocument);
                if (relatedPermissions.hasPermission(READ)) {
                    log.info("Get permissions from related documents [{}], {}", relatedDocument.getUniqueId(), relatedPermissions);
                    permissions.addPermission(READ);
                    break;
                }
            }
        }
        if (isReadPermission()) {
            //Простановка факта просмотра записи
            if (viewFactDao.registerViewFact(document, authData.getAuthorized())) {
                MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
            }
        }
        return true;
    }

    @Override
    public boolean beforeCreate(IncomingDocument document, AuthorizationData authData) {
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
    public boolean beforeDelete(IncomingDocument document, AuthorizationData authData) {
        boolean result = true;
        final List<OutgoingDocument> outgoingDocuments = getRelatedDocuments();
        for (OutgoingDocument outgoingDocument : outgoingDocuments) {
            MessageUtils.addMessage(
                    MessageHolder.MSG_CANT_DELETE_EXISTS_LINK_WITH_OTHER_DOCUMENT,
                    referenceBookHelper.getLinkDescriptionByUniqueId(outgoingDocument.getUniqueId())
            );
            result = false;
        }
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
    public boolean afterCreate(final IncomingDocument document, final AuthorizationData authData) {
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
    public boolean isCanRead(AuthorizationData authData) {
        if (permissions == null || !permissions.hasPermission(READ)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
            log.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
            return false;
        }
        return true;
    }

    @Override
    public boolean isCanUpdate(AuthorizationData authData) {
        if (!permissions.hasPermission(WRITE)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanDelete(AuthorizationData authData) {
        if (!permissions.hasPermission(WRITE)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }



    public DocumentForm getDefaultForm() {
        final DocumentForm form = documentFormDao.getByCode(DocumentForm.RB_CODE_INCOMING_LETTER);
        if (form != null) {
            return form;
        } else {
            final List<DocumentForm> formList = documentFormDao.findByDocumentTypeCode(DocumentType.RB_CODE_INCOMING);
            if (!formList.isEmpty()) {
                return formList.get(0);
            } else {
                return null;
            }
        }
    }

    /**
     * Найти в БД все исходящие документы для нашего входящего
     *
     * @return список исходящих документов \ пустой список
     */
    public List<OutgoingDocument> loadRelatedDocuments() {
        if (StringUtils.isNotEmpty(getDocument().getUniqueId())) {
            return outgoingDocumentDao
                    .findAllDocumentsByReasonDocumentId(getDocument().getUniqueId());
        } else {
            return new ArrayList<>(0);
        }
    }

    public List<OutgoingDocument> getRelatedDocuments() {
        return relatedDocuments;
    }
}