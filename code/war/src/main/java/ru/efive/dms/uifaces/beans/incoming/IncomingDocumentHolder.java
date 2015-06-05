package ru.efive.dms.uifaces.beans.incoming;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dao.alfresco.Revision;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.efive.dms.uifaces.beans.FileManagementBean.FileUploadDetails;
import ru.efive.dms.uifaces.beans.ProcessorModalBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.dialogs.*;
import ru.efive.dms.uifaces.beans.task.DocumentTaskTreeHolder;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.dms.util.security.PermissionChecker;
import ru.efive.dms.util.security.Permissions;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
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

import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.security.Permissions.Permission.*;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.*;

@Named("in_doc")
@ViewScoped
public class IncomingDocumentHolder extends AbstractDocumentHolderBean<IncomingDocument, Integer> {
    //Именованный логгер (INCOMING_DOCUMENT)
    private static final Logger LOGGER = LoggerFactory.getLogger("INCOMING_DOCUMENT");

    //TODO ACL
    private Permissions permissions;

    private transient String stateComment;
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
    @EJB
    private PermissionChecker permissionChecker;

    private List<Attachment> attachments = new ArrayList<Attachment>();
    private List<byte[]> files = new ArrayList<byte[]>();
    private VersionAppenderModal versionAppenderModal = new VersionAppenderModal();
    private VersionHistoryModal versionHistoryModal = new VersionHistoryModal();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Выбора руководителя ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(UserDialogHolder.DIALOG_TITLE_VALUE_CONTROLLER));
        params.put(UserDialogHolder.DIALOG_GROUP_KEY, ImmutableList.of(Group.RB_CODE_MANAGERS));
        final User preselected = getDocument().getController();
        if (preselected != null) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(UserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onControllerChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose controller: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
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
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectContragentDialog.xhtml", AbstractDialog.getViewParams(), null);
    }

    public void onContragentChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose contragent: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final Contragent selected = (Contragent) result.getResult();
            getDocument().setContragent(selected);
        }
    }

    // Выбора адресатов-пользователей /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRecipients() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(MultipleUserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleUserDialogHolder.DIALOG_TITLE_VALUE_RECIPIENTS));
        final List<User> preselected = getDocument().getRecipientUserList();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleUserDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onRecipientsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose recipients: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final List<User> selected = (List<User>) result.getResult();
            if(selected != null && !selected.isEmpty()) {
                getDocument().setRecipientUsers(new HashSet<User>(selected));
            } else {
                getDocument().getRecipientUsers().clear();
            }
        }
    }

    // Выбора адресатов-групп /////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseRecipientGroups() {
        final Map<String, List<String>> params = new HashMap<String, List<String>>();
        params.put(MultipleGroupDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, ImmutableList.of(MultipleGroupDialogHolder.DIALOG_TITLE_VALUE_RECIPIENTS));
        final Set<Group> preselected = getDocument().getRecipientGroups();
        if (preselected != null && !preselected.isEmpty()) {
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(MultipleGroupDialogHolder.DIALOG_SESSION_KEY, preselected);
        }
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleGroupDialog.xhtml", AbstractDialog.getViewParams(), params);
    }

    public void onRecipientGroupsChosen(SelectEvent event) {
        final AbstractDialog.DialogResult result = (AbstractDialog.DialogResult) event.getObject();
        LOGGER.info("Choose recipientGroups: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final Set<Group> selected = (Set<Group>) result.getResult();
            if(selected != null && !selected.isEmpty()) {
                getDocument().setRecipientGroups(selected);
            } else {
                getDocument().getRecipientGroups().clear();
            }
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
        LOGGER.info("Choose executors: {}", result);
        if(AbstractDialog.Button.CONFIRM.equals(result.getButton())){
            final List<User> selected = (List<User>) result.getResult();
            if(selected != null && !selected.isEmpty()) {
                getDocument().setExecutors(new HashSet<User>(selected));
            } else {
                getDocument().getExecutors().clear();
            }
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
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), params);
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
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleUserDialog.xhtml", AbstractDialog.getViewParams(), params);
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
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleRoleDialog.xhtml", AbstractDialog.getViewParams(), params);
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
        RequestContext.getCurrentInstance().openDialog("/dialogs/selectMultipleRoleDialog.xhtml", AbstractDialog.getViewParams(), params);
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
            IncomingDocument document = (IncomingDocument) actionResult.getProcessedData();
            HistoryEntry historyEntry = getHistoryEntry();
            if (getSelectedAction().isHistoryAction()) {
                Set<HistoryEntry> history = document.getHistory();
                if (history == null) {
                    history = new HashSet<HistoryEntry>();
                }
                history.add(historyEntry);
                document.setHistory(history);
            }
            setDocument(document);
            IncomingDocumentHolder.this.save();
        }

        @Override
        protected void doProcessException(ActionResult actionResult) {
            IncomingDocument document = (IncomingDocument) actionResult.getProcessedData();
            String in_result = document.getWFResultDescription();
            if (StringUtils.isNotEmpty(in_result)) {
                setActionResult(in_result);
            }
        }


    };

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
            final boolean result = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).delete(getDocumentId());
            if (!result) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_DELETE);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("INTERNAL ERROR ON DELETE_DOCUMENT:", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_DELETE);
            return false;
        }
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }

    @Override
    protected void initNewDocument() {
        permissions = Permissions.ALL_PERMISSIONS;
        IncomingDocument doc = new IncomingDocument();
        doc.setDocumentStatus(DocumentStatus.NEW);
        final Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
        doc.setDeliveryDate(created);
        doc.setCreationDate(created);
        doc.setAuthor(sessionManagement.getLoggedUser());

        String parentNumeratorId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
        if ((parentNumeratorId != null) && (StringUtils.isNotEmpty(parentNumeratorId))) {
            //doc.setTemplateFlag(true);
            //String parentNumeratorId=FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("parentId");
            doc.setParentNumeratorId(parentNumeratorId);
            //Numerator parentNumerator=sessionManagement.getDAO(NumeratorDAOImpl.class,NUMERATOR_DAO).findDocumentById(parentNumeratorId);
        }
        final DocumentForm form  = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).getByCode(DocumentForm.RB_CODE_INCOMING_LETTER);
        if(form != null) {
            doc.setForm(form);
        }
        else {
            final List<DocumentForm> formList = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO)
                    .findByDocumentTypeCode(DocumentType.RB_CODE_INCOMING);
            if (!formList.isEmpty()) {
                doc.setForm(formList.get(0));
            }
        }
        doc.setUserAccessLevel(sessionManagement.getLoggedUser().getCurrentUserAccessLevel());

        HistoryEntry historyEntry = new HistoryEntry();
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
        doc.addToHistory(historyEntry);
        setDocument(doc);
    }

    @Override
    protected void initDocument(final Integer id) {
        final User currentUser = sessionManagement.getLoggedUser();
        LOGGER.info("Open Document[{}] by user[{}]", id, currentUser.getId());
        try {
            final IncomingDocument document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).getItemById(id);
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
                //Установка идшника для поиска поручений
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                //Поиск поручений
                taskTreeHolder.refresh();
                try {
                    updateAttachments();
                } catch (Exception e) {
                    LOGGER.warn("Exception while check upload files", e);
                }
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_INITIALIZE);
            LOGGER.error("INTERNAL ERROR ON INITIALIZATION:", e);
        }
    }

    @Override
    protected boolean saveNewDocument() {
        boolean result = false;
        try {
            IncomingDocument document = getDocument();
            document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
            } else {
                final User currentUser = sessionManagement.getLoggedUser();
                //Простановка факта просмотра записи
                sessionManagement.getDAO(ViewFactDaoImpl.class, ApplicationDAONames.VIEW_FACT_DAO).registerViewFact(document, currentUser);

                Date created = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
                document.setCreationDate(created);

                document.setAuthor(currentUser);

                PaperCopyDocument paperCopy = new PaperCopyDocument();
                paperCopy.setDocumentStatus(DocumentStatus.NEW);
                paperCopy.setCreationDate(created);
                paperCopy.setAuthor(currentUser);

                String parentId = document.getUniqueId();
                if (StringUtils.isNotEmpty(parentId)) {
                    paperCopy.setParentDocumentId(parentId);
                }

                paperCopy.setRegistrationNumber(".../1");
                HistoryEntry historyEntry = new HistoryEntry();
                historyEntry.setCreated(created);
                historyEntry.setStartDate(created);
                historyEntry.setOwner(currentUser);
                historyEntry.setDocType(paperCopy.getDocumentType().getName());
                historyEntry.setParentId(paperCopy.getId());
                historyEntry.setActionId(0);
                historyEntry.setFromStatusId(1);
                historyEntry.setEndDate(created);
                historyEntry.setProcessed(true);
                historyEntry.setCommentary("");
                Set<HistoryEntry> history = new HashSet<HistoryEntry>();
                history.add(historyEntry);
                paperCopy.setHistory(history);

                paperCopy.setParentDocument(document);

                sessionManagement.getDAO(PaperCopyDocumentDAOImpl.class, PAPER_COPY_DOCUMENT_FORM_DAO).save(paperCopy);

                LOGGER.debug("uploading newly created files");
                for (int i = 0; i < files.size(); i++) {
                    Attachment tmpAttachment = attachments.get(i);
                    if (tmpAttachment != null) {
                        tmpAttachment.setParentId(new String(("incoming_" + getDocumentId()).getBytes(), "utf-8"));
                        fileManagement.createFile(tmpAttachment, files.get(i));
                    }
                }
                result = true;
                //Установка идшника для поиска поручений
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
            }
        } catch (Exception e) {
            LOGGER.error("saveNewDocument ERROR:", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE_NEW);
        }
        return result;
    }

    @Override
    protected boolean saveDocument() {
        try {
            IncomingDocument document = getDocument();
            document = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).save(document);
            if (document == null) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_SAVE);
                return false;
            } else {
                setDocument(document);
                //Установка идшника для поиска поручений
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("saveDocument ERROR:", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_SAVE);
            return false;
        }
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }

    @Override
    protected String doAfterSave() {
        if (getDocument().getUserAccessLevel().getLevel() > sessionManagement.getLoggedUser().getCurrentUserAccessLevel().getLevel()) {
            setState(STATE_FORBIDDEN);
        }
        return super.doAfterSave();
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
        if (permissions==null || !permissions.hasPermission(READ)) {
            setStateComment("Доступ запрещен");
            LOGGER.error("USER[{}] VIEW ACCESS TO DOCUMENT[{}] FORBIDDEN", sessionManagement.getLoggedUser().getId(), getDocumentId());
            return false;
        }
        return true;
    }

    @Override
    public String delete() {
        String in_result = super.delete();
        if (AbstractDocumentHolderBean.ACTION_RESULT_DELETE.equals(in_result)) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("../delete_document.xhtml");
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CANT_DELETE);
                LOGGER.error("INTERNAL ERROR ON DELETE:", e);
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
    private boolean checkState(final IncomingDocument document, final User user) {
        if (document == null) {
            setState(STATE_NOT_FOUND);
            LOGGER.warn("Document NOT FOUND");
            return false;
        }
        if (document.isDeleted()) {
            setState(STATE_DELETED);
            setStateComment("Документ удален");
            LOGGER.warn("Document[{}] IS DELETED", document.getId());
            return false;
        }
        final UserAccessLevel docAccessLevel = document.getUserAccessLevel();
        if (user.getCurrentUserAccessLevel() != null && docAccessLevel.getLevel() > user.getCurrentUserAccessLevel().getLevel()) {
            setState(STATE_FORBIDDEN);
            setStateComment("Уровень допуска к документу [" + docAccessLevel.getValue() + "] выше вашего уровня допуска.");
            LOGGER.warn("Document[{}] has higher accessLevel[{}] then user[{}]",
                    document.getId(),
                    docAccessLevel.getValue(),
                    user.getCurrentUserAccessLevel() != null ? user.getCurrentUserAccessLevel().getValue() : "null"
            );
            return false;
        }
        return true;
    }

    protected boolean validateHolder() {
        boolean result = true;
        FacesContext context = FacesContext.getCurrentInstance();
        if (getDocument().getController() == null) {
            context.addMessage(null, MessageHolder.MSG_CONTROLLER_NOT_SET);
            result = false;
        }
        if (getDocument().getContragent() == null) {
            context.addMessage(null, MessageHolder.MSG_CONTRAGENT_NOT_SET);
            result = false;
        }
        if (getDocument().getRecipientUsers() == null || getDocument().getRecipientUsers().isEmpty()) {
            context.addMessage(null, MessageHolder.MSG_RECIPIENTS_NOT_SET);
            result = false;
        }
        if (getDocument().getShortDescription() == null || getDocument().getShortDescription().equals("")) {
            context.addMessage(null, MessageHolder.MSG_SHORT_DESCRIPTION_NOT_SET);
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
                    attachment.setParentId(new String(("incoming_" + getDocumentId()).getBytes(), "utf-8"));
                    final boolean result = fileManagement.createFile(attachment, details.getByteArray());
                    LOGGER.info("result of the upload operation - {}", result);
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_ATTACH);
            LOGGER.error("uploadAttachments ERROR:", e);
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
                    final boolean result = fileManagement.createVersion(
                            attachment,
                            details.getByteArray(),
                            majorVersion,
                            details.getAttachment().getFileName()
                    );
                    LOGGER.info("result of the upload operation - {}", result);
                    updateAttachments();
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_ERROR_ON_ATTACH);
            LOGGER.error("versionAttachment ERROR:", e);
        }
    }

    public void updateAttachments() {
        if (getDocumentId() != null && getDocumentId() != 0) {
            attachments = fileManagement.getFilesByParentId("incoming_" + getDocumentId());
            if (attachments == null) {
                attachments = new ArrayList<Attachment>();
            }
        }
    }

    public void deleteAttachment(Attachment attachment) {
        if (attachment != null) {
            IncomingDocument document = getDocument();
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
            setDocument(sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).save(document));
        }
    }

    public List<String> getRelatedDocumentsUniqueId() {
        List<String> ids = new ArrayList<String>();
        String key = "incoming_" + getDocument().getId();
        if (!key.isEmpty()) {
            List<OutgoingDocument> out_docs = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO)
                    .findAllDocumentsByReasonDocumentId(key);
            for (OutgoingDocument out_doc : out_docs) {
                ids.add(out_doc.getUniqueId());
            }
        }
        return ids;
    }

    public String getLinkDescriptionByUniqueId(String key) {
        if (!key.isEmpty()) {
            final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(key);
            if (rootDocumentId == null) {
                return "";
            }
            if (key.contains("incoming")) {
                IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber()
                        .equals("") ? "Черновик входщяего документа от " + ApplicationHelper
                        .formatDate(in_doc.getCreationDate()) : "Входящий документ № " + in_doc.getRegistrationNumber() + " от " + ApplicationHelper
                        .formatDate(in_doc.getRegistrationDate()));

            } else if (key.contains("outgoing")) {
                OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber()
                        .equals("") ? "Черновик исходящего документа от " + ApplicationHelper
                        .formatDate(out_doc.getCreationDate()) : "Исходящий документ № " + out_doc
                        .getRegistrationNumber() + " от " + ApplicationHelper.formatDate(out_doc.getRegistrationDate()));

            } else if (key.contains("internal")) {
                InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber()
                        .equals("") ? "Черновик внутреннего документа от " + ApplicationHelper.formatDate(
                        internal_doc.getCreationDate()
                ) : "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                        internal_doc.getRegistrationDate()
                ));

            } else if (key.contains("request")) {
                RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO)
                        .getItemByIdForSimpleView(rootDocumentId);
                return (request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber()
                        .equals("") ? "Черновик обращения граждан от " + ApplicationHelper.formatDate(
                        request_doc.getCreationDate()
                ) : "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                        request_doc.getRegistrationDate()
                ));

            } else if (key.contains("task")) {
                Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).getItemByIdForSimpleView(rootDocumentId);
                return (task_doc.getTaskNumber() == null || task_doc.getTaskNumber().equals("") ? "Черновик поручения от " + ApplicationHelper
                        .formatDate(task_doc.getCreationDate()) : "Поручение № " + task_doc.getTaskNumber() + " от " + ApplicationHelper.formatDate(
                        task_doc.getCreationDate()
                ));
            }

        }
        return "";
    }

    public DocumentTaskTreeHolder getTaskTreeHolder() {
        return taskTreeHolder;
    }

    public void setTaskTreeHolder(DocumentTaskTreeHolder taskTreeHolder) {
        this.taskTreeHolder = taskTreeHolder;
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

        @Override
        protected void doShow() {
            super.doShow();
            setMajorVersion(false);
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

        @Override
        protected void doShow() {
            super.doShow();
            versionList = fileManagement.getVersionHistory(attachment);
        }

        public List<Revision> getVersionList() {
            return versionList == null ? new ArrayList<Revision>() : versionList;
        }

        @Override
        protected void doHide() {
            super.doHide();
            versionList = null;
            attachment = null;
        }
    }
}