package ru.efive.wf.core;

import java.util.ArrayList;
import java.util.List;

import ru.entity.model.wf.HumanTask;
import ru.efive.wf.core.util.EngineHelper;
import ru.external.AgreementIssue;
import ru.external.ProcessUser;
import ru.external.ProcessedData;

public final class Engine {

    public Engine() {

    }

    public ActionResult initialize(ProcessedData processedData, ProcessUser processUser) throws Exception {
        ActionResult result = new ActionResult();
        try {
            process = ProcessFactory.getProcessByType(processedData);
            process.setProcessUser(processUser);

            List<IAction> actions = new ArrayList<>();

            if (process.getCurrentStatus().isAgreementEnabled()) {
                HumanTaskTreeStateResolver resolver = new HumanTaskTreeStateResolver(processedData, ((AgreementIssue) processedData).getAgreementTree());
                if (((AgreementIssue) processedData).getAgreementTree() != null) {
                    HumanTaskActionGenerator generator = new HumanTaskActionGenerator(process, resolver);
                    if (resolver.isDeclined()) {
                        StatusChangeAction action = generator.generateProjectStateReturnAction();
                        if (action != null && action.isAvailable()) {
                            actions.add(action);
                        }
                        currentActions = actions;
                        result.setProcessed(true);
                        process.setProcessedData(processedData);
                        return result;
                    } else {
                        if (!resolver.isProcessed()) {
                            List<HumanTask> taskList = resolver.getCurrentUniqueTaskList();
                            if (taskList.size() > 0) {
                                for (HumanTask task : taskList) {
                                    actions.addAll(generator.generateActionsFromTask(task));
                                }
                                currentActions = actions;
                                result.setProcessed(true);
                                process.setProcessedData(processedData);
                                return result;
                            }
                        }
                    }
                }
            }

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

    private Process process;

    private List<IAction> currentActions;
}