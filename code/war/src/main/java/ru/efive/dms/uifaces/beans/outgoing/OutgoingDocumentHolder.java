package ru.efive.dms.uifaces.beans.outgoing;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.uifaces.beans.*;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.abstractBean.State;
import ru.efive.dms.uifaces.beans.dialogs.*;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
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

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

import static ru.efive.dms.uifaces.beans.utils.MessageHolder.*;
import static ru.efive.dms.util.security.Permissions.Permission.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.*;

@Named("out_doc")
@ViewScoped
public class OutgoingDocumentHolder extends AbstractDocumentHolderBean<OutgoingDocument> implements Serializable {

    //Именованный логгер (OUTGOING_DOCUMENT)
    private static final Logger LOGGER = LoggerFactory.getLogger("OUTGOING_DOCUMENT");
    private static final long serialVersionUID = 4716264614655470705L;
    //Для проверки прав доступа
    @Inject
    @Named("permissionChecker")
    private PermissionChecker permissionChecker;
    //TODO ACL
    private Permissions permissions;
    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();

    private DocumentSelectModal reasonDocumentSelectModal = new DocumentSelectModal() {

        @Override
        protected void doSave() {
            super.doSave();
            String viewType = this.getViewType();
            if (viewType.equals("Входящие документы")) {
                getDocument().setReasonDocumentId("incoming_" + String.valueOf(this.getIncomingDocument().getId()));
            } else if (viewType.equals("Обращения граждан")) {
                getDocument().setReasonDocumentId("request_" + String.valueOf(this.getRequestDocument().getId()));
            }
        }

        @Override
        protected void doHide() {
            super.doHide();
            this.getIncomingDocuments().setFilter("");
            this.getRequestDocuments().setFilter("");
            this.setViewTypesAlreadySelected(false);
        }
    };

    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();
    private transient String stateComment;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    @Inject
    @Named("dictionaryManagement")
    private transient DictionaryManagementBean dictionaryManagement;
    @Inject
    @Named("fileManagement")
    private transient FileManagementBean fileManagement;


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
                    history = new HashSet<HistoryEntry>();
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

    //Выбора руководителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
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
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
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
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final Contragent selected = (Contragent) result.getResult();
            getDocument().setContragent(selected);
        }
    }

    //Выбора исполнителя /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseExecutor() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
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
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final User selected = (User) result.getResult();
            getDocument().setExecutor(selected);
        }
    }

    // Выбора пользователей-читателей /////////////////////////////////////////////////////////////////////////////////////////////
    public void choosePersonReaders() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
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
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final List<User> selected = (List<User>) result.getResult();
            if(selected != null && !selected.isEmpty()) {
                getDocument().setPersonReaders(new HashSet<User>(selected));
            } else {
                getDocument().getPersonReaders().clear();
            }
        }
    }

    // Выбора пользователей-редакторов /////////////////////////////////////////////////////////////////////////////////////////////
    public void choosePersonEditors() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
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
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final List<User> selected = (List<User>) result.getResult();
            if(selected != null && !selected.isEmpty()) {
                getDocument().setPersonEditors(new HashSet<User>(selected));
            } else {
                getDocument().getPersonEditors().clear();
            }
        }
    }

    // Выбора ролей-читателей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRoleReaders() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
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
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final Set<Role> selected = (Set<Role>) result.getResult();
            if(selected != null && !selected.isEmpty()) {
                getDocument().setRoleReaders(selected);
            } else {
                getDocument().getRoleReaders().clear();
            }
        }
    }

    // Выбора ролей-редакторов /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRoleEditors() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
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
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final Set<Role> selected = (Set<Role>) result.getResult();
            if(selected != null && !selected.isEmpty()) {
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
        final OutgoingDocument document = new OutgoingDocument();
        document.setDocumentStatus(DocumentStatus.NEW);
        final LocalDateTime created = new LocalDateTime();
        document.setCreationDate(created.toDate());
        document.setAuthor(sessionManagement.getLoggedUser());

        final DocumentForm form = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO)
                .getByCode(DocumentForm.RB_CODE_OUTGOING_LETTER);

        if (form != null) {
            document.setForm(form);
        } else {
            List<DocumentForm> formList = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO)
                    .findByDocumentTypeCode(DocumentType.RB_CODE_OUTGOING);
            if (formList != null && !formList.isEmpty()) {
                document.setForm(formList.get(0));
            }
        }

        UserAccessLevel accessLevel = sessionManagement.getLoggedUser().getCurrentUserAccessLevel();
        document.setUserAccessLevel(accessLevel);

        HistoryEntry historyEntry = new HistoryEntry();
        historyEntry.setCreated(created.toDate());
        historyEntry.setStartDate(created.toDate());
        historyEntry.setOwner(sessionManagement.getLoggedUser());
        historyEntry.setDocType(document.getDocumentType().getName());
        historyEntry.setParentId(document.getId());
        historyEntry.setActionId(0);
        historyEntry.setFromStatusId(1);
        historyEntry.setEndDate(created.toDate());
        historyEntry.setProcessed(true);
        historyEntry.setCommentary("");
        Set<HistoryEntry> history = new HashSet<HistoryEntry>();
        history.add(historyEntry);
        document.setHistory(history);

        setDocument(document);
    }

    @Override
    protected void initDocument(Integer id) {
        final User currentUser = sessionManagement.getLoggedUser();
        LOGGER.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final OutgoingDocument document = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).getItemById(id);
            if (!checkState(document, currentUser)) {
                setDocument(document);
                return;
            }
            setDocument(document);
            //Проверка прав на открытие
            permissions = permissionChecker.getPermissions(sessionManagement.getAuthData(), document);
            if (isReadPermission()) {
                //Простановка факта просмотра записи
                if (sessionManagement.getDAO(ViewFactDaoImpl.class, ApplicationDAONames.VIEW_FACT_DAO).registerViewFact(document, currentUser)) {
                    FacesContext.getCurrentInstance().addMessage(MessageHolder.MSG_KEY_FOR_VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTERED);
                }
                updateAttachments();

            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_INITIALIZE);
            LOGGER.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    // END OF FILES

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            OutgoingDocument document = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).save(getDocument());
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
            } else {
                final User currentUser = sessionManagement.getLoggedUser();
                //Простановка факта просмотра документа
                sessionManagement.getDAO(ViewFactDaoImpl.class, ApplicationDAONames.VIEW_FACT_DAO).registerViewFact(document, currentUser);
                final LocalDateTime created = new LocalDateTime();
                document.setCreationDate(created.toDate());

                document.setAuthor(currentUser);

                LOGGER.debug("uploading newly created files");
                for (int i = 0; i < files.size(); i++) {
                    Attachment tmpAttachment = attachments.get(i);
                    if (tmpAttachment != null) {
                        tmpAttachment.setParentId(new String(("outgoing_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                result = true;
            }
        } catch (Exception e) {
            LOGGER.error("saveNewDocument ERROR:", e);
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_SAVE_NEW);
        }
        return result;
    }

    @Override
    protected boolean saveDocument() {
        try {
            OutgoingDocument document = getDocument();
            document = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MSG_CANT_SAVE);
                return false;
            } else {
                setDocument(document);
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("saveDocument ERROR:", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    // MODAL HOLDERS

    @Override
    protected boolean doAfterSave() {
        if (getDocument().getUserAccessLevel().getLevel() > sessionManagement.getLoggedUser().getCurrentUserAccessLevel().getLevel()) {
            //TODO
            setState(State.ERROR);
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_TRY_TO_VIEW_WITHOUT_PERMISSION);
        }
        return true;
    }

    @Override
    public boolean isCanDelete() {
        if (!permissions.hasPermission(WRITE)) {
            setStateComment("Доступ запрещен");
            LOGGER.error("USER[{}] DELETE ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanEdit() {
        if (!permissions.hasPermission(WRITE)) {
            setStateComment("Доступ запрещен");
            LOGGER.error("USER[{}] EDIT ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(WRITE);
    }

    @Override
    public boolean isCanView() {
        if (!permissions.hasPermission(READ)) {
            setStateComment("Доступ запрещен");
            LOGGER.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
        }
        return permissions.hasPermission(READ);
    }


    /**
     * Проверяем является ли документ валидным, и есть ли у пользователя к нему уровень допуска
     *
     * @param document документ для проверки
     * @return true - допуск есть
     */
    private boolean checkState(final OutgoingDocument document, final User user) {
        if (document == null) {
            setDocumentNotFound();
            LOGGER.warn("OutgoingDocument NOT FOUND");
            return false;
        }
        if (document.isDeleted()) {
            setDocumentDeleted();
            LOGGER.warn("OutgoingDocument[{}] IS DELETED", document.getId());
            return false;
        }
        final UserAccessLevel docAccessLevel = document.getUserAccessLevel();
        if (user.getCurrentUserAccessLevel() != null && docAccessLevel.getLevel() > user.getCurrentUserAccessLevel().getLevel()) {
            setState(State.ERROR);
            setStateComment("Уровень допуска к документу [" + docAccessLevel.getValue() + "] выше вашего уровня допуска.");
            LOGGER.warn(
                    "OutgoingDocument[{}] has higher accessLevel[{}] then user[{}]",
                    document.getId(),
                    docAccessLevel.getValue(),
                    user.getCurrentUserAccessLevel() != null ? user.getCurrentUserAccessLevel().getValue() : "null"
            );
            return false;
        }
        return true;
    }

    public String getLinkDescriptionByUniqueId(String documentKey) {

        if (!documentKey.isEmpty()) {
            final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(documentKey);
            if (documentKey.contains("incoming")) {
                IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber()
                        .equals("") ? "Черновик входщяего документа от " + ApplicationHelper
                        .formatDate(in_doc.getCreationDate()) : "Входящий документ № " + in_doc.getRegistrationNumber() + " от " + ApplicationHelper
                        .formatDate(in_doc.getRegistrationDate()));

            } else if (documentKey.contains("outgoing")) {
                OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber()
                        .equals("") ? "Черновик исходящего документа от " + ApplicationHelper
                        .formatDate(out_doc.getCreationDate()) : "Исходящий документ № " + out_doc
                        .getRegistrationNumber() + " от " + ApplicationHelper.formatDate(out_doc.getRegistrationDate()));

            } else if (documentKey.contains("internal")) {
                InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber()
                        .equals("") ? "Черновик внутреннего документа от " + ApplicationHelper.formatDate(
                        internal_doc.getCreationDate()
                ) : "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                        internal_doc.getRegistrationDate()
                ));

            } else if (documentKey.contains("request")) {
                RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber()
                        .equals("") ? "Черновик обращения граждан от " + ApplicationHelper.formatDate(
                        request_doc.getCreationDate()
                ) : "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                        request_doc.getRegistrationDate()
                ));

            } else if (documentKey.contains("task")) {
                Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemByIdForSimpleView(rootDocumentId);
                return (task_doc.getTaskNumber() == null || task_doc.getTaskNumber().equals("") ? "Черновик поручения от " + ApplicationHelper
                        .formatDate(task_doc.getCreationDate()) : "Поручение № " + task_doc.getTaskNumber() + " от " + ApplicationHelper.formatDate(
                        task_doc.getCreationDate()
                ));
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

    public void uploadAttachments(FileUploadDetails details) {
        try {
            if (details.getAttachment() != null) {
                Attachment attachment = details.getAttachment();
                //attachment.setFileName(new String(attachment.getFileName().getBytes(), "utf-8"));
                attachment.setFileName(attachment.getFileName());
                if (getDocumentId() == null || getDocumentId() == 0) {
                    attachments.add(attachment);
                    files.add(details.getByteArray());
                } else {
                    attachment.setParentId(new String(("outgoing_" + getDocumentId()).getBytes(), "utf-8"));
                    System.out.println("result of the upload operation - " + fileManagement.createFile(attachment, details.getByteArray()));
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_ATTACH);
            e.printStackTrace();
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
                    System.out.println(
                            "result of the upload operation - " + fileManagement.createVersion(
                                    attachment, details.getByteArray(), majorVersion, details.getAttachment().getFileName()
                            )
                    );
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MSG_ERROR_ON_ATTACH);
            e.printStackTrace();
        }
    }

    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("outgoing_" + getDocumentId());
            if (attachments == null) {
                attachments = new ArrayList<Attachment>();
            }
        }
    }

    public void deleteAttachment(Attachment attachment) {
        OutgoingDocument document = getDocument();
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

        sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).save(document);
    }

    public boolean isAgreementTreeViewAvailable() {
        try {
            int loggedUserId = sessionManagement.getLoggedUser().getId();
            if (getDocument().getAgreementTree() != null && (isViewState() || getDocument().getDocumentStatus().getId() != 1)) {
                if (loggedUserId == getDocument().getAuthor().getId() || loggedUserId == getDocument().getController().getId()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isAgreementTreeEditAvailable() {
        return getDocument().getDocumentStatus().getId() == 1 && (isEditState() || isCreateState()) && (getDocument().getAuthor()
                .getId().equals(sessionManagement.getLoggedUser().getId()));
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

    public DocumentSelectModal getReasonDocumentSelectModal() {
        return reasonDocumentSelectModal;
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

        public void saveAttachment() {
            if (attachment != null) {
                versionAttachment(fileManagement.getDetails(), attachment, majorVersion);
            }
            versionAppenderModal.save();
        }

        @Override
        protected void doShow() {
            super.doShow();
            setMajorVersion(false);
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

}