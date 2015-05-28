package ru.efive.dms.uifaces.beans.task;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.MultipleUserDialogHolder;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import javax.ejb.EJB;
import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.*;
import static ru.efive.dms.util.security.Permissions.Permission.*;

@Named("task")
@ConversationScoped
public class TaskHolder extends AbstractDocumentHolderBean<Task, Integer> implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger("TASK");
    private static final long serialVersionUID = 4716264614655470705L;
    private Permissions permissions;
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();

    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();
    private transient String stateComment;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;
    @Inject
    @Named("documentTaskTree")
    private transient DocumentTaskTreeHolder taskTreeHolder;
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
                    history = new HashSet<HistoryEntry>();
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
            if (in_result != null) {
                if (StringUtils.isNotEmpty(in_result)) {
                    setActionResult(in_result);
                }
            } else {
                setActionResult("");
            }
        }
    };
    //Для проверки прав доступа
    @EJB
    private PermissionChecker permissionChecker;

    public boolean isReadPermission() {
        return permissions.hasPermission(READ);
    }

    public boolean isEditPermission() {
        return permissions.hasPermission(WRITE);
    }

    public boolean isExecutePermission() {
        return permissions.hasPermission(EXECUTE);
    }

    public boolean isCanExecute() {
        if (!permissions.hasPermission(EXECUTE)) {
            setStateComment("Доступ запрещен");
            logger.error("USER[{}] EXECUTE ACCESS TO TASK[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(EXECUTE);
    }

    @Override
    protected boolean deleteDocument() {
        final Task document = getDocument();
        document.setDeleted(true);
        try {
            final Task afterChange = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).update(document);
            if (!afterChange.isDeleted()) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error on delete: ", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
        }
        return false;
    }


    @Override
    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }

    // FILES

    @Override
    protected void initNewDocument() {
        Task doc = new Task();
        permissions = Permissions.ALL_PERMISSIONS;
        final LocalDateTime created = new LocalDateTime();
        doc.setCreationDate(created.toDate());
        doc.setAuthor(sessionManagement.getLoggedUser());
        doc.setDocumentStatus(DocumentStatus.NEW);
        final String parentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
        if (StringUtils.isNotEmpty(parentId)) {
            //TODO ClassCastException
            final Task parentTask = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemByIdForSimpleView(Integer.valueOf(parentId));
            if (parentTask != null) {
                doc.setParent(parentTask);
            } else {
                logger.warn("Not found parentTask by \"{}\"", parentId);
            }
        }
        final String rootDocumentId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("rootDocumentId");
        if (StringUtils.isNotEmpty(rootDocumentId)) {
            doc.setRootDocumentId(rootDocumentId);
        } else {
            logger.warn("RootDocumentId is not set");
        }
        DocumentForm form = null;
        final String formId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("formId");
        List<DocumentForm> forms = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByCategoryAndDescription(
                "Поручения", StringUtils.isEmpty(formId) ? "task" : formId
        );
        if (!forms.isEmpty()) {
            form = forms.get(0);
        } else {
            forms = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByCategory("Поручения");
            if (forms.size() > 0) {
                form = forms.get(0);
            }
        }
        if (form != null) {
            doc.setForm(form);
        }
        HistoryEntry historyEntry = getInitialHistoryEntry(doc, created.toDate());
        Set<HistoryEntry> history = new HashSet<HistoryEntry>(1);
        history.add(historyEntry);
        doc.setHistory(history);
        setDocument(doc);
        initDefaultInitiator();
    }

    @Override
    protected void initDocument(Integer id) {
        final User currentUser = sessionManagement.getLoggedUser();
        logger.info("Open TASK[{}] by user[{}]", id, currentUser.getId());
        try {
            final Task document = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemById(id);
            if (!checkState(document, currentUser)) {
                setDocument(document);
                return;
            }
            setDocument(document);
            permissions = permissionChecker.getPermissions(sessionManagement.getAuthData(), document);
            if (isReadPermission()) {
                //Простановка факта просмотра записи
                if (sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).registerViewFact(document, currentUser)) {
                    FacesContext.getCurrentInstance().addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                taskTreeHolder.refresh();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_INITIALIZE);
            logger.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            final Task task = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(getDocument());
            if (task == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
                return false;
            } else {
                //Простановка факта просмотра записи
                sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).registerViewFact(task, sessionManagement.getLoggedUser());

                logger.debug("uploading newly created files");
                for (int i = 0; i < files.size(); i++) {
                    Attachment tmpAttachment = attachments.get(i);
                    if (tmpAttachment != null) {
                        tmpAttachment.setParentId(new String(("task_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                setDocument(task);
                taskTreeHolder.setRootDocumentId(getDocument().getUniqueId());
                return true;
            }
        } catch (Exception e) {
            logger.error("Error on save new: ", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
        }
        return false;
    }

    @Override
    protected boolean saveDocument() {
        boolean result = false;
        try {
            Task task = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(getDocument());
            if (task == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                result = true;
                taskTreeHolder.setRootDocumentId(getDocument().getUniqueId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
        }
        return result;
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    public boolean isCanDelete() {
        if (!permissions.hasPermission(WRITE)) {
            setStateComment("Доступ запрещен");
            logger.error("USER[{}] DELETE ACCESS TO TASK[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            setStateComment("Доступ запрещен");
            logger.error("USER[{}] EDIT ACCESS TO TASK[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    // END OF FILES

    @Override
    public boolean isCanView() {
        if (!permissions.hasPermission(READ)) {
            setStateComment("Доступ запрещен");
            logger.error("USER[{}] VIEW ACCESS TO TASK[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(READ);
    }

    @Override
    public String delete() {
        String in_result = super.delete();  // also call deleteDocument()
        if (in_result != null && in_result.equals(ACTION_RESULT_DELETE)) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../delete_document.xhtml");
            } catch (IOException e) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_DELETE);
                logger.error("Error on redirect: ", e);
            }
        }
        return in_result;
    }

    /**
     * Проверяем является ли документ валидным, и есть ли у пользователя к нему уровень допуска
     *
     * @param document документ для проверки
     * @return true - допуск есть
     */
    private boolean checkState(final Task document, final User user) {
        if (document == null) {
            setState(STATE_NOT_FOUND);
            logger.warn("Task NOT FOUND");
            return false;
        }
        if (document.isDeleted()) {
            setState(STATE_DELETED);
            setStateComment("Поручение удалено");
            logger.warn("TASK[{}] IS DELETED", document.getId());
            return false;
        }
        return true;
    }

    private HistoryEntry getInitialHistoryEntry(final Task doc, final Date created) {
        final HistoryEntry historyEntry = new HistoryEntry();
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
        return historyEntry;
    }

    private Task createTask(Task task) {
        try {
            Task currentTask = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(task);
            if (currentTask == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                System.out.println("uploading newly created files");
                for (int i = 0; i < files.size(); i++) {
                    Attachment tmpAttachment = attachments.get(i);
                    if (tmpAttachment != null) {
                        tmpAttachment.setParentId(new String(("task_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                return currentTask;
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
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
                    logger.error("TASK[{}] can not set initiator cause: {}", task.getId(), users == null ? "executors is null" : "too much executors. size =" + users.size()
                    );
                }
            }
            if (initiator != null) {
                task.setInitiator(initiator);
            } else {
                logger.warn("TASK[{}] Initiator not founded", task.getId());
            }
        }

    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void uploadAttachments(FileUploadDetails details) {
        try {
            if (details.getAttachment() != null) {
                Attachment attachment = details.getAttachment();
                attachment.setFileName(attachment.getFileName());
                if (getDocumentId() == null || getDocumentId() == 0) {
                    attachments.add(attachment);
                    files.add(details.getByteArray());
                } else {
                    attachment.setParentId(new String(("task_" + getDocumentId()).getBytes(), "utf-8"));
                    logger.info("result of the upload operation - " + fileManagement.createFile(attachment, details.getByteArray()));
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_ATTACH);
            logger.error("Error on attach:", e);
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
                    logger.info(
                            "result of the upload operation - " + fileManagement.createVersion(
                                    attachment, details.getByteArray(), majorVersion, details.getAttachment().getFileName()
                            )
                    );
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_ATTACH);
            logger.error("Error on attach:", e);
        }
    }
    ///////////////////////////////////////////////////////////////////////////////

    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("task_" + getDocumentId());
            if (attachments == null) {
                attachments = new ArrayList<Attachment>();
            }
        }
    }

    public void deleteAttachment(Attachment attachment) {
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
        Set<HistoryEntry> history = document.getHistory();
        history.add(historyEntry);
        document.setHistory(history);

        setDocument(document);
        if (fileManagement.deleteFile(attachment)) {
            updateAttachments();
        }
        setDocument(sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).save(document));
    }

    public VersionAppenderModal getVersionAppenderModal() {
        return versionAppenderModal;
    }
    ///////////////////////////////////////////////////////////

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

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

    public String getStateComment() {
        return stateComment;
    }

    public void setStateComment(String stateComment) {
        this.stateComment = stateComment;
    }

    public DocumentTaskTreeHolder getTaskTreeHolder() {
        return taskTreeHolder;
    }

    public void setTaskTreeHolder(DocumentTaskTreeHolder taskTreeHolder) {
        this.taskTreeHolder = taskTreeHolder;
    }

    public class VersionAppenderModal extends ModalWindowHolderBean {

        private Attachment attachment;
        private boolean majorVersion;

        public Attachment getAttachment() {
            return attachment;
        }

        public void setAttachment(Attachment attachment) {
            this.attachment = attachment;
        }

        public boolean isMajorVersion() {
            return majorVersion;
        }

        public void setMajorVersion(boolean majorVersion) {
            this.majorVersion = majorVersion;
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


    }

    public class VersionHistoryModal extends ModalWindowHolderBean {

        private Attachment attachment;
        private List<Revision> versionList;

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
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Выбора инициатора ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseInitiator() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
        final User preselected = getDocument().getInitiator();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onInitiatorChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose initiator: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final User selected = (User) result.getResult();
            getDocument().setInitiator(selected);
        }
    }

    //Выбора руководителя ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder.DIALOG_TITLE_VALUE_RESPONSIBLE));
        final User preselected = getDocument().getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onControllerChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose controller: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final User selected = (User) result.getResult();
            getDocument().setController(selected);
        }
    }
    // Выбора исполнителей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseExecutors() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_EXECUTORS));
        final List<User> preselected = getDocument().getExecutorsList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onExecutorsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        logger.info("Choose executors: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final List<User> selected = (List<User>) result.getResult();
            if(selected != null && !selected.isEmpty()) {
                getDocument().setExecutors(new HashSet<User>(selected));
            } else {
                getDocument().getExecutors().clear();
            }
        };
    }

}