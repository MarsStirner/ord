package ru.efive.dms.uifaces.beans.task;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
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
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.efive.dms.util.security.Permissions.Permission.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.*;

@Named("task")
@ViewScoped
public class TaskHolder extends AbstractDocumentHolderBean<Task> {

    private static final Logger LOGGER = LoggerFactory.getLogger("TASK");

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
    @Inject
    @Named("documentTaskTree")
    private DocumentTaskTreeHolder taskTreeHolder;
    //Для проверки прав доступа
    @Inject
    @Named("permissionChecker")
    private PermissionChecker permissionChecker;

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    // Выбора исполнителей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseExecutors() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_EXECUTORS));
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
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder.DIALOG_TITLE_VALUE_RESPONSIBLE));
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
            sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).update(document);
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
        final Date created = new LocalDateTime().toDate();
        final User currentUser = sessionManagement.getAuthData().getAuthorized();
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
        try{
            parentIdentifier = Integer.valueOf(parentId);
        } catch (NumberFormatException e){
            LOGGER.error("Cant parse parentId[{}] to Integer", parentId, e);
        }
        if(parentIdentifier != null) {
            final Task parentTask = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemByIdForSimpleView(parentIdentifier);
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
        final User currentUser = sessionManagement.getAuthData().getAuthorized();
        LOGGER.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final Task document = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemById(id);
            setDocument(document);
            if (!checkState(document, currentUser)) {
                return;
            }
            permissions = permissionChecker.getPermissions(sessionManagement.getAuthData(), document);
            if (isReadPermission()) {
                //Простановка факта просмотра записи
                if (sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).registerViewFact(document, currentUser)) {
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
    protected boolean saveNewDocument() {
        final User currentUser = sessionManagement.getAuthData().getAuthorized();
        final Date created = new LocalDateTime().toDate();
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
            sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(document);
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
        //Установка идшника для поиска поручений
        taskTreeHolder.setRootDocumentId(document.getUniqueId());
        return true;
    }

    @Override
    protected boolean saveDocument() {
        final User currentUser = sessionManagement.getAuthData().getAuthorized();
        LOGGER.info("Save document by USER[{}]", currentUser.getId());
        final Task document = getDocument();
        try {
            sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(document);
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

    public DocumentForm getDefaultForm(final String formId) {
        final String defaultForm = StringUtils.isNotEmpty(formId) ? formId : DocumentForm.RB_CODE_TASK_ASSIGMENT;
        final DocumentFormDAOImpl dao = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO);
        final DocumentForm form = dao.getByCode(defaultForm);
        if (form != null) {
            return form;
        } else {
            final List<DocumentForm> forms = dao.findByDocumentTypeCode(DocumentType.RB_CODE_TASK);
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
            Task currentTask = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(task);
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

    public String getLinkDescriptionById(String key) {
        if (!key.isEmpty()) {
            final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(key);
            if (rootDocumentId == null) {
                return "";
            }
            final SimpleDateFormat sdf = ApplicationHelper.getDateFormat();
            if (key.contains("incoming")) {
                final IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber().equals("") ? "Черновик входщяего документа от " + sdf
                        .format(in_doc.getCreationDate()) : "Входящий документ № " + in_doc.getRegistrationNumber() + " от " + sdf.format(
                        in_doc.getRegistrationDate()
                ));

            } else if (key.contains("outgoing")) {
                OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber()
                        .equals("") ? "Черновик исходящего документа от " + sdf.format(out_doc.getCreationDate()) : "Исходящий документ № " + out_doc
                        .getRegistrationNumber() + " от " + sdf.format(
                        out_doc.getRegistrationDate()
                ));

            } else if (key.contains("internal")) {
                InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber()
                        .equals("") ? "Черновик внутреннего документа от " + sdf.format(
                        internal_doc.getCreationDate()
                ) : "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + sdf.format(internal_doc.getRegistrationDate()));

            } else if (key.contains("request")) {
                RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber()
                        .equals("") ? "Черновик обращения граждан от " + sdf.format(
                        request_doc.getCreationDate()
                ) : "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + sdf.format(request_doc.getRegistrationDate()));

            } else if (key.contains("task")) {
                Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemByIdForSimpleView(rootDocumentId);
                return (task_doc.getTaskNumber() == null || task_doc.getTaskNumber().equals("") ? "Черновик поручения от " + sdf
                        .format(task_doc.getCreationDate()) : "Поручение № " + task_doc.getTaskNumber() + " от " + sdf
                        .format(task_doc.getCreationDate()));
            }
        }

        return "";
    }

    protected void initDefaultInitiator() {
        Task task = getDocument();
        User initiator = null;
        String key = task.getRootDocumentId();
        final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(key);
        if (rootDocumentId != null) {
            if (key.contains("incoming")) {
                IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).getItemByIdForListView(
                        rootDocumentId
                );
                initiator = in_doc.getController();
            } else if (key.contains("outgoing")) {
                OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).getItemByIdForListView(
                        rootDocumentId
                );
                initiator = out_doc.getController();
            } else if (key.contains("internal")) {
                InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO)
                        .getItemByIdForListView(rootDocumentId);
                initiator = internal_doc.getController();
            } else if (key.contains("request")) {
                RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO)
                        .getItemByIdForListView(rootDocumentId);
                initiator = request_doc.getController();
            } else if (key.contains("task")) {
                Task parent_task = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemByIdForListView(rootDocumentId);
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
            setDocument(sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(document));
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