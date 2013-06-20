package ru.efive.wf.core;

import java.util.ArrayList;
import java.util.List;

public final class Process {
	
	protected Process() {
		
	}
	
	public List<NoStatusAction> getNoStatusActions() {
		return noStatusActions;
	}
	
	public void setNoStatusActions(List<NoStatusAction> noStatusActions) {
		this.noStatusActions = noStatusActions;
	}
	
	public void setCurrentStatus(Status<? extends ProcessedData> currentStatus) {
		this.currentStatus = currentStatus;
	}
	
	public Status<? extends ProcessedData> getCurrentStatus() {
		return currentStatus;
	}
	
	public ProcessedData getProcessedData() {
		return processedData;
	}
	
	public void setProcessedData(ProcessedData processedData) {
		this.processedData = processedData;
	}
	
	public ProcessUser getProcessUser() {
		return processUser;
	}
	
	public void setProcessUser(ProcessUser processUser) {
		this.processUser = processUser;
	}
	
	
	private ProcessedData processedData;
	private ProcessUser processUser;
	private Status<? extends ProcessedData> currentStatus;
	
	private List<NoStatusAction> noStatusActions = new ArrayList<NoStatusAction>();
}