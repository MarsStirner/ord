package ru.efive.wf.core.activity;

import ru.efive.wf.core.IAction;
import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.util.EngineHelper;
import ru.external.ProcessedData;

public abstract class ParametrizedFieldActivity implements IActivity {

    Class<? extends ProcessedData> class_;
    private IAction parentAction;
    private ProcessedData processedData;
    private String resultMessage;

    @Override
    public <T extends ProcessedData> boolean initialize(T t) {
        boolean result;
        try {
            processedData = t;
            class_ = t.getClass();
            result = true;
        } catch (Exception e) {
            result = false;
            setResult(EngineHelper.DEFAULT_ERROR_MESSAGE);
            e.printStackTrace();
        }
        return result;
    }

    public IAction getParentAction() {
        return parentAction;
    }

    public void setParentAction(IAction parentAction) {
        this.parentAction = parentAction;
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

    public String getResult() {
        return resultMessage;
    }

    public void setResult(String resultMessage) {
        this.resultMessage = resultMessage;
    }
}