package ru.efive.wf.core.activity;

import ru.efive.wf.core.IActivity;
import ru.efive.wf.core.LocalActivity;
import ru.external.ProcessedData;
import ru.efive.wf.core.data.LocalBackingBean;

public class LocalValidateActivity implements IActivity, LocalActivity {

    @Override
    public <T extends ProcessedData> boolean initialize(T t) {
        boolean result = false;
        try {

        } catch (Exception e) {
            result = false;
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean execute() {
        boolean result = false;
        try {

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

    @Override
    public String getResult() {
        return null;
    }

    @Override
    public LocalBackingBean getDocument() {

        return null;
    }


}