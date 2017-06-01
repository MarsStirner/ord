package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.enums.DocumentAction;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.ArrayList;
import java.util.List;

import static ru.entity.model.enums.DocumentStatus.*;

@ViewScopedController("requestWorkflow")
public class RequestDocumentWorkflow extends AbstractWorkflow<RequestDocument, RequestDocumentDao> {


    @Autowired
    @Qualifier("numerationService")
    private NumerationService numerationService;

    @Autowired
    public RequestDocumentWorkflow(
            @Qualifier("authData") final AuthorizationData authData,
            @Qualifier("requestDocumentDao") final RequestDocumentDao dao) {
        super(authData, dao);
    }

    @Override
    public List<WorkflowAction> getActions(RequestDocument document, AuthorizationData authData) {
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
            case CHECK_IN_2: {
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


    @Override
    public boolean process(WorkflowAction action, RequestDocument document) {
        switch (action.getAction()) {
            case CHECK_IN_1: {
                return setRequestRegistrationNumber(document);
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_INCOMING_EXECUTORS);
            }
            default:
                log.warn("{} is only document state changing action!", action.getAction());
                return true;
        }
    }

    private boolean setRequestRegistrationNumber(RequestDocument document) {
        boolean result = false;
        if ((document.getSenderType() == null)) {
            addWarning("Необходимо указать Тип отправителя документа");
            result = false;
        }

        if ((document.getSenderType().getValue().equals("Физическое лицо")) && (document.getSenderLastName() == null)) {
            addWarning("Необходимо указать Фамилию адресанта");
            result = false;
        }
        if ((document.getSenderType().getValue().equals("Юридическое лицо")) && (document.getContragent() == null)) {
            addWarning("Необходимо указать Корреспондента");
            result = false;
        }
        if ((document.getRecipientUsers() == null || document.getRecipientUsers().size() == 0) && (document.getRecipientGroups() == null || document
                .getRecipientGroups().size() == 0)) {
            addWarning("Необходимо выбрать Адресатов");
            result = false;
        }
        if (document.getShortDescription() == null || document.getShortDescription().equals("")) {
            addWarning("Необходимо заполнить Краткое содержание");
            result = false;
        }

        if (!result) {
            log.warn("End. Validation failed: '{}'", getWarnings());
            return false;
        }
        log.debug("validation success");
        return numerationService.fillDocumentNumber(document);
    }


}
