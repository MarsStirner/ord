package ru.efive.uifaces.bean.workflow;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;

/**
 * Base workflow functionality.
 *
 * @author Denis Kotegov
 */
public abstract class AbstractWorkflow implements Serializable {

    /**
     * Single workflow state.
     */
    public static class State {

        private final boolean stateNew;

        private final boolean stateFinal;

        private final boolean stateError;

        private final String name;

        /**
         * Creates workflow state.
         *
         * @param name name of state.
         * @param isFinal is this state an final state.
         * @param isError is this state an error state. If so, it is should be and final too.
         */
        public State(String name, boolean isFinal, boolean isError) {
            this(name, false, isFinal, isError);
        }

        private State(String name, boolean isNew, boolean isFinal, boolean isError) {
            this.name = name;
            stateNew = isNew;
            stateFinal = isFinal;
            stateError = isError;

            if (isNew && (isFinal || isError) || isError && !isFinal) {
                throw new IllegalArgumentException(
                        "New state is not compartible with osers and error state should be final");
            }
        }

        /**
         * @return <code>true</code> if state is new state</state> and <code>false</code> otherwise.
         */
        public boolean isStateNew() {
            return stateNew;
        }

        /**
         * @return <code>true</code> if state is error state</state> and <code>false</code> otherwise.
         */
        public boolean isStateError() {
            return stateError;
        }

        /**
         * @return <code>true</code> if state is final state</state> and <code>false</code> otherwise.
         */
        public boolean isStateFinal() {
            return stateFinal;
        }

        /**
         * @return name of state.
         */
        public String getName() {
            return name;
        }

    }

    /**
     * Single action result.
     */
    public static class ActionResult {

        private final State state;

        private final String result;

        /**
         * Creates action result.
         *
         * @param state state to go.
         * @param result view to apply.
         */
        public ActionResult(State state, String result) {
            this.state = state;
            this.result = result;
        }

        /**
         * @return view which will be attempted to select after action completion.
         */
        public String getResult() {
            return result;
        }

        /**
         * @return state which is will activated after end of action.
         */
        public State getState() {
            return state;
        }

    }

    /**
     * Single workflow action.
     *
     * Action execution should be processed by scheme:
     * <ul>
     * <li>Allowance and enabled checks - should produce no user messages.</li>
     * <li>Precondition checks - may produce messages.</li>
     * <li>Action doing</li>
     * <li>OnSuccess handler or exception handler, depending on previous step</li>
     * </ul>
     */
    public class Action {

        /**
         * Do some business checks. Action will not executed if <code>false</code> returned.
         *
         * @return <code>true</code> if checks are successful and <code>false</code> otherwise.
         */
        public boolean actionChecks() {
            return true;
        }

        /**
         * Doing main work of action.
         *
         * @param params parameters for action.
         * @return result of action.
         */
        public ActionResult doAction(Object... params) {
            return null;
        }

        /**
         * Successful action execution (postpone) handler.
         */
        public void onSuccess() {
            
        }

        /**
         * @return <code>true</code> if action is allowed (but still can be disabled) and <code>false</code> otherwise.
         */
        public boolean isAllowed() {
            return true;
        }

        /**
         * @return <code>true</code> if action is allowed and enabled (i.e. can be executed) and <code>false</code>
         * otherwise.
         */
        public boolean isEnabled() {
            return true;
        }

        /**
         * Exception handler for action. Can process exception and return correct result or re-throw exception.
         *
         * @param ex exception which was thrown.
         * @return action result.
         */
        public ActionResult handleException(RuntimeException ex) {
            if (ex instanceof WorkflowException) {
                return new ActionResult(STATE_ERROR, getViewKeyError());
            } else {
                throw ex;
            }
        }

    }

    /**
     * Simple action wrapper to simplify code overriding.
     */
    protected class ActionWrapper extends Action {

        private Action action;

        public ActionWrapper(Action action) {
            this.action = action;
        }

        @Override
        public boolean actionChecks() {
            return action.actionChecks();
        }

        @Override
        public ActionResult doAction(Object... params) {
            return action.doAction(params);
        }

        @Override
        public ActionResult handleException(RuntimeException ex) {
            return action.handleException(ex);
        }

        @Override
        public boolean isAllowed() {
            return action.isAllowed();
        }

        @Override
        public boolean isEnabled() {
            return action.isEnabled();
        }

        @Override
        public void onSuccess() {
            action.onSuccess();
        }

    }

    /**
     * Collection which is stores all possible actions for bean.
     */
    protected static interface ActionCollection {

        void addAction(String name, Action action);

    }

    protected class WorkflowException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public WorkflowException(Throwable cause) {
            super(cause);
        }

        public WorkflowException(String message, Throwable cause) {
            super(message, cause);
        }

        public WorkflowException(String message) {
            super(message);
        }

        public WorkflowException() {
        }
        
    }

    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------------------------------------------------------

    /** Default "new" state */
    public static final State STATE_NEW = new State("new", true, false, false);

    /** Default "final" state */
    public static final State STATE_FINAL = new State("final", false, true, false);

    /** Default "error" state */
    public static final State STATE_ERROR = new State("error", false, true, true);

    // ----------------------------------------------------------------------------------------------------------------

    private State state;

    private Map<String, Action> actions;

    @Inject
    private Conversation conversation;

    protected void doInit() {
        
    }

    protected void listActions(ActionCollection actionLister) {
        
    }

    private void startConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    private void stopConversation() {
        if (!conversation.isTransient()) {
            conversation.end();
        }
    }

    @PostConstruct
    private void postConstruct() {
        state = STATE_NEW;
        actions = new HashMap<String, Action>();

        doInit();
        listActions(new ActionCollection() {
            @Override
            public void addAction(String name, Action action) {
                actions.put(name, action);
            }
        });
    }

    protected String getViewKeyFinal() {
        return null;
    }

    protected String getViewKeyError() {
        return null;
    }

    /**
     *
     *
     * @param action
     * @return
     */
    private String doActionInternal(String name, Object... params) {
        Action action = actions.get(name);
        String result = null;

        if (action != null && action.isAllowed() && action.isEnabled()) {
            ActionResult actionResult = null;

            boolean wasNewState = state != null && state.isStateNew();

            try {
                if (action.actionChecks()) {
                    actionResult = action.doAction(params);
                }
            } catch (RuntimeException ex) {
                actionResult = action.handleException(ex);
            }

            if (actionResult != null && actionResult.getState() != null) {
                state = actionResult.getState();
                result = actionResult.getResult();

                action.onSuccess();
            }

            boolean becomeFinalState = state != null && state.isStateFinal();

            if (wasNewState && !becomeFinalState) {
                startConversation();
            } else if (!wasNewState && becomeFinalState) {
                stopConversation();
            }
        }

        return result;
    }

    /**
     * Starts action execution.
     * 
     * @param actionName name of action to be started.
     * @return view which is should to be activated (if any).
     */
    public String doAction(String actionName) {
        return doActionInternal(actionName);
    }

    /**
     * Starts action execution.
     *
     * @param actionName name of action to be started.
     * @param param action parameter
     * @return view which is should to be activated (if any).
     */
    public String doAction(String actionName, Object param) {
        return doActionInternal(actionName, param);
    }

    /**
     * Starts action execution.
     *
     * @param actionName name of action to be started.
     * @param param1 first action parameter.
     * @param param2 second action parameter.
     * @return view which is should to be activated (if any).
     */
    public String doAction(String actionName, Object param1, Object param2) {
        return doActionInternal(actionName, param1, param2);
    }

    /**
     * @param name name of action.
     * @return is the action with given name allowed.
     */
    public boolean actionAllowed(String name) {
        Action action = actions.get(name);
        return action == null? false: action.isAllowed();
    }

    /**
     * @param name name of action.
     * @return is the action with given name enabled.
     */
    public boolean actionEnabled(String name) {
        Action action = actions.get(name);
        return action == null ? false : action.isEnabled();
    }

    /**
     * @return current state.
     */
    public State getState() {
        return state;
    }

}
