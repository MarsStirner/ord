package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.enums.DocumentAction;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.List;

import static ru.entity.model.enums.DocumentStatus.*;

/**
 * Created by EUpatov on 13.04.2017.
 */
@ViewScopedController("outgoingWorkflow")
public class OutgoingDocumentWorkflow extends AbstractWorkflow<OutgoingDocument, OutgoingDocumentDao> {
    @Autowired
    @Qualifier("numerationService")
    private NumerationService numerationService;

    @Autowired
    public OutgoingDocumentWorkflow(
            @Qualifier("authData") final AuthorizationData authData,
            @Qualifier("outgoingDocumentDao") final OutgoingDocumentDao dao) {
        super(authData, dao);
    }


    public boolean setOutgoingRegistrationNumber(OutgoingDocument doc) {
        log.info("Call->setOutgoingRegistrationNumber({})", doc);
        boolean result = true;
        if (doc.getController() == null) {
            addWarning("Необходимо выбрать Руководителя");
            result = false;
        }
        if (doc.getExecutor() == null) {
            addWarning("Необходимо выбрать Ответственного исполнителя");
            result = false;
        }
        if (doc.getContragent() == null) {
            addWarning("Необходимо выбрать Адресата");
            result = false;
        }
        if (doc.getShortDescription() == null || doc.getShortDescription().equals("")) {
            addWarning("Необходимо заполнить Краткое содержание");
            result = false;

        }
        log.debug("valid: {}", result);
        return result && numerationService.registerDocument(doc);
    }


    @Override
    public List<WorkflowAction> getActions(OutgoingDocument document, AuthorizationData authData) {
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
            case ON_CONSIDERATION: {
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


    @Override
    public boolean process(WorkflowAction action, OutgoingDocument document) {
        switch (action.getAction()) {
            case CHECK_IN_80: {
                return setOutgoingRegistrationNumber(document);
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_INCOMING_EXECUTORS);

            }
            default:
                log.warn("{} is only document state changing action!", action.getAction());
                return true;
        }
    }
}


