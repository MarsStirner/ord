package ru.efive.dms.uifaces.beans.workflow;

import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.enums.DocumentAction;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.mapped.DocumentEntity;
import ru.external.ProcessedData;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by EUpatov on 13.04.2017.
 */
public class WorkflowAction {
    private DocumentAction action;
    private boolean available;
    private String additionalInfo;
    private DocumentStatus initialStatus;
    private DocumentStatus targetStatus;
    private boolean needHistory;
    private String ui;

    public WorkflowAction() {
    }


    public DocumentAction getAction() {
        return action;
    }

    public void setAction(DocumentAction action) {
        this.action = action;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public DocumentStatus getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(DocumentStatus targetStatus) {
        this.targetStatus = targetStatus;
    }

    public DocumentStatus getInitialStatus() {
        return initialStatus;
    }

    public void setInitialStatus(DocumentStatus initialStatus) {
        this.initialStatus = initialStatus;
    }

    public void setNeedHistory(boolean needHistory) {
        this.needHistory = needHistory;
    }

    public String getUi() {
        return ui;
    }

    public void setUi(String ui) {
        this.ui = ui;
    }

    public boolean isNeedHistory() {
        return needHistory;
    }

    public HistoryEntry getHistoryEntity(ProcessedData document, AuthorizationData authData) {
        final HistoryEntry result = new HistoryEntry();
        LocalDateTime date = LocalDateTime.now();
        result.setCreated(date);
        result.setStartDate(date);
        result.setOwner(authData.getAuthorized());
        result.setDocType(document.getType().getName());
        result.setParentId(document.getId());
        result.setActionId(action.getId());
        result.setCommentary(additionalInfo);
        result.setFromStatusId(initialStatus.getId());
        if (targetStatus != null) {
            result.setToStatusId(targetStatus.getId());
        }
        return result;
    }

    @Override
    public String toString() {
        return "WorkflowAction{" +
                "action=" + action +
                ", available=" + available +
                ", additionalInfo='" + additionalInfo + '\'' +
                ", initialStatus=" + initialStatus +
                ", targetStatus=" + targetStatus +
                ", needHistory=" + needHistory +
                ", ui='" + ui + '\'' +
                '}';
    }
}
