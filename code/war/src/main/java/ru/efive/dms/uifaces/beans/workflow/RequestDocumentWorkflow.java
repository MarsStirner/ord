package ru.efive.dms.uifaces.beans.workflow;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.util.WorkflowHelper;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.enums.DocumentAction;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static ru.entity.model.enums.DocumentStatus.*;

/**
 * Created by EUpatov on 13.04.2017.
 */
@ViewScopedController("requestWorkflow")
public class RequestDocumentWorkflow {
    private static final Logger log = LoggerFactory.getLogger(RequestDocumentWorkflow.class);

    @Autowired
    @Qualifier("requestDocumentDao")
    private RequestDocumentDao requestDocumentDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    private WorkflowHelper helper;


    private List<WorkflowAction> actions = new ArrayList<>(3);

    private State state = State.HIDDEN;


    private RequestDocument document;
    private String processedMessage;
    private WorkflowAction selectedAction;

    @PostConstruct
    public void postConstruct(){
        log.info("@PostConstruct callback");
    }


    public void init(final RequestDocument document) {
        log.info("Start initializing new Workflow by {} with document: {}", authData.getLogString(), document.getUniqueId());
        state = State.OPENED;
        this.document = document;
        actions = getActions(document, authData);
    }

    private List<WorkflowAction> getActions(RequestDocument document, AuthorizationData authData) {
        log.debug("getActions by {} with document: {}", authData.getLogString(), document.getUniqueId());
        final List<WorkflowAction> result = new ArrayList<>();
        switch (document.getDocumentStatus()) {
            // Проект документа
            case ON_REGISTRATION: {
                // На регистрации - Зарегистрирован
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.CHECK_IN_1);
                action.setAvailable(true);
                action.setInitialStatus(ON_REGISTRATION);
                action.setTargetStatus(CHECK_IN_2);
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            //Зарегистрирован - На исполнении
            case CHECK_IN_2:{
                final WorkflowAction action2 = new WorkflowAction();
                action2.setAction(DocumentAction.REDIRECT_TO_EXECUTION_2);
                action2.setAvailable(true);
                action2.setInitialStatus(CHECK_IN_2);
                action2.setTargetStatus(ON_EXECUTION_80);
                action2.setNeedHistory(true);
                result.add(action2);
                break;
            }
            //На исполнении - Исполнен
            case ON_EXECUTION_80: {
                //На исполнении
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.EXECUTE_80);
                action.setAvailable(true);
                action.setInitialStatus(ON_EXECUTION_80);
                action.setTargetStatus(EXECUTE);
                //TODO не проверять ли юзверя среди исполнителей
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            //Исполнен - Архив
            case EXECUTE: {
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.IN_ARCHIVE_90);
                action.setAvailable(true);
                action.setInitialStatus(EXECUTE);
                action.setTargetStatus(IN_ARCHIVE_100);
                //TODO привелегия архивариуса
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            case IN_ARCHIVE_100:
                break;
            default:
                log.error("NON processed STATE!!!! {}", document.getDocumentStatus());
                break;
        }
        log.info("Total action available = {}", result);
        return result;
    }


    public List<WorkflowAction> getActions() {
        return actions;
    }

    public void process() {
        document.setWFResultDescription(null);
        boolean success = false;
        try {
            final WorkflowAction workflowAction = selectedAction;
            switch (workflowAction.getAction()) {
                case CHECK_IN_1: {
                    success = helper.setRequestRegistrationNumber(document);
                    processedMessage = document.getWFResultDescription();
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_INCOMING_EXECUTORS);
                    break;
                }
                default:
                    log.warn("{} is only document state changing action!", workflowAction.getAction());
                    success = true;
                    break;
            }
            if (success) {
                if(StringUtils.isEmpty(workflowAction.getUi())) {
                    finish();
                    state=State.PROCESSED;
                } else {
                    state= State.WAIT_FOR_USER_INPUT;
                }
            } else {
                state=State.PROCESSED;
            }
        } catch (Exception e) {
            this.processedMessage = "Внутренняя ошибка";
            state=State.PROCESSED;
        }
    }

    public void finish() {
        final WorkflowAction workflowAction = selectedAction;
        if (workflowAction.getTargetStatus() != null) {
            document.setDocumentStatus(workflowAction.getTargetStatus());
        }
        if (workflowAction.isNeedHistory()) {
            document.addToHistory(workflowAction.getHistoryEntity(document, authData));
        }
        requestDocumentDao.update(document);
        this.processedMessage = "Успешно выполнено";
    }

    public String getProcessedMessage() {
        return processedMessage;
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
}
