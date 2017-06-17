package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.workflow.Action;
import ru.entity.model.workflow.Transition;
import ru.hitsl.sql.dao.interfaces.TransitionDao;
import ru.hitsl.sql.dao.interfaces.document.OutgoingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

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
            @Qualifier("outgoingDocumentDao") final OutgoingDocumentDao dao,
            @Qualifier("transitionDao") final TransitionDao transitionDao) {
        super(authData, dao, transitionDao);
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
    public boolean process(Transition transition, OutgoingDocument document) {
        final Action action = transition.getAction();
        if (Action.REGISTER.equals(action)) {
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_INCOMING_EXECUTORS);
            return setOutgoingRegistrationNumber(document);
        } else {
            log.warn("{} is only document state changing transition!", transition);
            return true;
        }
    }
}


