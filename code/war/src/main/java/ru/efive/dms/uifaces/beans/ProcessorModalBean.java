package ru.efive.dms.uifaces.beans;

import org.apache.commons.lang.StringUtils;
import ru.efive.uifaces.bean.ModalWindowHolderBean;
import ru.efive.wf.core.*;
import ru.efive.wf.core.activity.enums.ProcessState;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.util.EngineHelper;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.Person;
import ru.external.ProcessedData;
import ru.util.ApplicationHelper;

import javax.faces.context.FacesContext;
import java.util.*;

public class ProcessorModalBean extends ModalWindowHolderBean {
    private static final long serialVersionUID = 8956219164518339475L;

    private ProcessState state;
    private ProcessedData processedData;
    private Engine engine = null;
    private List<IAction> availableActions;
    private IAction selectedAction;
    private LocalActivity processedActivity;
    private List<LocalActivity> localPreActionActivities;
    private int processedActivityIndex;
    private HistoryEntry historyEntry;
    private String actionResult;
    private SessionManagementBean sessionManagement;

    public void setProcessedData(ProcessedData processedData) {
        this.processedData = processedData;
    }

    public ProcessedData getProcessedData() {
        return processedData;
    }

    public List<IAction> getAvailableActions() {
        return availableActions;
    }

    public void select(IAction selectedAction) {
        this.selectedAction = selectedAction;
    }

    public void setSelectedAction(IAction selectedAction){
        this.selectedAction = selectedAction;
    }

    public boolean selected(IAction action) {
        return this.selectedAction != null && this.selectedAction.equals(action);
    }

    public IAction getSelectedAction() {
        return selectedAction;
    }

    public LocalActivity getProcessedActivity() {
        return processedActivity;
    }

    public boolean isNoActionsAvailable() {
        return state.equals(ProcessState.NO_ACTIONS_AVAILABLE);
    }

    public boolean isActionsAvailable() {
        return state.equals(ProcessState.ACTIONS_AVAILABLE);
    }

    public boolean isProcessingState() {
        return state.equals(ProcessState.PROCESSING);
    }

    public boolean isProcessedState() {
        return state.equals(ProcessState.PROCESSED);
    }

    public boolean isFailureState() {
        return state.equals(ProcessState.FAILURE);
    }

    @Override
    protected void doShow() {
        super.doShow();
        try {
            doInit();
            if (getProcessedData() == null) {
                actionResult = EngineHelper.DEFAULT_ERROR_MESSAGE;
                System.out.println(EngineHelper.WRONG_PROCESSED_DATA);
                state = ProcessState.FAILURE;
            }
            FacesContext context = FacesContext.getCurrentInstance();
            sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
            if (sessionManagement == null) {
                actionResult = EngineHelper.DEFAULT_ERROR_MESSAGE;
                System.out.println(EngineHelper.WRONG_SESSION_BEAN);
                state = ProcessState.FAILURE;
            }
            availableActions = Collections.synchronizedList(new ArrayList<IAction>());
            localPreActionActivities = Collections.synchronizedList(new ArrayList<LocalActivity>());
            processedActivity = null;
            engine = new Engine();
            Person person;
            if (!sessionManagement.isSubstitution()) {
                person = new Person(sessionManagement.getLoggedUser());
            } else {
                person = new Person(sessionManagement.getLoggedUser(), sessionManagement.getSubstitutedUsers());
            }
            engine.initialize(getProcessedData(), person);
            for (IAction action : engine.getActions()) {
                availableActions.add(action);
            }
            if (!availableActions.isEmpty()) {
                state = ProcessState.ACTIONS_AVAILABLE;
            } else {
                actionResult = EngineHelper.PROCESSOR_NO_ACTIONS;
                state = ProcessState.NO_ACTIONS_AVAILABLE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void process() {
        if (selectedAction == null) {
            actionResult = EngineHelper.ACTION_NOT_SELECTED;
            state = ProcessState.FAILURE;
            return;
        }
        if (state != ProcessState.PROCESSING) {
            state = ProcessState.PROCESSING;
            if (selectedAction.isHistoryAction()) {
                historyEntry = new HistoryEntry();
                Date date = Calendar.getInstance(ApplicationHelper.getLocale()).getTime();
                historyEntry.setCreated(date);
                historyEntry.setStartDate(date);
                historyEntry.setOwner(sessionManagement.getLoggedUser());
                historyEntry.setDocType(getProcessedData().getDocumentType().getName());
                historyEntry.setParentId(getProcessedData().getId());
                historyEntry.setActionId(selectedAction.getAction().getId());
                historyEntry.setCommentary("");
                if (selectedAction instanceof StatusChangeAction) {
                    historyEntry.setFromStatusId(((StatusChangeAction) selectedAction).getInitialStatus().getStatus().getId());
                    historyEntry.setToStatusId(((StatusChangeAction) selectedAction).getDestinationStatus().getStatus().getId());
                }
            }
            for (IActivity activity : selectedAction.getLocalActivities()) {
                if (activity instanceof LocalActivity) {
                    localPreActionActivities.add((LocalActivity) activity);
                } else {
                    actionResult = EngineHelper.DEFAULT_ERROR_MESSAGE;
                    System.out.println(EngineHelper.WRONG_LOCAL_ACTIVITY_CLASS);
                    state = ProcessState.FAILURE;
                    return;
                }
            }
            processedActivityIndex = 0;
            if (localPreActionActivities.size() > 0) {
                processedActivity = localPreActionActivities.get(processedActivityIndex);
                return;
            }
        }
        if (state == ProcessState.PROCESSING) {
            try {
                if (processedActivity != null) {
                    ActionResult documentInitializationResult = processedActivity.getDocument().initialize();
                    if (!documentInitializationResult.isProcessed()) {
                        if (documentInitializationResult.getDescription() != null && !documentInitializationResult.getDescription().equals("")) {
                            actionResult = documentInitializationResult.getDescription();
                            state = ProcessState.FAILURE;
                        }
                        return;
                    }
                    if (!(((IActivity) processedActivity).initialize(getProcessedData()) && ((IActivity) processedActivity).execute())) {
                        actionResult = EngineHelper.DEFAULT_ERROR_MESSAGE;
                        System.out.println(EngineHelper.EXCEPTION_PROCESSING_LOCAL_ACTIVITY);
                        state = ProcessState.FAILURE;
                        return;
                    }
                }
                if (processedActivityIndex < (localPreActionActivities.size() - 1)) {
                    processedActivityIndex++;
                    processedActivity = localPreActionActivities.get(processedActivityIndex);
                } else {
                    if (engine == null) {
                        engine = new Engine();
                        Person person;
                        if (!sessionManagement.isSubstitution()) {
                            person = new Person(sessionManagement.getLoggedUser());
                        } else {
                            person = new Person(sessionManagement.getLoggedUser(), sessionManagement.getSubstitutedUsers());
                        }
                        new Engine().initialize(getProcessedData(), person);
                    }
                    if (selectedAction != null &&
                            !selectedAction.getAction().getName().equals("") &&
                            selectedAction.getAction().getId() != 0) {
                        ActionResult actionResult = engine.process(selectedAction);
                        if (actionResult.isProcessed()) {
                            doPostProcess(actionResult);
                            state = ProcessState.PROCESSED;
                            this.actionResult = StringUtils.isEmpty(selectedAction.getEvaluationMessage()) ? EngineHelper.DEFAULT_PROCESSED_MESSAGE :
                                    selectedAction.getEvaluationMessage();
                        } else {
                            this.actionResult = EngineHelper.DEFAULT_ERROR_MESSAGE;
                            doProcessException(actionResult);
                            state = ProcessState.FAILURE;
                            return;
                        }
                        if (selectedAction.isHistoryAction()) {
                            try {
                                historyEntry.setEndDate(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
                                historyEntry.setProcessed(actionResult.isProcessed());
                                if (actionResult.getDescription() == null || actionResult.getDescription().equals("")) {
                                    for (EditableProperty property : selectedAction.getProperties()) {
                                        if (property.getName().equals(EngineHelper.PROP_WF_RESULT_DESCRIPTION)) {
                                            historyEntry.setCommentary(property.getValue().toString());
                                        }
                                    }
                                } else {
                                    historyEntry.setCommentary(actionResult.getDescription());
                                }
                                doSave();
                            } catch (Exception e) {
                                System.out.println(EngineHelper.EXCEPTION_PROCESSING_HISTORY);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                actionResult = EngineHelper.DEFAULT_ERROR_MESSAGE;
                state = ProcessState.FAILURE;
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doSave() {
        super.doSave();
    }

    public String getActionResult() {
        return actionResult;
    }

    protected void setActionResult(String actionResult) {
        this.actionResult = actionResult;
    }

    public HistoryEntry getHistoryEntry() {
        return historyEntry;
    }

    /**
     * Инициализация modal bean и WF engine
     */
    protected void doInit() {

    }

    /**
     * Завершение работы WF engine
     */
    protected void doPostProcess(ActionResult actionResult) {

    }

    /**
     * Обработка ошибок
     */
    protected void doProcessException(ActionResult actionResult) {

    }

}