package ru.efive.dms.uifaces.beans.task;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.user.UserListSelectModalBean;
import ru.efive.dms.uifaces.beans.user.UserSelectModalBean;
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

    private Permissions permissions;

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

    @Override
    public boolean isCanView() {
        if (!permissions.hasPermission(READ)) {
            setStateComment("Доступ запрещен");
            logger.error("USER[{}] VIEW ACCESS TO TASK[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(READ);
    }

    public boolean isCanExecute() {
        if (!permissions.hasPermission(EXECUTE)) {
            setStateComment("Доступ запрещен");
            logger.error("USER[{}] EXECUTE ACCESS TO TASK[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(EXECUTE);
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

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    protected void initDocument(Integer id) {
        final User currentUser = sessionManagement.getLoggedUser();
        logger.info("Open TASK[{}] by user[{}]", id, currentUser.getId());
        try {
            final Task document = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getTask(id);
            if (!checkState(document, currentUser)) {
                setDocument(document);
                return;
            }
            setDocument(document);
            permissions = permissionChecker.getPermissions(sessionManagement, document);
            if(isReadPermission()){
                //Простановка факта просмотра записи
                if(sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO).registerViewFact(document, currentUser)){
                    FacesContext.getCurrentInstance().addMessage("viewFact", MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                taskTreeHolder.changeOffset(0);
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_INITIALIZE);
            logger.error("INTERNAL ERROR ON INITIALIZATION:", e);
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
            final Task parentTask = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findDocumentById(parentId);
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
        List<DocumentForm> forms = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO)
                .findByCategoryAndDescription("Поручения", StringUtils.isEmpty(formId) ? "task" : formId);
        if (!forms.isEmpty()) {
            form = forms.get(0);
            if (form.getDescription().equals("exercise")) {
                final String exerciseTypeId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("exerciseTypeId");
                if (StringUtils.isNotEmpty(exerciseTypeId)) {
                    List<DocumentForm> exerciseTypes = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByCategoryAndValue("Задачи", exerciseTypeId);
                    DocumentForm exerciseType;
                    if (!exerciseTypes.isEmpty()) {
                        exerciseType = exerciseTypes.get(0);
                        if (exerciseType != null) {
                            doc.setExerciseType(exerciseType);
                        }
                    }
                }
            }
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
            int pos = key.indexOf('_');
            if (pos != -1) {
                String id = key.substring(pos + 1, key.length());
                //String in_type=key.substring(0,pos);
                StringBuffer in_description = new StringBuffer("");
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                if (key.contains("incoming")) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber().equals("") ?
                            "Черновик входщяего документа от " + sdf.format(in_doc.getCreationDate()) :
                            "Входящий документ № " + in_doc.getRegistrationNumber() + " от " + sdf.format(in_doc.getRegistrationDate())
                    );

                } else if (key.contains("outgoing")) {
                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber().equals("") ?
                            "Черновик исходящего документа от " + sdf.format(out_doc.getCreationDate()) :
                            "Исходящий документ № " + out_doc.getRegistrationNumber() + " от " + sdf.format(out_doc.getRegistrationDate())
                    );

                } else if (key.contains("internal")) {
                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber().equals("") ?
                            "Черновик внутреннего документа от " + sdf.format(internal_doc.getCreationDate()) :
                            "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + sdf.format(internal_doc.getRegistrationDate())
                    );

                } else if (key.contains("request")) {
                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                    in_description = new StringBuffer(request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber().equals("") ?
                            "Черновик обращения граждан от " + sdf.format(request_doc.getCreationDate()) :
                            "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + sdf.format(request_doc.getRegistrationDate())
                    );

                } else if (key.contains("task")) {
                    Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findDocumentById(id);
                    in_description = new StringBuffer(task_doc.getTaskNumber() == null || task_doc.getTaskNumber().equals("") ?
                            "Черновик поручения от " + sdf.format(task_doc.getCreationDate()) :
                            "Поручение № " + task_doc.getTaskNumber() + " от " + sdf.format(task_doc.getCreationDate())
                    );
                }
                return in_description.toString();
            }
        }
        return "";
    }

    protected void initDefaultInitiator() {
        Task task = getDocument();
        User initiator = null;
        String key = task.getRootDocumentId();
        if (key != null && !key.isEmpty()) {
            int pos = key.indexOf('_');
            if (pos != -1) {
                String id = key.substring(pos + 1, key.length());
                if (key.contains("incoming")) {
                    IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    initiator = in_doc.getController();
                } else if (key.contains("outgoing")) {
                    OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                    initiator = out_doc.getController();
                } else if (key.contains("internal")) {
                    InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                    initiator = internal_doc.getController();
                } else if (key.contains("request")) {
                    RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                    initiator = request_doc.getController();
                } else if (key.contains("task")) {
                    Task parent_task = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findDocumentById(id);
                    final List<User> users = parent_task.getExecutors();
                    if (users != null && users.size() == 1) {
                        initiator = users.iterator().next();
                    } else {
                        logger.error("TASK[{}] can not set initiator cause: {}", task.getId(), users == null ? "executors is null" : "too much executors. size =" + users.size());
                    }
                }

                if (initiator != null) {
                    task.setInitiator(initiator);
                } else {
                    logger.warn("TASK[{}] Initiator not founded", task.getId());
                }
            }
        }
    }

    // FILES

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
                    logger.info("result of the upload operation - " + fileManagement.createVersion(attachment, details.getByteArray(), majorVersion, details.getAttachment().getFileName()));
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_ATTACH);
            logger.error("Error on attach:", e);
        }
    }

    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("task_" + getDocumentId());
            if (attachments == null) attachments = new ArrayList<Attachment>();
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

    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();

    // END OF FILES

    public class VersionAppenderModal extends ModalWindowHolderBean {

        public Attachment getAttachment() {
            return attachment;
        }

        public void setAttachment(Attachment attachment) {
            this.attachment = attachment;
        }

        public void setMajorVersion(boolean majorVersion) {
            this.majorVersion = majorVersion;
        }

        public boolean isMajorVersion() {
            return majorVersion;
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

        private Attachment attachment;
        private boolean majorVersion;
    }

    public class VersionHistoryModal extends ModalWindowHolderBean {

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


        private Attachment attachment;
        private List<Revision> versionList;
    }

    public VersionAppenderModal getVersionAppenderModal() {
        return versionAppenderModal;
    }

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

    // MODAL HOLDERS

    // END OF MODAL HOLDERS


    public UserSelectModalBean getInitiatorSelectModal() {
        return initiatorSelectModal;
    }

    public UserSelectModalBean getControllerSelectModal() {
        return controllerSelectModal;
    }


    private UserSelectModalBean initiatorSelectModal = new UserSelectModalBean() {
        @Override
        protected void doShow() {
            this.setUser(getDocument().getInitiator());
            super.doShow();
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setInitiator(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            this.setUser(null);
        }
    };

    private UserSelectModalBean controllerSelectModal = new UserSelectModalBean() {
        @Override
        protected void doShow() {
            this.setUser(getDocument().getController());
            super.doShow();
        }

        @Override
        protected void doSave() {
            super.doSave();
            getDocument().setController(getUser());
        }

        @Override
        protected void doHide() {
            super.doHide();
            getUserList().setFilter("");
            getUserList().markNeedRefresh();
            this.setUser(null);
        }
    };
    ///////////////////////////////////////////////////////////////////////////////

    private UserListSelectModalBean executorsSelectModal = new UserListSelectModalBean() {
        @Override
        protected void doSave() {
            getDocument().setExecutors(new HashSet<User>(getUsers()));
            super.doSave();
        }

        @Override
        protected void doHide() {
            super.doHide();
            setUsers(null);
        }

        @Override
        protected void doShow() {
            super.doShow();
            if (getDocument() != null && getDocument().getExecutors() != null) {
                setUsers(new ArrayList<User>(getDocument().getExecutors()));
            }
        }
    };

    public UserListSelectModalBean getExecutorsSelectModal() {
        return executorsSelectModal;
    }

    public void setExecutorsSelectModal(UserListSelectModalBean executorsSelectModal) {
        this.executorsSelectModal = executorsSelectModal;
    }
    ///////////////////////////////////////////////////////////

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

    private ProcessorModalBean processorModal = new ProcessorModalBean() {
        @Override
        protected void doInit() {
            setProcessedData(getDocument());
            if (getDocumentId() == null || getDocumentId() == 0) saveNewDocument();
        }

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

    public void setStateComment(String stateComment) {
        this.stateComment = stateComment;
    }

    public String getStateComment() {
        return stateComment;
    }

    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

    private transient String stateComment;

    public DocumentTaskTreeHolder getTaskTreeHolder() {
        return taskTreeHolder;
    }
    public void setTaskTreeHolder(DocumentTaskTreeHolder taskTreeHolder) {
        this.taskTreeHolder = taskTreeHolder;
    }

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;
    @Inject
    @Named("documentTaskTree")
    private transient DocumentTaskTreeHolder taskTreeHolder;
    //Для проверки прав доступа
    @EJB
    private PermissionChecker permissionChecker;

    private static final long serialVersionUID = 4716264614655470705L;
}