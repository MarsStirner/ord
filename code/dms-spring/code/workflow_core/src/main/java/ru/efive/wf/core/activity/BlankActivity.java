package ru.efive.wf.core.activity;

import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.ProcessedData;

public class BlankActivity implements IActivity {
	
	public BlankActivity() {
		
	}
	
	@Override
	public <T extends ProcessedData> boolean initialize(T t) {
		return true;
	}
	
	@Override
	public boolean execute() {
		return true;
	}
	
	@Override
	public boolean dispose() {
		return true;
	}
	
	@Override
	public String getResult() {
		return "Success";
	}
	
}