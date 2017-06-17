package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.IncomingDocument;
import ru.entity.model.workflow.Action;
import ru.entity.model.workflow.Transition;
import ru.hitsl.sql.dao.interfaces.TransitionDao;
import ru.hitsl.sql.dao.interfaces.document.IncomingDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;


@ViewScopedController("incomingWorkflow")
public class IncomingDocumentWorkflow extends AbstractWorkflow<IncomingDocument, IncomingDocumentDao> {


    @Autowired
    private NumerationService numerationService;

    @Autowired
    IncomingDocumentWorkflow(AuthorizationData authData, IncomingDocumentDao dao, TransitionDao transitionDao) {
        super(authData, dao, transitionDao);
    }

    @Override
    public boolean process(Transition transition, IncomingDocument document) {
        final Action action = transition.getAction();
        if (Action.REGISTER.equals(action)) {
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_INCOMING_EXECUTORS);
            return setIncomingRegistrationNumber(document);
        } else {
            log.warn("{} is only document state changing transition!", transition);
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
