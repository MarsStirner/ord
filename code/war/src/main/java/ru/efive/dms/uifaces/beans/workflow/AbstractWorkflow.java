package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractLoggableBean;
import ru.efive.dms.util.message.MessageUtils;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.workflow.Action;
import ru.entity.model.workflow.HistoryEntry;
import ru.entity.model.workflow.Status;
import ru.entity.model.workflow.Transition;
import ru.hitsl.sql.dao.interfaces.TransitionDao;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.application.FacesMessage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractWorkflow<I extends DocumentEntity, D extends DocumentDao> extends AbstractLoggableBean {

    protected final D dao;
    private final AuthorizationData authData;
    private final TransitionDao transitionDao;
    private I document;
    private State state;
    private Transition selectedTransition;
    private List<Transition> transitions = new ArrayList<>(3);

    AbstractWorkflow(AuthorizationData authData, D dao, TransitionDao transitionDao) {
        log.debug("<init>:[@{}]", Integer.toHexString(hashCode()));
        this.authData = authData;
        this.transitionDao = transitionDao;
        this.dao = dao;
    }


    public void init(final I document) {
        log.info("Start initializing new Workflow by {} with document: {}", authData.getLogString(), document.getUniqueId());
        state = State.OPENED;
        this.document = document;
        transitions = getAvailableTransitions(document, authData);
    }

    public List<Transition> getAvailableTransitions(I document, AuthorizationData authData) {
        log.debug("getTransitions by {} with document: {}", authData.getLogString(), document.getUniqueId());
        List<Transition> result = transitionDao.getAvailableTransitions(document.getType(), document.getStatus());
        log.info("Total action available = {}", result);
        return result;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    @Transactional(transactionManager = "ordTransactionManager")
    public void process() {
        try {
            log.info("Start process {} ", selectedTransition);
            if (process(selectedTransition, document)) {
                log.info("processed {}", selectedTransition);
                finish();
                state = State.PROCESSED;
            } else {
                state = State.WARNING;
            }
        } catch (Exception e) {
            addWarning("Внутренняя ошибка");
            log.error("Exception while processing {} ", selectedTransition, e);
            state = State.WARNING;
        }
    }

    protected abstract boolean process(Transition transition, I document);


    @Transactional(transactionManager = "ordTransactionManager")
    public void finish() {
        final Status previousStatus = document.getStatus();
        if (selectedTransition.getToStatus() != null) {
            document.setStatus(selectedTransition.getToStatus());
        }
        if (selectedTransition.isWriteHistory()) {
            document.addToHistory(
                    createHistoryEntry(document, authData, selectedTransition.getAction(), previousStatus)
            );
        }
        dao.update(document);
    }

    private HistoryEntry createHistoryEntry(I document, AuthorizationData authData, Action action, Status previousStatus) {
        final HistoryEntry result = new HistoryEntry(document);
        result.setUser(authData.getAuthorized());
        result.setStartDate(LocalDateTime.now());
        result.setAction(action);
        result.setToStatus(document.getStatus());
        result.setFromStatus(previousStatus);
        result.setProcessed(true);
        return result;
    }


    public State getState() {
        return state;
    }

    public Transition getSelectedTransition() {
        return selectedTransition;
    }

    public void setSelectedTransition(Transition selectedTransition) {
        this.selectedTransition = selectedTransition;
    }

    public void addWarning(final String message) {
        MessageUtils.addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, message, ""));
    }

}
