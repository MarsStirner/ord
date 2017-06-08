package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.enums.DocumentAction;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.ArrayList;
import java.util.List;

import static ru.entity.model.enums.DocumentStatus.*;


@ViewScopedController("incomingWorkflow")
public class IncomingDocumentWorkflow extends AbstractWorkflow<IncomingDocument, IncomingDocumentDao> {

    @Autowired
    @Qualifier("numerationService")
    private NumerationService numerationService;

    @Autowired
    public IncomingDocumentWorkflow(
            @Qualifier("authData") final AuthorizationData authData,
            @Qualifier("incomingDocumentDao") final IncomingDocumentDao dao) {
        super(authData, dao);
    }

    @Override
    public List<WorkflowAction> getActions(IncomingDocument document, AuthorizationData authData) {
        log.debug("getActions by {} with document: {}", authData.getLogString(), document.getUniqueId());
        final List<WorkflowAction> result = new ArrayList<>();
        switch (document.getDocumentStatus()) {
            // На регистрации
            case ON_REGISTRATION: {
                //Зарегистрирован
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.CHECK_IN_1);
                action.setAvailable(authData.isAdministrator() || authData.isOfficeManager());
                action.setInitialStatus(ON_REGISTRATION);
                action.setTargetStatus(CHECK_IN_2);
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            //Зарегистрирован
            case CHECK_IN_2: {
                //На исполнении
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.REDIRECT_TO_EXECUTION_2);
                action.setAvailable(true);
                action.setInitialStatus(CHECK_IN_2);
                action.setTargetStatus(ON_EXECUTION_80);
                //TODO не проверять ли юзверя среди исполнителей
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            //На исполнении - Исполнен
            case ON_EXECUTION_80: {
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

    @Override
    public boolean process(WorkflowAction action, IncomingDocument document) {
        switch (action.getAction()) {
            case CHECK_IN_1: {
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_INCOMING_EXECUTORS);
                return setIncomingRegistrationNumber(document);
            }
            default:
                log.warn("{} is only document state changing action!", action.getAction());
                return true;
        }
    }


    public boolean setIncomingRegistrationNumber(IncomingDocument doc) {
        log.info("Call->setIncomingRegistrationNumber({})", doc);
        boolean result = true;
        if (doc.getController() == null) {
            addWarning("Необходимо выбрать Руководителя");
            result = false;
        }
        if (doc.getContragent() == null) {
            addWarning("Необходимо выбрать Корреспондента");
            result = false;
        }
        if ((doc.getRecipientUsers() == null || doc.getRecipientUsers().isEmpty()) && (doc.getRecipientGroups() == null || doc.getRecipientGroups().isEmpty())) {
            addWarning("Необходимо выбрать Адресатов");
            result = false;
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            addWarning("Необходимо заполнить Краткое содержание");
            result = false;
        }
        log.debug("valid: {}", result);
        return result && numerationService.registerDocument(doc);
    }




}
