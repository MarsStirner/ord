package ru.efive.dms.uifaces.beans.outgoing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.dialogs.*;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.*;
import ru.hitsl.sql.dao.referenceBook.DocumentFormDAOImpl;
import ru.hitsl.sql.dao.util.ApplicationDAONames;
import ru.util.ApplicationHelper;

import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.efive.dms.util.security.Permissions.Permission.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.*;

@Named("out_doc")
@ViewScoped
public class OutgoingDocumentHolder extends AbstractDocumentHolderBean<OutgoingDocument> {
    //Именованный логгер (OUTGOING_DOCUMENT)
    private static final Logger LOGGER = LoggerFactory.getLogger("OUTGOING_DOCUMENT");

    @PreDestroy
    public void destroy(){
        LOGGER.info("Bean destroyed");
    }

    //TODO ACL
    private Permissions permissions;
    @Inject
    @Named("sessionManagement")
    private SessionManagementBean sessionManagement;
    @Inject
    @Named("fileManagement")
    private FileManagementBean fileManagement;
    //Для проверки прав доступа
    @Inject
    @Named("permissionChecker")
    private PermissionChecker permissionChecker;

    private List<Attachment> attachments = new ArrayList<>();

    public void handleFileUpload(FileUploadEvent event) {
        final UploadedFile file = event.getFile();
        if (file != null) {
            LOGGER.info("Upload new file[{}] content-type={} size={}", file.getFileName(), file.getContentType(), file.getSize());
            final Attachment attachment = new Attachment();
            attachment.setFileName(file.getFileName());
            attachment.setCreated(new LocalDateTime().toDate());
            attachment.setAuthorId(sessionManagement.getAuthData().getAuthorized().getId());
            attachment.setParentId(getDocument().getUniqueId());
            final boolean result = fileManagement.createFile(attachment, file.getContents());
            LOGGER.info("After alfresco call Attachment.id={}", attachment.getId());
            if(result){
                attachments.add(attachment);
            }
            addMessage(new FacesMessage("Successful! " + file.getFileName() + " is uploaded. Size " + file.getSize()));
        } else {
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
        }
    }

    private ProcessorModalBean processorModal = new ProcessorModalBean() {
        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) {
                saveNewDocument();
            }
        }

        @Override
        protected void doPostProcess(ActionResult actionResult) {
            OutgoingDocument document = (OutgoingDocument) actionResult.getProcessedData();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<>();
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  ////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Выбора документа-основания /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseReasonDocument() {
        final String preselected = getDocument().getReasonDocumentId();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ReasonDocumentDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        final Map<String, Object> viewParams = new HashMap<>(AbstractDialog.getViewOptions());
        viewParams.put("width", "\'95%\'");
        viewParams.put("contentWidth", "\'100%\'");
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectReasonDocumentDialog.xhtml", viewParams, null);
    }

    public void onReasonDocumentChosen(SelectEvent event) {
        final String selected = (String) event.getObject();
        getDocument().setReasonDocumentId(selected);
        LOGGER.info("Choose reasonDocument From Dialog \'{}\'", selected != null ? selected : "#NOTSET");
    }

    public void addVersionForAttachment(final Attachment attachment){
        if (attachment != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(AttachmentVersionDialogHolder.DIALOG_SESSION_KEY, attachment);
            final Map<String, List<String>> params = ImmutableMap.of(
                    AttachmentVersionDialogHolder.DIALOG_DOCUMENT_KEY,
                    (List<String>) ImmutableList.of(getDocument().getUniqueId())
            );
            RequestContext.getCurrentInstance().openDialog("/dialogs/addVersionForAttachment.xhtml", AbstractDialog.getViewOptions(), params );
        } else {
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
        }
    }

    public void handleAddVersionDialogResult(final SelectEvent event){
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Add version dialog: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final FacesMessage msg = (FacesMessage) result.getResult();
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, msg);
        }
    }

    //Выбора руководителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
        params.put(UserDialogHolder.DIALOG_GROUP_KEY, ImmutableList.of(Group.RB_CODE_MANAGERS));
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

    //Выбора контрагента //////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseContragent() {
        final Contragent preselected = getDocument().getContragent();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(ContragentDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectContragentDialog.xhtml", AbstractDialog.getViewOptions(), null);
    }

    public void onContragentChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose contragent: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Contragent selected = (Contragent) result.getResult();
            getDocument().setContragent(selected);
        }
    }

    //Выбора исполнителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseExecutor() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder.DIALOG_TITLE_VALUE_RESPONSIBLE));
        final User preselected = getDocument().getExecutor();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onExecutorChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose executor: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final User selected = (User) result.getResult();
            getDocument().setExecutor(selected);
        }
    }

    // Выбора пользователей-читателей /////////////////////////////////////////////////////////////////////////////////////////////
    public void choosePersonReaders() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_PERSON_READERS));
        final List<User> preselected = getDocument().getPersonReadersList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onPersonReadersChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose personReaders: {}", result);
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
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_PERSON_EDITORS));
        final List<User> preselected = getDocument().getPersonEditorsList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onPersonEditorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose personEditors: {}", result);
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
        params.put(MultipleRoleDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleRoleDialogHolder.DIALOG_TITLE_VALUE_READERS));
        final Set<Role> preselected = getDocument().getRoleReaders();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleRoleDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleRoleDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onRoleReadersChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose roleReaders: {}", result);
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
        params.put(MultipleRoleDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleRoleDialogHolder.DIALOG_TITLE_VALUE_EDITORS));
        final Set<Role> preselected = getDocument().getRoleEditors();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleRoleDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleRoleDialog.xhtml", AbstractDialog.getViewOptions(), params);
    }

    public void onRoleEditorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose roleEditors: {}", result);
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
            final boolean result = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).delete(getDocumentId());
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
    protected void initNewDocument() {
        permissions = Permissions.ALL_PERMISSIONS;
        final Date created = new LocalDateTime().toDate();
        final User currentUser = sessionManagement.getAuthData().getAuthorized();
        LOGGER.info("Start initialize new document by USER[{}]", currentUser.getId());
        final OutgoingDocument document = new OutgoingDocument();
        document.setDocumentStatus(DocumentStatus.NEW);
        document.setCreationDate(created);
        document.setAuthor(sessionManagement.getLoggedUser());
        document.setForm(getDefaultForm());
        document.setUserAccessLevel(sessionManagement.getAuthData().getCurrentAccessLevel());
        setDocument(document);
    }

    @Override
    protected void initDocument(Integer id) {
        final User currentUser = sessionManagement.getAuthData().getAuthorized();
        LOGGER.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final OutgoingDocument document = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).getItemById(id);
            setDocument(document);
            if (!checkState(document, currentUser)) {
                return;
            }
            //Проверка прав на открытие
            permissions = permissionChecker.getPermissions(sessionManagement.getAuthData(), document);
            if (isReadPermission()) {
                //Простановка факта просмотра записи
                if (sessionManagement.getDAO(ViewFactDaoImpl.class, ApplicationDAONames.VIEW_FACT_DAO).registerViewFact(document, currentUser)) {
                    addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                try {
                    updateAttachments();
                } catch (Exception e){
                    LOGGER.warn("Exception while check upload files", e);
                    addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
                }
            }
        } catch (Exception e) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MSG_ERROR_ON_INITIALIZE);
            LOGGER.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    @Override
    protected boolean saveNewDocument() {
        final User currentUser = sessionManagement.getAuthData().getAuthorized();
        final Date created = new LocalDateTime().toDate();
        LOGGER.info("Save new document by USER[{}]", currentUser.getId());
        // Сохранение дока в БД и создание записи в истории о создании
        final OutgoingDocument document = getDocument();
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
            sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).save(document);
        } catch (Exception e) {
            LOGGER.error("saveNewDocument: on save document", e);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
        //Простановка факта просмотра записи
        try {
            sessionManagement.getDAO(ViewFactDaoImpl.class, ApplicationDAONames.VIEW_FACT_DAO).registerViewFact(document, currentUser);
        } catch (Exception e) {
            LOGGER.error("saveNewDocument: on viewFact register", e);
            addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
        }
        return true;
    }

    @Override
    protected boolean saveDocument() {
        final User currentUser = sessionManagement.getAuthData().getAuthorized();
        LOGGER.info("Save document by USER[{}]", currentUser.getId());
        final OutgoingDocument document = getDocument();
        try {
            sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).save(document);
            return true;
        } catch (Exception e) {
            LOGGER.error("saveDocument ERROR:", e);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    protected boolean doAfterSave() {
        final UserAccessLevel accessLevel = sessionManagement.getAuthData().getCurrentAccessLevel();
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
            LOGGER.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            LOGGER.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanView() {
        if (permissions == null || !permissions.hasPermission(READ)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
            LOGGER.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
            return false;
        }
        return true;
    }

    public DocumentForm getDefaultForm() {
        final DocumentFormDAOImpl dao = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO);
        final DocumentForm form = dao.getByCode(DocumentForm.RB_CODE_OUTGOING_LETTER);
        if (form != null) {
            return form;
        } else {
            final List<DocumentForm> formList = dao.findByDocumentTypeCode(DocumentType.RB_CODE_OUTGOING);
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
    private boolean checkState(final OutgoingDocument document, final User user) {
        if (document == null) {
            setState(State.ERROR);
            LOGGER.warn("Document NOT FOUND");
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
            return false;
        }
        if (document.isDeleted()) {
            setState(State.ERROR);
            LOGGER.warn("Document[{}] IS DELETED", document.getId());
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
            return false;
        }
        final UserAccessLevel docAccessLevel = document.getUserAccessLevel();
        final UserAccessLevel accessLevel = sessionManagement.getAuthData().getCurrentAccessLevel();
        if (docAccessLevel.getLevel() > accessLevel.getLevel()) {
            setState(State.ERROR);
            final FacesMessage msg = MessageHolder.MSG_TRY_TO_VIEW_WITH_LESSER_ACCESS_LEVEL;
            msg.setSummary(
                    String.format(
                            msg.getSummary(), docAccessLevel.getValue(), accessLevel.getValue()

                    )
            );
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, msg);
            LOGGER.warn(
                    "Document[{}] has higher accessLevel[{}] then user[{}]", document.getId(), docAccessLevel.getValue(), accessLevel.getValue()
            );
            return false;
        }
        return true;
    }

    public String getLinkDescriptionByUniqueId(String documentKey) {

        if (!documentKey.isEmpty()) {
            final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(documentKey);
            if (documentKey.contains("incoming")) {
                final  IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                if (in_doc != null) {
                    return (in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber().equals("") ? "Черновик входщяего документа от " + ApplicationHelper
                            .formatDate(in_doc.getCreationDate()) : "Входящий документ № " + in_doc
                            .getRegistrationNumber() + " от " + ApplicationHelper.formatDate(in_doc.getRegistrationDate()));
                } else {
                    return "";
                }

            } else if (documentKey.contains("outgoing")) {
                final OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                if(out_doc != null) {
                    return (out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber().equals("") ? "Черновик исходящего документа от " + ApplicationHelper
                            .formatDate(out_doc.getCreationDate()) : "Исходящий документ № " + out_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(out_doc.getRegistrationDate()));
                } else {
                    return "";
                }

            } else if (documentKey.contains("internal")) {
                final InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                if(internal_doc != null) {
                    return (internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber().equals("") ? "Черновик внутреннего документа от " + ApplicationHelper.formatDate(
                            internal_doc.getCreationDate()
                    ) : "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                            internal_doc.getRegistrationDate()
                    ));
                } else {
                    return "";
                }

            } else if (documentKey.contains("request")) {
                final RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                if(request_doc != null) {
                    return (request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber().equals("") ? "Черновик обращения граждан от " + ApplicationHelper.formatDate(
                            request_doc.getCreationDate()
                    ) : "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                            request_doc.getRegistrationDate()
                    ));
                } else {
                    return "";
                }

            } else if (documentKey.contains("task")) {
                final Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemByIdForSimpleView(rootDocumentId);
                if(task_doc != null) {
                    return (task_doc.getTaskNumber() == null || task_doc.getTaskNumber().equals("") ? "Черновик поручения от " + ApplicationHelper
                            .formatDate(task_doc.getCreationDate()) : "Поручение № " + task_doc.getTaskNumber() + " от " + ApplicationHelper.formatDate(
                            task_doc.getCreationDate()
                    ));
                } else {
                    return "";
                }
            }
        }
        return "";
    }

    protected boolean validateHolder() {
        boolean result = true;
        FacesContext context = FacesContext.getCurrentInstance();
        if (getDocument().getController() == null) {
            context.addMessage(null, MSG_CONTROLLER_NOT_SET);
            result = false;
        }
        if (getDocument().getExecutor() == null) {
            context.addMessage(null, MSG_EXECUTOR_NOT_SET);
            result = false;
        }
        if (getDocument().getContragent() == null) {
            context.addMessage(null, MSG_RECIPIENTS_NOT_SET);
            result = false;
        }
        if (StringUtils.isEmpty(getDocument().getShortDescription())) {
            context.addMessage(null, MSG_SHORT_DESCRIPTION_NOT_SET);
            result = false;
        }
        return result;
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
        if (attachment != null && isCanEdit()) {
            final OutgoingDocument document = getDocument();
            final Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
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
            sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).save(document);
        }
    }

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

}