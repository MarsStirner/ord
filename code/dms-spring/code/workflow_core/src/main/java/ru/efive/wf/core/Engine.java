package ru.efive.wf.core;

import java.util.ArrayList;
import java.util.List;

import ru.efive.wf.core.data.HumanTask;
import ru.efive.wf.core.util.EngineHelper;

public final class Engine {
	
	public Engine() {
		
	}
	
	public ActionResult initialize(ProcessedData processedData, ProcessUser processUser) throws Exception {		
		ActionResult result = new ActionResult();
		try {
			process = ProcessFactory.getProcessByType(processedData);
			process.setProcessUser(processUser);
			
			List <IAction> actions = new ArrayList<IAction>();
						
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
					}
					else {
						if (!resolver.isProcessed()) {
							List<HumanTask> taskList = resolver.getCurrentUniqueTaskList();
							if (taskList.size() > 0) {
								for (HumanTask task: taskList) {
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
			for (StatusChangeAction action:statusActions) {
				if (action.isAvailable()) actions.add(action);
			}
			List<NoStatusAction> noStatusActions = process.getNoStatusActions();
			for (NoStatusAction action:noStatusActions) {
				if (action.isAvailable()) actions.add(action);
			}
			currentActions = actions;
			
			result.setProcessed(true);
			process.setProcessedData(processedData);
		}
		catch (Exception e) {
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
			System.out.println("Processing action " + selectedAction.getId() + " - " + selectedAction.getName());
			result = selectedAction.run();
			System.out.println("current status in process is: " + process.getCurrentStatus().getId());
			System.out.println("after processing processedData status id is: " + process.getProcessedData().getStatusId());
			System.out.println("transaction result processedData status id is: " + result.getProcessedData().getStatusId());	
			process.setProcessedData(result.getProcessedData());		
		}
		catch (Exception e) {
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