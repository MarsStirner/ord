package ru.efive.dms.uifaces.beans.abstractBean;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.springframework.transaction.annotation.Transactional;
import ru.bars_open.medvtr.ord.cmis.CmisDao;
import ru.efive.dms.uifaces.beans.dialogs.AbstractDialog;
import ru.efive.dms.uifaces.beans.dialogs.UserDialogHolder;
import ru.efive.dms.uifaces.beans.task.DocumentTaskTreeHolder;
import ru.efive.dms.uifaces.beans.utils.ReferenceBookHelper;
import ru.efive.dms.uifaces.beans.workflow.NumerationService;
import ru.efive.dms.util.message.MessageHolder;
import ru.efive.dms.util.message.MessageKey;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.Task;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.context.FacesContext;
import java.time.LocalDateTime;
import java.util.*;


public abstract class AbstractDocumentLazyTabHolder<I extends DocumentEntity, D extends DocumentDao<I>>
        extends AbstractDocumentHolderBean<I, D>
        implements TaskTreeHolder{

    public static final String TAB_ID_TASK = "tab_task";
    public static final String TAB_ID_RELATION = "tab_relation";
    public static final String TAB_ID_ATTACHMENTS = "tab_files";


    private final CmisDao cmisDao;
    protected final DocumentTaskTreeHolder taskTreeHolder;
    protected final ReferenceBookHelper referenceBookHelper;
    protected final ViewFactDao viewFactDao;
    protected final NumerationService numerationService;
    protected List<DocumentEntity> additionalRelatedDocuments = new ArrayList<>();

    private Map<String, Boolean> initialized = new HashMap<>(3);

    private AttachmentHolder attachmentHolder;


    /**
     * Документы имеющие связь основным документом (кроме поручений)
     */
    private List<DocumentEntity> relatedDocuments;


    public AbstractDocumentLazyTabHolder(
            final D dao,
            final AuthorizationData authData,
            final CmisDao cmisDao,
            final DocumentTaskTreeHolder taskTreeHolder,
            final ReferenceBookHelper referenceBookHelper,
            final ViewFactDao viewFactDao,
            final NumerationService numerationService) {
        super(dao, authData);
        this.cmisDao = cmisDao;
        this.taskTreeHolder = taskTreeHolder;
        this.referenceBookHelper = referenceBookHelper;
        this.viewFactDao = viewFactDao;
        this.numerationService = numerationService;
    }


    @Transactional(transactionManager = "ordTransactionManager")
    public void onTabChange(final TabChangeEvent event) {
        final String tabId = event.getTab().getId();
        log.debug("Tab changed: Tab[{}]['{}']", tabId, event.getTab().getTitle());
        if (!initialized.getOrDefault(tabId, false)) {
            log.debug("Tab not fetched, start");
            initialized.put(tabId, loadTab(tabId, getDocument(), authData));
            log.trace("Tab fetch complete");
        } else {
            log.trace("Tab is already fetched");
        }
    }


    public AttachmentHolder getAttachmentHolder() {
        return attachmentHolder;
    }

    /**
     * Загрузить и закешировать вкладку с указанным идентифактором
     *
     * @param tabId    идентифкатор вкладки
     * @param document
     * @param authData
     * @return флаг успешности загрузки\кеширования
     * @see AbstractDocumentLazyTabHolder#TAB_ID_ATTACHMENTS
     * @see AbstractDocumentLazyTabHolder#TAB_ID_RELATION
     * @see AbstractDocumentLazyTabHolder#TAB_ID_TASK
     */
    protected boolean loadTab(final String tabId, final I document, final AuthorizationData authData) {
        switch (tabId) {
            case TAB_ID_ATTACHMENTS: {
                // Найти в БД все исходящие документы для нашего входящего
                if (StringUtils.isNotEmpty(document.getUniqueId())) {
                    attachmentHolder = new AttachmentHolderImpl(cmisDao, authData, document.getUniqueId());
                    attachmentHolder.refresh();
                }
                //NOT CACHE
                return false;
            }
            case TAB_ID_RELATION: {
                // Найти в БД все исходящие документы для нашего входящего
                relatedDocuments = loadRelatedDocuments(document, authData);
                //CACHE
                return true;
            }
            case TAB_ID_TASK: {
                taskTreeHolder.setRootDocumentId(document.getUniqueId());
                taskTreeHolder.refresh(true);
                //NOT CACHE
                return false;

            }
            default: {
                return loadUncommonTab(tabId, document, authData);
            }
        }
    }

    public boolean loadUncommonTab(String tabId, I document, AuthorizationData authData) {
        log.warn("Unknown tabId to fetch [{}]", tabId);
        return false;
    }


    public List<DocumentEntity> loadRelatedDocuments(I document, AuthorizationData authData) {
        return new ArrayList<>(0);
    }

    public void refreshRelatedDocuments() {
        relatedDocuments = loadRelatedDocuments(getDocument(), authData);
    }

    public List<DocumentEntity> getRelatedDocuments() {
        return relatedDocuments;
    }

    public void addNewRelatedDocument(DocumentEntity relatedDocument) {
        if (!relatedDocuments.contains(relatedDocument)) {
            additionalRelatedDocuments.add(relatedDocument);
            relatedDocuments.add(relatedDocument);
        }
        FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove("relatedDocument");
    }

    @Override
    public DocumentTaskTreeHolder getTaskTree() {
        return taskTreeHolder;
    }

    public List<Task> getTaskListByRootDocumentId(String uniqueId, boolean showDeleted) {
        return taskTreeHolder.getFlatList(uniqueId, showDeleted);
    }


    @Override
    public boolean beforeDelete(I document, AuthorizationData authData) {
        boolean result = true;
        final List<DocumentEntity> relatedDocuments = loadRelatedDocuments(document, authData);
        for (DocumentEntity related : relatedDocuments) {
            MessageUtils.addMessage(
                    MessageHolder.MSG_CANT_DELETE_EXISTS_LINK_WITH_OTHER_DOCUMENT,
                    referenceBookHelper.getLinkDescriptionByUniqueId(related.getUniqueId())
            );
            result = false;
        }
        final List<Task> taskList = getTaskListByRootDocumentId(document.getUniqueId(), false);
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
    public boolean beforeCreate(I document, AuthorizationData authData) {
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
    public boolean afterCreate(final I document, final AuthorizationData authData) {
        //Простановка факта просмотра записи
        try {
            viewFactDao.registerViewFact(document, authData.getAuthorized());
        } catch (Exception e) {
            log.error("createModel: on viewFact register", e);
            MessageUtils.addMessage(MessageKey.VIEW_FACT, MessageHolder.MSG_VIEW_FACT_REGISTRATION_ERROR);
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Диалоговые окошки  /////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    //Выбора руководителя ////////////////////////////////////////////////////////////////////////////////////////////////////
    public void chooseController() {
        final Map<String, List<String>> params = new HashMap<>();
        params.put(UserDialogHolder.DIALOG_TITLE_GET_PARAM_KEY, Collections.singletonList(UserDialogHolder.DIALOG_TITLE_VALUE_RESPONSIBLE));
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

    @Override
    public boolean afterDelete(I document, AuthorizationData authData) {
        numerationService.freeNumeration(document);
        return super.afterDelete(document, authData);
    }
}
