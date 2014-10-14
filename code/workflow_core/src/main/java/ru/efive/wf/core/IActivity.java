package ru.efive.wf.core;

import ru.external.ProcessedData;

public interface IActivity {

    public <T extends ProcessedData> boolean initialize(T t);

    public boolean execute();

    public boolean dispose();

    public String getResult();

}