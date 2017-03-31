package ru.efive.wf.core.activity;

import ru.entity.model.wf.HumanTask;
import ru.external.ProcessedData;

public class SetHumanTaskStatus extends BlankActivity {

    Class<? extends ProcessedData> class_;
    private ProcessedData processedData;
    private HumanTask task;

    public SetHumanTaskStatus() {

    }

    @Override
    public <T extends ProcessedData> boolean initialize(T t) {
        boolean result = false;
        try {
            processedData = t;
            class_ = t.getClass();
            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    public HumanTask getTask() {
        return task;
    }

    public void setTask(HumanTask task) {
        this.task = task;
    }

    @Override
    public boolean execute() {
        boolean result = false;
        try {

            result = true;
        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean dispose() {
        return true;
    }
}