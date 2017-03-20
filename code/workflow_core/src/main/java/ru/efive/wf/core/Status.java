package ru.efive.wf.core;

import ru.entity.model.enums.DocumentStatus;
import ru.external.ProcessedData;

import java.util.ArrayList;
import java.util.List;

public final class Status<T extends ProcessedData> {
    private List<IActivity> preStatusActivities = new ArrayList<>();
    private List<IActivity> postStatusActivities = new ArrayList<>();
    private List<StatusChangeAction> actions = new ArrayList<>();
    private T processedData;
    private boolean agreementEnabled = false;
    private DocumentStatus status = DocumentStatus.NEW;

    protected Status() {

    }

    public void setPreStatusActivities(List<IActivity> preStatusActivities) {
        this.preStatusActivities = preStatusActivities;
    }

    public List<IActivity> getPreStatusActivities() {
        return preStatusActivities;
    }


    public void setPostStatusActivities(List<IActivity> postStatusActivities) {
        this.postStatusActivities = postStatusActivities;
    }

    public List<IActivity> getPostStatusActivities() {
        return postStatusActivities;
    }


    public void setAvailableActions(List<StatusChangeAction> actions) {
        this.actions = actions;
    }

    public List<StatusChangeAction> getAvailableActions() {
        return actions;
    }

    public T getProcessedData() {
        return processedData;
    }

    public void setProcessedData(T processedData) {
        this.processedData = processedData;
    }


    public void setAgreementEnabled(boolean agreementEnabled) {
        this.agreementEnabled = agreementEnabled;
    }

    public boolean isAgreementEnabled() {
        return agreementEnabled;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }
}