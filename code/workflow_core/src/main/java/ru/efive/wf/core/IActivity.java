package ru.efive.wf.core;

import ru.external.ProcessedData;

public interface IActivity {

    <T extends ProcessedData> boolean initialize(T t);

    boolean execute();

    boolean dispose();

    String getResult();

}