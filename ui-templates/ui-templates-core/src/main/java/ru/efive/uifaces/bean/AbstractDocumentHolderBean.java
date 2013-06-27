package ru.efive.uifaces.bean;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author Denis Kotegov
 */
// TODO: Conversition lifecycle apply.
public abstract class AbstractDocumentHolderBean<D extends Serializable, I extends Serializable>
        implements Serializable {

    public static class State implements Serializable {

        private static final long serialVersionUID = 1L;

        private final boolean errorState;

        public State(boolean errorState) {
            this.errorState = errorState;
        }

        public boolean isErrorState() {
            return errorState;
        }

    }

    // ----------------------------------------------------------------------------------------------------------------

    public static final String ACTION_RESULT_CREATE = "create";

    public static final String ACTION_RESULT_DELETE = "delete";

    public static final String ACTION_RESULT_EDIT = "edit";

    public static final String ACTION_RESULT_FORBIDDEN = "forbidden";
    
    public static final String ACTION_RESULT_INTERNAL_ERROR = "internalError";

    public static final String ACTION_RESULT_NOT_FOUND = "notFound";

    public static final String ACTION_RESULT_PERMISSION_DENIED = "permissionDenied";

    public static final String ACTION_RESULT_SAVE = "save";

    public static final String ACTION_RESULT_VIEW = "view";

    public static final State STATE_CREATE = new State(false);

    public static final State STATE_EDIT = new State(false);

    public static final State STATE_FORBIDDEN = new State(true);

    public static final State STATE_NOT_FOUND = new State(true);

    public static final State STATE_VIEW = new State(false);
    
    public static final State STATE_INTERNAL_ERROR = new State(true);

    public static final String REQUEST_PARAM_DOC_ACTION = "docAction";

    public static final String REQUEST_PVALUE_DOC_ACTION_CREATE = "create";

    public static final String REQUEST_PVALUE_DOC_ACTION_EDIT = "edit";

    public static final String REQUEST_PARAM_DOC_ID = "docId";

    // ----------------------------------------------------------------------------------------------------------------

    private D document;

    private State state;

    @Inject
    private transient Conversation conversation;

    // ----------------------------------------------------------------------------------------------------------------

    // ----------------------------------------------------------------------------------------------------------------

    protected abstract boolean deleteDocument();

    protected abstract I getDocumentId();

    protected abstract void initNewDocument();

    protected abstract void initDocument(I documentId);

    protected abstract boolean saveNewDocument();

    protected abstract boolean saveDocument();

    protected abstract FromStringConverter<I> getIdConverter();

    // ----------------------------------------------------------------------------------------------------------------

    protected String doAfterCreate() {
        return ACTION_RESULT_CREATE;
    }

    protected String doAfterDelete() {
        return ACTION_RESULT_DELETE;
    }

    protected String doAfterEdit() {
        return ACTION_RESULT_EDIT;
    }

    protected String doAfterError() {
        String result;

        if (isNotFoundState()) {
            result = ACTION_RESULT_NOT_FOUND;
        } else if (isForbiddenState()) {
            result = ACTION_RESULT_FORBIDDEN;
        } else if (isInternalErrorState()) {
        	result = ACTION_RESULT_INTERNAL_ERROR;
        } else {
            result = ACTION_RESULT_PERMISSION_DENIED;
        }

        return result;
    }

    protected String doAfterSave() {
        return ACTION_RESULT_SAVE;
    }

    protected String doAfterView() {
        return ACTION_RESULT_VIEW;
    }

    protected String getActionParameterName() {
        return REQUEST_PARAM_DOC_ACTION;
    }

    protected String getDocIdParameterName() {
        return REQUEST_PARAM_DOC_ID;
    }

    protected State getInitialState() {
        String action = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get(getActionParameterName());
        
        State result;

        if (REQUEST_PVALUE_DOC_ACTION_CREATE.equals(action)) {
            result = STATE_CREATE;
        } else if (REQUEST_PVALUE_DOC_ACTION_EDIT.equals(action)) {
            result = STATE_EDIT;
        } else {
            result = STATE_VIEW;
        }

        return result;
    }

    protected I getInitialDocumentId() {
        String docId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get(getDocIdParameterName());
        return getIdConverter().getValueFromString(docId);
    }

    public boolean isCanCreate() {
        return true;
    }

    public boolean isCanDelete() {
        return !isForbiddenState() && !isNotFoundState() && !isInternalErrorState();
    }

    public boolean isCanEdit() {
        return !isForbiddenState() && !isNotFoundState() && !isInternalErrorState();
    }

    public boolean isCanView() {
        return !isForbiddenState() && !isNotFoundState() && !isInternalErrorState();
    }

    // ----------------------------------------------------------------------------------------------------------------

    public String cancel() {
        if (state.isErrorState()) {
            return doAfterError();
        }
        
        return viewWithId(getDocumentId());
    }

    public String create() {
        if (isCanCreate()) {
            state = null;
            initNewDocument();
        }

        if (state == null) {
            state = STATE_CREATE;
            return doAfterCreate();
        } else {
            return doAfterError();
        }
    }

    public String delete() {
        String result;

        if (state.isErrorState() || !isCanDelete()) {
            result = doAfterError();
        } else if (isCanDelete() && deleteDocument()) {
            state = null;
            document = null;
            result = doAfterDelete();
        } else {
            // If this instructions are executed then deleteDocument() is returned <code>false</code> and it should
            // add error messages to context themself.
            result = null;
        }

        return result;
    }

    public String edit() {
        return edit(getDocumentId());
    }

    public String edit(I documentId) {
        initDocument(documentId);

        if (state.isErrorState() || !isCanEdit()) {
            return doAfterError();
        } else {
            state = STATE_EDIT;
            return doAfterEdit();
        }
    }

    public String save() {
        if (state.isErrorState() || !isCanEdit()) {
            return doAfterError();
        }

        boolean success;
        if (STATE_CREATE.equals(state)) {
            success = saveNewDocument();
        } else {
            success = saveDocument();
        }

        String result = null; // If save operation is usuccessfull, then methods saveDocument() and saveNewDocument()
        if (success) {        // should add error messages to context themself. (For example, concurrent modify).
            state = STATE_VIEW;
            result = doAfterSave();
        }

        return result;
    }

    public String view() {
        return viewWithId(getDocumentId());
    }

    public String viewWithId(I documentId) {
        document = null;
        state = null;
        initDocument(documentId);

        if (state == null) {
            state = STATE_VIEW;
            return doAfterView();
        } else {
            return doAfterError();
        }
    }

    @PostConstruct
    public void init() {
        state = getInitialState();

        if (STATE_CREATE.equals(state)) {
            initNewDocument();
        } else {
            I id = getInitialDocumentId();
            if (id != null) {
                initDocument(id);
            }
        }

        if (document == null) {
            state = STATE_NOT_FOUND;
        }

        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    // ----------------------------------------------------------------------------------------------------------------

    public boolean isCreateState() {
        return STATE_CREATE.equals(state);
    }

    public boolean isEditState() {
        return STATE_EDIT.equals(state);
    }

    public boolean isForbiddenState() {
        return STATE_FORBIDDEN.equals(state);
    }

    public boolean isNotFoundState() {
        return STATE_NOT_FOUND.equals(state);
    }

    public boolean isViewState() {
        return STATE_VIEW.equals(state);
    }
    
    public boolean isInternalErrorState() {
    	return STATE_INTERNAL_ERROR.equals(state);
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

    protected void setState(State state) {
        this.state = state;
    }

}
