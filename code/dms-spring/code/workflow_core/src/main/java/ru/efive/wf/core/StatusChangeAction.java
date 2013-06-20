package ru.efive.wf.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.efive.wf.core.activity.SetPropertyActivity;

public class StatusChangeAction extends UserAction {
	
	public StatusChangeAction(Process process) {
		super(process);
	}
	
	@Override
	public ActionResult run() {
		ActionResult tresult = new ActionResult();
		if (autoCommit) transaction = new Transaction(this);						
		tresult.setProcessed(runPostStatus() && runPreAction() && runPostAction() && runPreStatus());
		tresult.setProcessedData(getProcess().getProcessedData());
		
		return tresult;
	}
	
	
	public Status<? extends ProcessedData> getInitialStatus() {
		return fromStatus;
	}
	
	public void setInitialStatus(Status <? extends ProcessedData> fromStatus) {
		this.fromStatus = fromStatus;
	}
	
	
	public Status<? extends ProcessedData> getDestinationStatus() {
		return toStatus;
	}
	
	public void setDestinationStatus(Status <? extends ProcessedData> toStatus) {
		this.toStatus = toStatus;
	}
	
	//TODO: need to rewrite status change activity
	private boolean runPreStatus() {
		boolean result = false;
		try {
			List<IActivity> activities = toStatus.getPreStatusActivities();
			SetPropertyActivity changeStatusActivity = new SetPropertyActivity();
			Map<String, Object> propertyChanges = new HashMap<String, Object>();
			propertyChanges.put("statusId", toStatus.getId());
			changeStatusActivity.setPropertyChanges(propertyChanges);
			if (activities.add(changeStatusActivity)) {
				if (activities.size() > 0) {
					int i = 0;
					result = true;
					while (i < activities.size() && result) {
						IActivity activity = activities.get(i);
						System.out.println("processing activity " + i);
						result = activity.initialize(getProcess().getProcessedData()) && activity.execute() && activity.dispose();			
						
						System.out.println("status id: " + getProcess().getProcessedData().getStatusId());
						i++;
					}
					if (result) {
						getProcess().setCurrentStatus(toStatus);
					}
				}
				else {
					result = true;
				}
			}
			else {
				System.out.println("Unable to instantiate status change activity");
			}
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	private boolean runPostStatus() {
		boolean result = false;
		try {
			List<IActivity> activities = fromStatus.getPostStatusActivities();
			if (activities.size() > 0) {
				int i = 0;
				result = true;
				while (i < activities.size() && result) {
					IActivity activity = activities.get(i);
					System.out.println("processing activity " + i);
					result = activity.initialize(getProcess().getProcessedData()) && activity.execute() && activity.dispose();
					i++;
				}
			}
			else {
				result = true;
			}
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	
	private Status<? extends ProcessedData> fromStatus;
	private Status<? extends ProcessedData> toStatus;
}