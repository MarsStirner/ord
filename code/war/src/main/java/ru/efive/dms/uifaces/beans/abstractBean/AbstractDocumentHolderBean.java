package ru.efive.dms.uifaces.beans.abstractBean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.mapped.IdentifiedEntity;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;

/**
 * Абстарктный бин-обработчик страницы документа (Serializable)
 *
 * @param <D> тип документа (должен наследовать от IdentifiedEntity)
 */
@Transactional("ordTransactionManager")
public abstract class AbstractDocumentHolderBean<D extends IdentifiedEntity> implements Serializable {
    /**
     * Название GET-параметра, определяющего идентифкатор документа
     */
    public static final String REQUEST_PARAM_DOC_ID = "docId";
    /**
     * Название GET-параметра, определяющего действие над документом
     */
    public static final String REQUEST_PARAM_DOC_ACTION = "docAction";
    /**
     * Значение GET-параметра REQUEST_PARAM_DOC_ACTION, означающее создание нового документа
     */
    public static final String REQUEST_PVALUE_DOC_ACTION_CREATE = "create";
    /**
     * Значение GET-параметра REQUEST_PARAM_DOC_ACTION, означающее редактирование документа
     */
    public static final String REQUEST_PVALUE_DOC_ACTION_EDIT = "edit";
    /**
     * Абстрактный логгер для документа
     */
    private static final Logger logger = LoggerFactory.getLogger("DOCUMENT");
    /**
     * Поле, где будет храниться сам документ
     */
    private D document;
    /**
     * Текущее состояние бина-обработчика
     */
    private State state;

    /**
     * Удаление документа (из БД или только флаг решается в реализации)
     *
     * @return упсешность удаления
     */
    protected abstract boolean deleteDocument();

    /**
     * @return
     */
    protected Integer getDocumentId() {
        return getDocument() == null ? null : getDocument().getId();
    }

    protected abstract void initNewDocument();

    protected abstract void initDocument(Integer documentId);

    protected abstract boolean saveNewDocument();

    protected abstract boolean saveDocument();

    // ----------------------------------------------------------------------------------------------------------------

    protected boolean doAfterCreate() {
        return true;
    }

    protected boolean doAfterDelete() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("/component/delete_document.xhtml");
        } catch (IOException e) {
            logger.error("Error on redirect", e);
            return false;
        }
        return true;
    }

    protected boolean doAfterEdit() {
        return true;
    }

    protected boolean doAfterError() {
        return true;
    }

    protected boolean doAfterSave() {
        return true;
    }

    protected boolean doAfterView() {
        return true;
    }

    protected Integer getInitialDocumentId() {
        final String docId = getRequestParamByName(REQUEST_PARAM_DOC_ID);
        if (StringUtils.isNotEmpty(docId)) {
            try {
                return Integer.valueOf(docId);
            } catch (NumberFormatException ex) {
                logger.error("Cannot convert docId[{}] to integer", docId);
                addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOC_ID_CONVERSION_ERROR);
                return null;
            }
        } else {
            logger.error("docId is empty or null");
            addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_NO_DOC_ID);
            return null;
        }
    }

    protected void addMessage(final FacesMessage message) {
        addMessage(null, message);
    }

    protected void addMessage(final String tag, final FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(tag, message);
    }

    /**
     * returns GET param value from URL by his name
     *
     * @param paramName name of the GET parameter
     * @return String value of named get parameter, if not set returns null
     */
    public String getRequestParamByName(final String paramName) {
        return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(paramName);
    }

    public boolean isCanCreate() {
        return true;
    }

    public boolean isCanDelete() {
        return !isErrorState();
    }

    public boolean isCanEdit() {
        return !isErrorState();
    }

    public boolean isCanView() {
        return !isErrorState();
    }

    // ----------------------------------------------------------------------------------------------------------------

    public boolean cancel() {
        if (isErrorState()) {
            return doAfterError();
        }
        return view();
    }

    public boolean create() {
        if (isCanCreate()) {
            initNewDocument();
        }
        if (!isErrorState()) {
            setState(State.CREATE);
            return doAfterCreate();
        } else {
            return doAfterError();
        }
    }

    public boolean delete() {
        if (isErrorState()) {
            return doAfterError();
        } else if (isCanDelete() && deleteDocument()) {
            setState(null);
            document = null;
            return doAfterDelete();
        } else {
            // If this instructions are executed then deleteDocument() is returned <code>false</code> and it should
            // add error messages to context themself.
            return false;
        }
    }

    public boolean edit() {
        if (isErrorState() || !isCanEdit()) {
            return doAfterError();
        } else {
            setState(State.EDIT);
            return doAfterEdit();
        }
    }

    public boolean save() {
        if (isErrorState() || !isCanEdit()) {
            return doAfterError();
        }
        boolean success;
        if (isCreateState()) {
            success = saveNewDocument();
        } else {
            success = saveDocument();
        }
        if (success) {        // should add error messages to context themself. (For example, concurrent modify).
            setState(State.VIEW);
            return doAfterSave();
        }
        return false;
    }

    public boolean view() {
        initDocument(getDocumentId());
        if (!isErrorState()) {
            setState(State.VIEW);
            return doAfterView();
        } else {
            return doAfterError();
        }
    }


    public void init() {
        logger.info("Initialize new HolderBean");
        final String action = getRequestParamByName(REQUEST_PARAM_DOC_ACTION);
        //CREATE
        if (REQUEST_PVALUE_DOC_ACTION_CREATE.equals(action)) {
            if (isCanCreate()) {
                setState(State.CREATE);
                initNewDocument();
                return;
            } else {
                setState(State.ERROR);
                return;
            }
        }
        //EDIT OR VIEW EXISTING DOCUMENT
        Integer id = getInitialDocumentId();
        if (id != null) {
            initDocument(id);
        }
        if (document == null) {
            setState(State.ERROR);
        } else if (REQUEST_PVALUE_DOC_ACTION_EDIT.equals(action)) {
            setState(isCanEdit() ? State.EDIT : State.ERROR);
        } else {
            setState(isCanView() ? State.VIEW : State.ERROR);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public boolean isCreateState() {
        return State.CREATE.equals(state);
    }

    public boolean isEditState() {
        return State.EDIT.equals(state);
    }

    public boolean isViewState() {
        return State.VIEW.equals(state);
    }

    public boolean isErrorState() {
        return State.ERROR.equals(state);
    }

    public D getDocument() {
        return document;
    }

    protected void setDocument(D document) {
        this.document = document;
    }

    public State getState() {
        return state;
    }

    protected void setState(final State state) {
        logger.info("Document state changed from {} to {}", this.state, state);
        this.state = state;
    }

    /**
     * Стандартные действия когда документ не был найден
     */
    protected void setDocumentNotFound() {
        setState(State.ERROR);
        addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_NOT_FOUND);
    }

    /**
     * Стандартные действия когда документ не был найден
     */
    protected void setDocumentDeleted() {
        setState(State.ERROR);
        addMessage(MessageHolder.MSG_KEY_FOR_ERROR, MessageHolder.MSG_DOCUMENT_IS_DELETED);
    }

}
