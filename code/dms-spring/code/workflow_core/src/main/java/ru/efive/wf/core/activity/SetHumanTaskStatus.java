package ru.efive.wf.core.activity;

import ru.efive.wf.core.ProcessedData;
import ru.efive.wf.core.data.HumanTask;

public class SetHumanTaskStatus extends BlankActivity {
	
	public SetHumanTaskStatus() {
		
	}
	
	@Override
	public <T extends ProcessedData> boolean initialize(T t) {
		boolean result = false;
		try {
			processedData = t;
			class_ = t.getClass();
			result = true;
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}
	
	public void setTask(HumanTask task) {
		this.task = task;
	}
	
	public HumanTask getTask() {
		return task;
	}
	
	@Override
	public boolean execute() {
		boolean result = false;
		try {
			
			result = true;
		}
		catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean dispose() {
		return true;
	}
	
	
	Class<? extends ProcessedData> class_;
	private ProcessedData processedData;
	private HumanTask task;
}