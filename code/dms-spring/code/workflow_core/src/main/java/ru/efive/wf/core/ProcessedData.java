package ru.efive.wf.core;

import java.io.Serializable;

public interface ProcessedData extends Serializable {
	
	public int getId();
	public String getType();
	public int getStatusId();
	public void setStatusId(int statusId);
	public String getBeanName();
	
}