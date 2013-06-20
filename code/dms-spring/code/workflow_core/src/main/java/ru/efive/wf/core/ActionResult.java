package ru.efive.wf.core;

public class ActionResult {
	
	public ActionResult() {
		
	}
	
	public ProcessedData getProcessedData() {
		return processedData;
	}
	
	public void setProcessedData(ProcessedData processedData) {
		this.processedData = processedData;
	}
	
	public boolean isProcessed() {
		return isProcessed;
	}
	
	public void setProcessed(boolean isProcessed) {
		this.isProcessed = isProcessed;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	private ProcessedData processedData;
	private boolean isProcessed;
	private String description;
}