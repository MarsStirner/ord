package ru.efive.wf.core.activity;

import ru.efive.wf.core.IAction;
import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.ProcessedData;
import ru.efive.wf.core.util.EngineHelper;

public abstract class ParametrizedFieldActivity implements IActivity {
	
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
			setResult(EngineHelper.DEFAULT_ERROR_MESSAGE);
			e.printStackTrace();
		}
		return result;
	}
	
	public void setParentAction(IAction parentAction) {
		this.parentAction = parentAction;
	}
	
	public IAction getParentAction() {
		return parentAction;
	}

	@Override
	public boolean dispose() {
		return true;
	}
	
	protected Class<? extends ProcessedData> getPersistentClass() {
		return class_;
	}
	
	protected ProcessedData getProcessedData() {
		return processedData;
	}
	
	public void setResult(String resultMessage) {
		this.resultMessage = resultMessage;
	}
	
	public String getResult() {
		return resultMessage;
	}
	
	
	private IAction parentAction;
	
	Class<? extends ProcessedData> class_;
	private ProcessedData processedData;
	
	private String resultMessage;
}