package ru.efive.uifaces.bean.workflow;

import java.io.Serializable;

/**
 * Base workflow for document lifecycle.
 *
 * @param <D> Document Class
 * @param <I> Identifier Class
 * @author Denis Kotegov
 */
public abstract class AbstractDocumentWorkflow<D extends Serializable, I extends Serializable>  extends AbstractWorkflow {

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------------------------------------------------

    /** "edit" state */
    public static final State STATE_EDIT = new State("edit", false, false);

    /** "create" state */
    public static final State STATE_CREATE = new State("create", false, false);

    /** "view" state */
    public static final State STATE_VIEW = new State("view", false, false);

    // ----------------------------------------------------------------------------------------------------------------

    /** Apply action name. */
    public static final String ACTION_APPLY = "apply";

    public static final String ACTION_CANCEL = "cancel";

    public static final String ACTION_CANCEL_AND_VIEW = "cancelAndView";

    public static final String ACTION_CREATE = "create";

    public static final String ACTION_DELETE = "delete";

    public static final String ACTION_EDIT = "edit";

    public static final String ACTION_EDIT_BY_ID = "editById";

    public static final String ACTION_SAVE = "save";

    public static final String ACTION_SAVE_AND_VIEW = "saveAndView";

    public static final String ACTION_VIEW_BY_ID = "viewById";

    // ----------------------------------------------------------------------------------------------------------------

    private D document;

    // ----------------------------------------------------------------------------------------------------------------

    protected abstract D initNewDocument();

    protected abstract D initDocument(I documentId);

    protected abstract I getDocumentId(D document);

    protected abstract void saveDocument(D document);

    protected abstract D saveNewDocument(D document);

    protected abstract void deleteDocument(D document);

    // ----------------------------------------------------------------------------------------------------------------

    protected String getViewKeyCreate() {
        return null;
    }

    protected String getViewKeyEdit() {
        return null;
    }

    protected String getViewKeyView() {
        return null;
    }

    // ----------------------------------------------------------------------------------------------------------------

    protected String getActionResultApply() {
        return getViewKeyEdit();
    }

    protected String getActionResultCancel() {
        return getViewKeyFinal();
    }

    protected String getActionResultCancelAndView() {
        return getViewKeyView();
    }

    protected String getActionResultCreate() {
        return getViewKeyCreate();
    }

    protected String getActionResultDelete() {
        return getViewKeyFinal();
    }

    protected String getActionResultEdit() {
        return getViewKeyEdit();
    }

    protected String getActionResultEditById() {
        return getViewKeyEdit();
    }

    protected String getActionResultSave() {
        return getViewKeyFinal();
    }

    protected String getActionResultSaveAndView() {
        return getViewKeyView();
    }

    protected String getActionResultViewById() {
        return getViewKeyView();
    }

    // ----------------------------------------------------------------------------------------------------------------

    protected State getActionStateApply() {
        return STATE_EDIT;
    }

    protected State getActionStateCancel() {
        return STATE_FINAL;
    }

    protected State getActionStateCancelAndView() {
        return STATE_VIEW;
    }

    protected State getActionStateCreate() {
        return STATE_CREATE;
    }

    protected State getActionStateDelete() {
        return STATE_FINAL;
    }
    
    protected State getActionStateEdit() {
        return STATE_EDIT;
    }
    
    protected State getActionStateEditById() {
        return STATE_EDIT;
    }

    protected State getActionStateSave() {
        return STATE_FINAL;
    }

    protected State getActionStateSaveAndView() {
        return STATE_VIEW;
    }

    protected State getActionStateViewById() {
        return STATE_VIEW;
    }

    // ----------------------------------------------------------------------------------------------------------------

    protected Action buildActionApply() {
        return new Action() {

            @Override
            public ActionResult doAction(Object... params) {
                if (STATE_CREATE.equals(getState())) {
                    document = saveNewDocument(document);
                } else {
                    saveDocument(document);
                }

                return new ActionResult(getActionStateApply(), getActionResultApply());
            }

            @Override
            public boolean isAllowed() {
                return STATE_CREATE.equals(getState()) || STATE_EDIT.equals(getState());
            }
            
        };
    }

    protected Action buildActionCancel() {
        return new Action() {

            @Override
            public ActionResult doAction(Object... params) {
                return new ActionResult(getActionStateCancel(), getActionResultCancel());
            }

            @Override
            public boolean isAllowed() {
                return true;
            }

        };
    }

    protected Action buildActionCancelAndView() {
        return new Action() {

            @Override
            public ActionResult doAction(Object... params) {
                document = initDocument(getDocumentId(document));
                return new ActionResult(getActionStateCancelAndView(), getActionResultCancelAndView());
            }

            @Override
            public boolean isAllowed() {
                return STATE_EDIT.equals(getState());
            }

        };
    }

    protected Action buildActionCreate() {
        return new Action() {

            @Override
            public ActionResult doAction(Object... params) {
                document = initNewDocument();
                return new ActionResult(getActionStateCreate(), getActionResultCreate());
            }

            @Override
            public boolean isAllowed() {
                return STATE_NEW.equals(getState());
            }

        };
    }

    protected Action buildActionDelete() {
        return new Action() {

            @Override
            public ActionResult doAction(Object... params) {
                deleteDocument(document);
                document = null;
                return new ActionResult(getActionStateDelete(), getActionResultDelete());
            }

            @Override
            public boolean isAllowed() {
                return STATE_VIEW.equals(getState()) || STATE_EDIT.equals(getState());
            }
            
        };
    }

    protected Action buildActionEdit() {
        return new Action() {

            @Override
            public ActionResult doAction(Object... params) {
                document = initDocument(getDocumentId(document));
                return new ActionResult(getActionStateEdit(), getActionResultEdit());
            }

            @Override
            public boolean isAllowed() {
                return STATE_VIEW.equals(getState());
            }
          
        };
    }

    protected Action buildActionEditById() {
        return new Action() {

            @Override
            @SuppressWarnings("unchecked")
            public ActionResult doAction(Object... params) {
                assert(params != null);
                assert(params.length == 1);
                assert(params[0] instanceof Serializable);

                document = initDocument((I) params[0]);
                return new ActionResult(getActionStateEditById(), getActionResultEditById());
            }

            @Override
            public boolean isAllowed() {
                return STATE_NEW.equals(getState());
            }

        };
    }

    protected Action buildActionSave() {
        return new Action() {

            @Override
            public ActionResult doAction(Object... params) {
                if (STATE_CREATE.equals(getState())) {
                    document = saveNewDocument(document);
                } else {
                    saveDocument(document);
                }

                return new ActionResult(getActionStateSave(), getActionResultSave());
            }

            @Override
            public boolean isAllowed() {
                return STATE_CREATE.equals(getState()) || STATE_EDIT.equals(getState());
            }

        };
    }

    protected Action buildActionSaveAndView() {
        return new Action() {

            @Override
            public ActionResult doAction(Object... params) {
                if (STATE_CREATE.equals(getState())) {
                    document = saveNewDocument(document);
                } else {
                    saveDocument(document);
                }

                return new ActionResult(getActionStateSaveAndView(), getActionResultSaveAndView());
            }

            @Override
            public boolean isAllowed() {
                return STATE_CREATE.equals(getState()) || STATE_EDIT.equals(getState());
            }
        };
    }

    protected Action buildActionViewById() {
        return new Action() {

            @Override
            @SuppressWarnings("unchecked")
            public ActionResult doAction(Object... params) {
                assert (params != null);
                assert (params.length == 1);
                assert (params[0] instanceof Serializable);

                document = initDocument((I) params[0]);
                return new ActionResult(getActionStateViewById(), getActionResultViewById());
            }

            @Override
            public boolean isAllowed() {
                return STATE_NEW.equals(getState());
            }
        };
    }

    // ----------------------------------------------------------------------------------------------------------------

    @Override
    protected void listActions(ActionCollection actionLister) {
        super.listActions(actionLister);

        actionLister.addAction(ACTION_APPLY, buildActionApply());
        actionLister.addAction(ACTION_CANCEL, buildActionCancel());
        actionLister.addAction(ACTION_CANCEL_AND_VIEW, buildActionCancelAndView());
        actionLister.addAction(ACTION_CREATE, buildActionCreate());
        actionLister.addAction(ACTION_DELETE, buildActionDelete());
        actionLister.addAction(ACTION_EDIT, buildActionEdit());
        actionLister.addAction(ACTION_EDIT_BY_ID, buildActionEditById());
        actionLister.addAction(ACTION_SAVE, buildActionSave());
        actionLister.addAction(ACTION_SAVE_AND_VIEW, buildActionSaveAndView());
        actionLister.addAction(ACTION_VIEW_BY_ID, buildActionViewById());
    }

    // ----------------------------------------------------------------------------------------------------------------

    public D getDocument() {
        return document;
    }

}
