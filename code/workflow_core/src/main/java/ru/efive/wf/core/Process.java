package ru.efive.wf.core;

import ru.external.ProcessUser;
import ru.external.ProcessedData;

import java.util.ArrayList;
import java.util.List;

public final class Process {

    private ProcessedData processedData;
    private ProcessUser processUser;
    private Status<? extends ProcessedData> currentStatus;
    private List<NoStatusAction> noStatusActions = new ArrayList<>();

    protected Process() {

    }

    public List<NoStatusAction> getNoStatusActions() {
        return noStatusActions;
    }

    public void setNoStatusActions(List<NoStatusAction> noStatusActions) {
        this.noStatusActions = noStatusActions;
    }

    public Status<? extends ProcessedData> getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status<? extends ProcessedData> currentStatus) {
        this.currentStatus = currentStatus;
        this.getProcessedData().setDocumentStatus(currentStatus.getStatus());
    }

    public ProcessedData getProcessedData() {
        return processedData;
    }

    public void setProcessedData(ProcessedData processedData) {
        this.processedData = processedData;
    }

    public ProcessUser getProcessUser() {
        return processUser;
    }

    public void setProcessUser(ProcessUser processUser) {
        this.processUser = processUser;
    }
}