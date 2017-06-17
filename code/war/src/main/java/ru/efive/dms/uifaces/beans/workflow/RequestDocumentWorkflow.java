package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.workflow.Action;
import ru.entity.model.workflow.Transition;
import ru.hitsl.sql.dao.interfaces.TransitionDao;
import ru.hitsl.sql.dao.interfaces.document.RequestDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

@ViewScopedController("requestWorkflow")
public class RequestDocumentWorkflow extends AbstractWorkflow<RequestDocument, RequestDocumentDao> {


    @Autowired
    @Qualifier("numerationService")
    private NumerationService numerationService;

    @Autowired
    public RequestDocumentWorkflow(
            @Qualifier("authData") final AuthorizationData authData,
            @Qualifier("requestDocumentDao") final RequestDocumentDao dao,
            @Qualifier("transitionDao") final TransitionDao transitionDao) {
        super(authData, dao, transitionDao);
    }


    @Override
    public boolean process(Transition transition, RequestDocument document) {
        final Action action = transition.getAction();
        if (Action.REGISTER.equals(action)) {
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_INCOMING_EXECUTORS);
            return setRequestRegistrationNumber(document);
        } else {
            log.warn("{} is only document state changing transition!", transition);
            return true;
        }
    }

    private boolean setRequestRegistrationNumber(RequestDocument doc) {
        boolean result = true;
        if ((doc.getSenderType() == null)) {
            addWarning("Необходимо указать Тип отправителя документа");
            result = false;
        }
        if ((doc.getSenderType().getValue().equals("Физическое лицо")) && (doc.getSenderLastName() == null)) {
            addWarning("Необходимо указать Фамилию адресанта");
            result = false;
        }
        if ((doc.getSenderType().getValue().equals("Юридическое лицо")) && (doc.getContragent() == null)) {
            addWarning("Необходимо указать Корреспондента");
            result = false;
        }
        if ((doc.getRecipientUsers() == null || doc.getRecipientUsers().size() == 0) && (doc.getRecipientGroups() == null || doc
                .getRecipientGroups().size() == 0)) {
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
