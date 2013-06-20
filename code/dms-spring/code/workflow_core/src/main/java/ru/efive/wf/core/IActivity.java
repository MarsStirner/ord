package ru.efive.wf.core;

public interface IActivity {
	
	public <T extends ProcessedData> boolean initialize(T t);
	public boolean execute();
	public boolean dispose();
	public String getResult();
	
}