package ru.efive.dms.uifaces.beans.request;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.uifaces.beans.dialogs.*;
import ru.efive.dms.uifaces.beans.task.DocumentTaskTreeHolder;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.efive.wf.core.ActionResult;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.*;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DeliveryTypeDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;
import ru.hitsl.sql.dao.interfaces.referencebook.SenderTypeDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;

import static ru.efive.dms.util.security.Permissions.Permission.*;

@ViewScopedController(value = "request_doc", transactionManager = "ordTransactionManager")
public class RequestDocumentHolder extends AbstractDocumentHolderBean<RequestDocument> {

    @Autowired
    @Qualifier("requestDocumentDao")
    private RequestDocumentDao requestDocumentDao;
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
    @Qualifier("senderTypeDao")
    private SenderTypeDao senderTypeDao;
    @Autowired
    @Qualifier("deliveryTypeDao")
    private DeliveryTypeDao deliveryTypeDao;
    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;
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
    private List<Attachment> attachments = new ArrayList<>();
    /**
     * Связанные с этим обращением исходящие документы
     */
    private List<OutgoingDocument> relatedDocuments;
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
            RequestDocument document = (RequestDocument) actionResult.getProcessedData();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<>();
                }
                history.add(getHistoryEntry());
                document.setHistory(history);
            }
            setDocument(document);
            RequestDocumentHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            RequestDocument document = (RequestDocument) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();
            if (StringUtils.isNotEmpty(in_result)) {
                setActionResult(in_result);
            }
        }
    };


    public void handleFileUpload(FileUploadEvent event) {
        final UploadedFile file = event.getFile();
        if (file != null) {
            log.info("Upload new file[{}] content-type={} size={}", file.getFileName(), file.getContentType(), file.getSize());
            final Attachment attachment = new Attachment();
            attachment.setFileName(file.getFileName());
            attachment.setCreated(LocalDateTime.now());
            attachment.setAuthorId(authData.getAuthorized().getId());
            attachment.setParentId(getDocument().getUniqueId());
            final boolean result = fileManagement.createFile(attachment, file.getContents());
            log.info("After alfresco call Attachment.id={}", attachment.getId());
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
            params.put(
                    AttachmentVersionDialogHolder.DIALOG_DOCUMENT_KEY,
                    Collections.singletonList(getDocument().getUniqueId())
            );
            RequestContext.getCurrentInstance().openDialog("/dialogs/addVersionForAttachment.xhtml", AbstractDialog.getViewOptions(), params);
        } else {
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
        }
    }

    public void handleAddVersionDialogResult(final SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Add version dialog: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final FacesMessage msg = (FacesMessage) result.getResult();
            addMessage(MessageHolder.MSG_KEY_FOR_FILES, msg);
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

    //Выбора Региона ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRegion() {
        final Region preselected = getDocument().getRegion();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(RegionDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectRegionDialog.xhtml", AbstractDialog.getViewOptions(), null);
    }

    public void onRegionChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        log.info("Choose region: {}", result);
        if (AbstractDialog.Button.CONFIRM.equals(result.getButton())) {
            final Region selected = (Region) result.getResult();
            getDocument().setRegion(selected);
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
    protected boolean deleteDocument() {
        try {
            final boolean result = requestDocumentDao.delete(getDocument());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_DELETE);
            }
            return result;
        } catch (Exception e) {
            log.error("INTERNAL ERROR ON DELETE_DOCUMENT:", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
            return false;
        }
    }

    @Override
    protected void initNewDocument() {
        permissions = Permissions.ALL_PERMISSIONS;
        final LocalDateTime created = LocalDateTime.now();
        final User currentUser = authData.getAuthorized();
        log.info("Start initialize new document by USER[{}]", currentUser.getId());
        final RequestDocument doc = new RequestDocument();
        doc.setDocumentStatus(DocumentStatus.NEW);
        doc.setDeliveryDate(created);
        doc.setCreationDate(created);
        doc.setAuthor(currentUser);
        doc.setForm(getDefaultForm());
        doc.setDeliveryType(getDefaultDeliveryType());
        doc.setSenderType(getDefaultSenderType());
        setDocument(doc);
    }


    @Override
    protected void initDocument(Integer id) {
        final User currentUser = authData.getAuthorized();
        log.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final RequestDocument document = requestDocumentDao.get(id);
            setDocument(document);
            if (!checkState(document, currentUser)) {
                return;
            }
            setDocument(document);
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
                    addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                //Установка идшника для поиска поручений
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                //Поиск поручений
                taskTreeHolder.refresh();
                try {
                    updateAttachments();
                } catch (Exception e) {
                    log.warn("Exception while check upload files", e);
                    addMessage(MessageHolder.MSG_KEY_FOR_FILES, MessageHolder.MSG_ERROR_ON_ATTACH);
                }
            }
        } catch (Exception e) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_INITIALIZE);
            log.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    @Override
    public boolean saveNewDocument() {
        final User currentUser = authData.getAuthorized();
        final LocalDateTime created = LocalDateTime.now();
        log.info("Save new document by USER[{}]", currentUser.getId());
        // Сохранение дока в БД и создание записи в истории о создании
        final RequestDocument document = getDocument();
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
            requestDocumentDao.save(document);
        } catch (Exception e) {
            log.error("saveNewDocument: on save document", e);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
            return false;
        }
        //Простановка факта просмотра записи
        try {
            viewFactDao.registerViewFact(document, currentUser);
        } catch (Exception e) {
            log.error("saveNewDocument: on viewFact register", e);
            addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
        }
        //Установка идшника для поиска поручений
        taskTreeHolder.setRootDocumentId(document.getUniqueId());
        return true;
    }

    @Override
    protected boolean saveDocument() {
        final User currentUser = authData.getAuthorized();
        log.info("Save document by USER[{}]", currentUser.getId());
        final RequestDocument document = getDocument();
        try {
            requestDocumentDao.update(document);
            //Установка идшника для поиска поручений
            taskTreeHolder.setRootDocumentId(document.getUniqueId());
            return true;
        } catch (Exception e) {
            log.error("saveDocument ERROR:", e);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    public boolean isCanDelete() {
        if (!permissions.hasPermission(WRITE)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_EDIT_WITHOUT_PERMISSION);
            log.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanView() {
        if (permissions == null || !permissions.hasPermission(READ)) {
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
            log.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", authData.getAuthorized().getId(), getDocumentId());
            return false;
        }
        return true;
    }

    public DocumentForm getDefaultForm() {
        final DocumentForm form = documentFormDao.getByCode(DocumentForm.RB_CODE_REQUEST_TREATMENT_CLAIM);
        if (form != null) {
            return form;
        } else {
            final List<DocumentForm> formList = documentFormDao.findByDocumentTypeCode(DocumentType.RB_CODE_REQUEST);
            if (!formList.isEmpty()) {
                return formList.get(0);
            } else {
                return null;
            }
        }
    }

    private SenderType getDefaultSenderType() {
        final SenderType senderType = senderTypeDao.getByCode(SenderType.PHYSICAL_ENTITY);
        if (senderType != null) {
            return senderType;
        } else {
            final List<SenderType> senderTypes = senderTypeDao.getItems();
            if (senderTypes != null && !senderTypes.isEmpty()) {
                return senderTypes.get(0);
            } else {
                return null;
            }
        }
    }

    private DeliveryType getDefaultDeliveryType() {
        final DeliveryType deliveryType = deliveryTypeDao.getByCode(DeliveryType.RB_CODE_EMAIL);
        if (deliveryType != null) {
            return deliveryType;
        } else {
            final List<DeliveryType> deliveryTypes = deliveryTypeDao.getItems();
            if (deliveryTypes != null && !deliveryTypes.isEmpty()) {
                return deliveryTypes.get(0);
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
    private boolean checkState(final RequestDocument document, final User user) {
        if (document == null) {
            setDocumentNotFound();
            log.warn("Document NOT FOUND");
            return false;
        }
        if (document.isDeleted()) {
            setDocumentDeleted();
            log.warn("Document[{}] IS DELETED", document.getId());
            return false;
        }
        return true;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void updateAttachments() {
        log.debug("Start updating attachments");
        if (getDocument() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId(getDocument().getUniqueId());
        }
        log.debug("Finish updating attachments");
    }

    public void deleteAttachment(Attachment attachment) {
        if (attachment != null && isCanEdit()) {
            final RequestDocument document = getDocument();
            final LocalDateTime created = LocalDateTime.now();
            ;
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
            setDocument(requestDocumentDao.save(document));
        }
    }

    /**
     * Найти в БД все исходящие документы для нашего входящего
     *
     * @return список исходящих документов \ пустой список
     */
    public List<OutgoingDocument> loadRelatedDocuments() {
        if (StringUtils.isNotEmpty(getDocument().getUniqueId())) {
            return outgoingDocumentDao.findAllDocumentsByReasonDocumentId(getDocument().getUniqueId());
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

    public ProcessorModalBean getProcessorModal() {
        return processorModal;
    }

}