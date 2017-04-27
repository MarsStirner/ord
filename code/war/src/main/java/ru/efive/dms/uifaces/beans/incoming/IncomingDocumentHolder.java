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
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageKey;
import ru.efive.dms.util.message.MessageUtils;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.*;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;

import static ru.efive.dms.util.security.Permissions.Permission.*;

@ViewScopedController(value = "in_doc", transactionManager = "ordTransactionManager")
public class IncomingDocumentHolder extends AbstractDocumentHolderBean<IncomingDocument> {
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
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;

    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;
    @Autowired
    @Qualifier("incomingDocumentDao")
    private IncomingDocumentDao incomingDocumentDao;

    /**
     * Связанные с этим входящим письмом исходящие документы
     */
    private List<OutgoingDocument> relatedDocuments;


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
    protected boolean deleteDocument() {
        try {
            final boolean result = incomingDocumentDao.delete(getDocument());
            if (!result) {
                MessageUtils.addMessage(MessageHolder.MSG_CANT_DELETE);
            }
            return result;
        } catch (Exception e) {
            log.error("INTERNAL ERROR ON DELETE_DOCUMENT:", e);
            MessageUtils.addMessage(MessageHolder.MSG_ERROR_ON_DELETE);
            return false;
        }
    }

    @Override
    @Transactional("ordTransactionManager")
    protected void initNewDocument() {
        permissions = Permissions.ALL_PERMISSIONS;
        final LocalDateTime created = LocalDateTime.now();
        final User currentUser = authData.getAuthorized();
        log.info("Start initialize new document by USER[{}]", currentUser.getId());
        final IncomingDocument doc = new IncomingDocument();
        doc.setDocumentStatus(DocumentStatus.NEW);
        doc.setDeliveryDate(created);
        doc.setCreationDate(created);
        doc.setAuthor(currentUser);
        doc.setForm(getDefaultForm());
        doc.setUserAccessLevel(authData.getCurrentAccessLevel());
        setDocument(doc);
        relatedDocuments = new ArrayList<>(0);
    }


    @Override
    @Transactional(value = "ordTransactionManager")
    protected void initDocument(final Integer id) {
        final User currentUser = authData.getAuthorized();
        log.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final IncomingDocument document = incomingDocumentDao.get(id);
            setDocument(document);
            if (!checkState(document, currentUser)) {
                return;
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
                if (viewFactDao.registerViewFact(document, currentUser)) {
                    MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                //Установка идшника для поиска поручений
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                //Поиск поручений
                taskTreeHolder.refresh();
            }
        } catch (Exception e) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_ERROR_ON_INITIALIZE);
            log.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    @Override
    @Transactional("ordTransactionManager")
    public boolean saveNewDocument() {
        final User currentUser = authData.getAuthorized();
        final LocalDateTime created = LocalDateTime.now();
        log.info("Save new document by USER[{}]", currentUser.getId());
        // Сохранение дока в БД и создание записи в истории о создании
        final IncomingDocument document = getDocument();
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
            incomingDocumentDao.save(document);
        } catch (Exception e) {
            log.error("saveNewDocument: on save document", e);
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
        //Простановка факта просмотра записи
        try {
            viewFactDao.registerViewFact(document, currentUser);
        } catch (Exception e) {
            log.error("saveNewDocument: on viewFact register", e);
            MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
        }
        //Установка идшника для поиска поручений
        taskTreeHolder.setRootDocumentId(document.getUniqueId());
        return true;
    }

    @Override
    @Transactional("ordTransactionManager")
    protected boolean saveDocument() {
        final User currentUser = authData.getAuthorized();
        log.info("Save document by USER[{}]", currentUser.getId());
        final IncomingDocument document = getDocument();
        try {
            incomingDocumentDao.update(document);
            //Установка идшника для поиска поручений
            taskTreeHolder.setRootDocumentId(document.getUniqueId());
            return true;
        } catch (Exception e) {
            log.error("saveDocument ERROR:", e);
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_ERROR_ON_SAVE);
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
            MessageUtils.addMessage(MessageKey.ERROR, msg);
            return false;
        }
        return true;
    }

    @Override
    public boolean isCanDelete() {
        if (!permissions.hasPermission(WRITE)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanView() {
        if (permissions == null || !permissions.hasPermission(READ)) {
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
            log.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
            return false;
        }
        return true;
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
     * Проверяем является ли документ валидным, и есть ли у пользователя к нему уровень допуска
     *
     * @param document документ для проверки
     * @return true - допуск есть
     */
    private boolean checkState(final IncomingDocument document, final User user) {
        if (document == null) {
            setState(State.ERROR);
            log.warn("Document NOT FOUND");
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
            return false;
        }
        if (document.isDeleted()) {
            setState(State.ERROR);
            log.warn("Document[{}] IS DELETED", document.getId());
            MessageUtils.addMessage(MessageKey.ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
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
            MessageUtils.addMessage(MessageKey.ERROR, msg);
            log.warn(
                    "Document[{}] has higher accessLevel[{}] then user[{}]", document.getId(), docAccessLevel.getValue(), accessLevel.getValue()
            );
            return false;
        }
        return true;
    }

    protected boolean validateHolder() {
        boolean result = true;
        if (getDocument().getController() == null) {
MessageUtils.addMessage( MessageHolder.MSG_CONTROLLER_NOT_SET);
            result = false;
        }
        if (getDocument().getContragent() == null) {
MessageUtils.addMessage( MessageHolder.MSG_CONTRAGENT_NOT_SET);
            result = false;
        }
        if (getDocument().getRecipientUsers() == null || getDocument().getRecipientUsers().isEmpty()) {
MessageUtils.addMessage( MessageHolder.MSG_RECIPIENTS_NOT_SET);
            result = false;
        }
        if (getDocument().getShortDescription() == null || getDocument().getShortDescription().equals("")) {
MessageUtils.addMessage( MessageHolder.MSG_SHORT_DESCRIPTION_NOT_SET);
            result = false;
        }
        return result;
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

    public DocumentTaskTreeHolder getTaskTreeHolder() {
        return taskTreeHolder;
    }

    public void setTaskTreeHolder(DocumentTaskTreeHolder taskTreeHolder) {
        this.taskTreeHolder = taskTreeHolder;
    }

}