package ru.efive.wf.core;

import ru.efive.wf.core.util.EngineHelper;
import ru.external.ProcessUser;
import ru.external.ProcessedData;

import java.util.ArrayList;
import java.util.List;

public final class Engine {

    private Process process;
    private List<IAction> currentActions;

    public Engine() {

    }

    public ActionResult initialize(ProcessedData processedData, ProcessUser processUser) throws Exception {
        ActionResult result = new ActionResult();
        try {
            process = ProcessFactory.getProcessByType(processedData);
            process.setProcessUser(processUser);

            List<IAction> actions = new ArrayList<>();


            List<StatusChangeAction> statusActions = process.getCurrentStatus().getAvailableActions();
            for (StatusChangeAction action : statusActions) {
                if (action.isAvailable()) actions.add(action);
            }
            List<NoStatusAction> noStatusActions = process.getNoStatusActions();
            for (NoStatusAction action : noStatusActions) {
                if (action.isAvailable()) actions.add(action);
            }
            currentActions = actions;

            result.setProcessed(true);
            process.setProcessedData(processedData);
        } catch (Exception e) {
            result = new ActionResult();
            result.setProcessed(false);
            result.setProcessedData(process.getProcessedData());
            result.setDescription(EngineHelper.DEFAULT_ERROR_MESSAGE);
            e.printStackTrace();
        }
        return result;
    }

    public List<? extends IAction> getActions() {
        return currentActions;
    }

    public ActionResult process(IAction selectedAction) {
        ActionResult result = new ActionResult();
        try {
            System.out.println("Processing action " + selectedAction.getAction().getId() + " - " + selectedAction.getAction().getName());
            result = selectedAction.run();
            System.out.println("current status in process is: " + process.getCurrentStatus().getStatus().getId());
            System.out.println("after processing processedData status id is: " + process.getProcessedData().getDocumentStatus().getId());
            System.out.println("transaction result processedData status id is: " + result.getProcessedData().getDocumentStatus().getId());
            process.setProcessedData(result.getProcessedData());
        } catch (Exception e) {
            result = new ActionResult();
            result.setProcessed(false);
            result.setProcessedData(process.getProcessedData());
            e.printStackTrace();
        }
        return result;
    }

    public ProcessedData getProcessedData() {
        return process.getProcessedData();
    }
}