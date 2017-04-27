package ru.efive.dms.uifaces.beans.workflow;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.util.WorkflowHelper;

import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.enums.DocumentAction;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static ru.entity.model.enums.DocumentStatus.*;

/**
 * Created by EUpatov on 13.04.2017.
 */
@ViewScopedController("outgoingWorkflow")
public class OutgoingDocumentWorkflow {
    private static final Logger log = LoggerFactory.getLogger(OutgoingDocumentWorkflow.class);

    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    private WorkflowHelper helper;


    private List<WorkflowAction> actions = new ArrayList<>(3);

    private State state = State.HIDDEN;


    private OutgoingDocument document;
    private String processedMessage;
    private WorkflowAction selectedAction;

    @PostConstruct
    public void postConstruct(){
        log.info("@PostConstruct callback");
    }


    public void init(final OutgoingDocument document) {
        log.info("Start initializing new Workflow by {} with document: {}", authData.getLogString(), document.getUniqueId());
        state = State.OPENED;
        this.document = document;
        actions = getActions(document, authData);
    }

    private List<WorkflowAction> getActions(OutgoingDocument document, AuthorizationData authData) {
        log.debug("getActions by {} with document: {}", authData.getLogString(), document.getUniqueId());
        final List<WorkflowAction> result = new ArrayList<>();
        switch (document.getDocumentStatus()) {
            // Проект документа
            case DOC_PROJECT_1: {
                // Проект документа - На рассмотрение
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.REDIRECT_TO_CONSIDERATION_2);
                action.setAvailable(true);
                action.setInitialStatus(DOC_PROJECT_1);
                action.setTargetStatus(ON_CONSIDERATION);
                action.setNeedHistory(true);
                result.add(action);

                // Проект документа - Зарегистрирован
                final WorkflowAction action2 = new WorkflowAction();
                action2.setAction(DocumentAction.CHECK_IN_80);
                action2.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action2.setInitialStatus(DOC_PROJECT_1);
                action2.setTargetStatus(CHECK_IN_80);
                action2.setNeedHistory(true);
                result.add(action2);

                // Проект документа - Согласование
                final WorkflowAction action4 = new WorkflowAction();
                action4.setAction(DocumentAction.REDIRECT_TO_AGREEMENT);
                action4.setAvailable(true);
                action4.setInitialStatus(DOC_PROJECT_1);
                action4.setTargetStatus(AGREEMENT_3);
                action4.setNeedHistory(true);
                //TODO
                result.add(action4);
                break;
            }
            //На рассмотрении - Зарегистрирован
            case ON_CONSIDERATION:{
                final WorkflowAction action2 = new WorkflowAction();
                action2.setAction(DocumentAction.CHECK_IN_80);
                action2.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action2.setInitialStatus(ON_CONSIDERATION);
                action2.setTargetStatus(CHECK_IN_80);
                action2.setNeedHistory(true);
                result.add(action2);
                break;
            }
            //Зарегистрирован
            case CHECK_IN_80: {
                //На исполнении
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.EXECUTE_90);
                action.setAvailable(true);
                action.setInitialStatus(CHECK_IN_80);
                action.setTargetStatus(EXECUTE);
                //TODO не проверять ли юзверя среди исполнителей
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            //Исполнен - Архив
            case EXECUTE: {
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.IN_ARCHIVE_99);
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
            case EXTRACT:
                break;
            case SOURCE_DESTROY:
                break;
            case REDIRECT:
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
                case CHECK_IN_80: {
                    success = helper.setOutgoingRegistrationNumber(document);
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
        outgoingDocumentDao.update(document);
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
