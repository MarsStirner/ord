package ru.efive.dms.uifaces.beans.workflow;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.efive.dms.util.WorkflowHelper;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.enums.DocumentAction;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static ru.entity.model.enums.DocumentStatus.*;

/**
 * Created by EUpatov on 13.04.2017.
 */
@ViewScopedController("internalWorkflow")
public class InternalDocumentWorkflow {
    private static final Logger log = LoggerFactory.getLogger(InternalDocumentWorkflow.class);

    @Autowired
    @Qualifier("internalDocumentDao")
    private InternalDocumentDao internalDocumentDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    private WorkflowHelper helper;


    private List<WorkflowAction> actions = new ArrayList<>(3);

    private State state = State.HIDDEN;


    private InternalDocument document;
    private String processedMessage;
    private WorkflowAction selectedAction;

    @PostConstruct
    public void postConstruct() {
        log.info("@PostConstruct callback");
    }


    public void init(final InternalDocument document) {
        log.info("Start initializing new Workflow by {} with document: {}", authData.getLogString(), document.getUniqueId());
        state = State.OPENED;
        this.document = document;
        actions = getActions(document, authData);
    }

    private List<WorkflowAction> getActions(InternalDocument document, AuthorizationData authData) {
        log.debug("getActions by {} with document: {}", authData.getLogString(), document.getUniqueId());
        final List<WorkflowAction> result = new ArrayList<>();
        switch (document.getDocumentStatus()) {
            // Проект документа
            case DOC_PROJECT_1: {
                // Проект документа - На рассмотрение
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.REDIRECT_TO_CONSIDERATION_1);
                action.setAvailable(true);
                action.setInitialStatus(DOC_PROJECT_1);
                action.setTargetStatus(ON_CONSIDERATION);
                action.setNeedHistory(true);
                result.add(action);

                // Проект документа - Зарегистрирован
                final WorkflowAction action2 = new WorkflowAction();
                action2.setAction(DocumentAction.CHECK_IN_5);
                action2.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action2.setInitialStatus(DOC_PROJECT_1);
                action2.setTargetStatus(CHECK_IN_5);
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

                // Регистрация изменений закрытого периода (Проект документа - Зарегистрирован)
                final WorkflowAction action3 = new WorkflowAction();
                action3.setAction(DocumentAction.REGISTRATION_CLOSE_PERIOD_551);
                action3.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action3.setInitialStatus(DOC_PROJECT_1);
                action3.setTargetStatus(CHECK_IN_5);
                action3.setNeedHistory(true);
                //TODO
                action3.setUi("/workflow/internalClosePeriodRegistration.xhtml");
                result.add(action3);
                break;
            }
            // На рассмотрении - Зарегистрирован
            case ON_CONSIDERATION: {
                //На исполнении
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.CHECK_IN_5);
                action.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action.setInitialStatus(ON_CONSIDERATION);
                action.setTargetStatus(CHECK_IN_5);
                //TODO не проверять ли юзверя среди исполнителей
                action.setNeedHistory(true);
                result.add(action);

                // Регистрация изменений закрытого периода (Проект документа - Зарегистрирован)
                final WorkflowAction action3 = new WorkflowAction();
                action3.setAction(DocumentAction.REGISTRATION_CLOSE_PERIOD_551);
                action3.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action3.setInitialStatus(ON_CONSIDERATION);
                action3.setTargetStatus(CHECK_IN_5);
                action3.setNeedHistory(true);
                //TODO
                action3.setUi("/workflow/internalClosePeriodRegistration.xhtml");
                result.add(action3);

                //На рассмотрении - Отказ
                final WorkflowAction action2 = new WorkflowAction();
                action2.setAction(DocumentAction.CANCEL_150);
                action2.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action2.setInitialStatus(ON_CONSIDERATION);
                action2.setTargetStatus(CANCEL_150);
                action2.setNeedHistory(true);
                result.add(action2);
                break;
            }
            // На согласовании
            case AGREEMENT_3: {
                // На согласовании - Зарегистрирован
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.CHECK_IN_5);
                action.setAvailable(authData.isAdministrator());
                action.setInitialStatus(AGREEMENT_3);
                action.setTargetStatus(CHECK_IN_5);
                action.setNeedHistory(true);
                result.add(action);
                // Регистрация изменений закрытого периода (На согласовании - Зарегистрирован)
                final WorkflowAction action3 = new WorkflowAction();
                action3.setAction(DocumentAction.REGISTRATION_CLOSE_PERIOD_551);
                action3.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action3.setInitialStatus(AGREEMENT_3);
                action3.setTargetStatus(CHECK_IN_5);
                action3.setNeedHistory(true);
                //TODO
                action3.setUi("/workflow/internalClosePeriodRegistration.xhtml");
                result.add(action3);
                break;
            }
            // Зарегистрирован
            case CHECK_IN_5: {
                //Зарегистрирован - На исполнении
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.REDIRECT_TO_EXECUTION_80);
                action.setAvailable(true);
                action.setInitialStatus(CHECK_IN_5);
                action.setTargetStatus(ON_EXECUTION_80);
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            case ON_EXECUTION_80: {
                //На исполнении - Исполнен
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.EXECUTE_80);
                action.setAvailable(true);
                action.setInitialStatus(ON_EXECUTION_80);
                action.setTargetStatus(EXECUTE);
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            case EXECUTE:{
                //Исполнен - Архив
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.IN_ARCHIVE_90);
                action.setAvailable(true);
                action.setInitialStatus(EXECUTE);
                action.setTargetStatus(IN_ARCHIVE_100);
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            case IN_ARCHIVE_100:
                break;
            case EXTRACT:
                break;
            case REDIRECT:
                break;
            case CANCEL_150:
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
                case REDIRECT_TO_CONSIDERATION_1: {
                    success = helper.checkInternalDocumentPropertiesForReview(document);
                    processedMessage = document.getWFResultDescription();
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                    break;
                }
                case REGISTRATION_CLOSE_PERIOD_551: {
                    success = helper.setInternalRegistrationNumberOnOutDate(document);
                    processedMessage = document.getWFResultDescription();
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                    break;
                }
                case CHECK_IN_5: {
                    success = helper.setInternalRegistrationNumber(document);
                    processedMessage = document.getWFResultDescription();
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                    break;
                }
                case REDIRECT_TO_AGREEMENT: {
                    success =true;
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                    break;
                }
                case REDIRECT_TO_EXECUTION_80: {
                    success = true;
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                    break;
                }
                case EXECUTE_80: {
                    success = true;
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                    break;
                }
                case IN_ARCHIVE_90: {
                    success = helper.checkInternalPropertiesForArchiving(document);
                    processedMessage = document.getWFResultDescription();
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                    break;
                }
                case CANCEL_150: {
                    success = true;
                    //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                    break;
                }
                default:
                    log.warn("{} is only document state changing action!", workflowAction.getAction());
                    success = true;
                    break;
            }
            if (success) {
                if (StringUtils.isEmpty(workflowAction.getUi())) {
                    finish();
                    state = State.PROCESSED;
                } else {
                    state = State.WAIT_FOR_USER_INPUT;
                }
            } else {
                state = State.PROCESSED;
            }
        } catch (Exception e) {
            this.processedMessage = "Внутренняя ошибка";
            state = State.PROCESSED;
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
        internalDocumentDao.update(document);
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
