package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractLoggableBean;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.mapped.DocumentEntity;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.application.FacesMessage;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractWorkflow<I extends DocumentEntity, D extends DocumentDao> extends AbstractLoggableBean {

    protected final D dao;
    private final AuthorizationData authData;
    private I document;
    private State state;
    private WorkflowAction selectedAction;

    private List<WorkflowAction> actions = new ArrayList<>(3);

    AbstractWorkflow(AuthorizationData authData, D dao) {
        log.debug("<init>:[@{}]", Integer.toHexString(hashCode()));
        this.authData = authData;
        this.dao = dao;
    }


    public void init(final I document) {
        log.info("Start initializing new Workflow by {} with document: {}", authData.getLogString(), document.getUniqueId());
        state = State.OPENED;
        this.document = document;
        actions = getActions(document, authData);
    }

    public abstract List<WorkflowAction> getActions(I document, AuthorizationData authData);

    public List<WorkflowAction> getActions() {
        return actions;
    }

    @Transactional(transactionManager = "ordTransactionManager")
    public void process() {
        try {
            log.info("Start process {} ", selectedAction);
            if (process(selectedAction, document)) {
                log.info("processed {}", selectedAction);
                finish();
                state = State.PROCESSED;
            } else {
                state = State.WARNING;
            }
        } catch (Exception e) {
            addWarning("Внутренняя ошибка");
            log.error("Exception while processing {} ", selectedAction, e);
            state = State.WARNING;
        }
    }

    protected abstract boolean process(WorkflowAction action, I document);


    @Transactional(transactionManager = "ordTransactionManager")
    public void finish() {
        final WorkflowAction workflowAction = selectedAction;
        if (workflowAction.getTargetStatus() != null) {
            document.setStatus(workflowAction.getTargetStatus());
        }
        if (workflowAction.isNeedHistory()) {
            document.addToHistory(workflowAction.getHistoryEntity(document, authData));
        }
        dao.update(document);
    }


    public State getState() {
        return state;
    }

    public WorkflowAction getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(WorkflowAction selectedAction) {
        this.selectedAction = selectedAction;
    }

    public void addWarning(final String message) {
        MessageUtils.addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, message, ""));
    }

}
