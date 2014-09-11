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

        private String value;

        public State(boolean errorState, String value) {
            this.errorState = errorState;this.value=value;
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

    public static final State STATE_CREATE = new State(false, "CREATE");

    public static final State STATE_EDIT = new State(false, "EDIT");

    public static final State STATE_FORBIDDEN = new State(true, "FORBIDDEN");

    public static final State STATE_NOT_FOUND = new State(true, "NOT_FOUND");

    public static final State STATE_DELETED = new State(true, "DELETED");

    public static final State STATE_VIEW = new State(false, "VIEW");
    
    public static final State STATE_INTERNAL_ERROR = new State(true, "INTERNAL_ERROR");

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

    protected I getInitialDocumentId() {
        String docId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get(REQUEST_PARAM_DOC_ID);
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
        if (conversation.isTransient()) {
            conversation.begin();
        }
        final String action = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(REQUEST_PARAM_DOC_ACTION);
        //CREATE
        if (REQUEST_PVALUE_DOC_ACTION_CREATE.equals(action)) {
           if(isCanCreate()){
               state = STATE_CREATE;
               initNewDocument();
               return;
           } else {
               state = STATE_FORBIDDEN;
               return;
           }
        }
        //EDIT OR VIEW EXISTING DOCUMENT
        I id = getInitialDocumentId();
        if (id != null) {
            initDocument(id);
        }
        if (document == null) {
            state = STATE_NOT_FOUND;
        } else if (REQUEST_PVALUE_DOC_ACTION_EDIT.equals(action)) {
            state = isCanEdit() ? STATE_EDIT : STATE_FORBIDDEN;
        } else {
            state = isCanView() ? STATE_VIEW : STATE_FORBIDDEN;
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
