package ru.efive.dms.uifaces.beans.workflow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.workflow.Action;
import ru.entity.model.workflow.Transition;
import ru.hitsl.sql.dao.interfaces.TransitionDao;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ViewScopedController("internalWorkflow")
public class InternalDocumentWorkflow extends AbstractWorkflow<InternalDocument, InternalDocumentDao> {

    @Autowired
    @Qualifier("numerationService")
    private NumerationService numerationService;

    @Autowired
    public InternalDocumentWorkflow(
            @Qualifier("authData") final AuthorizationData authData,
            @Qualifier("internalDocumentDao") final InternalDocumentDao dao,
            @Qualifier("transitionDao") final TransitionDao transitionDao) {
        super(authData, dao, transitionDao);
    }


    @Override
    public boolean process(Transition transition, InternalDocument document) {
        final Action action = transition.getAction();
        if (Action.TO_CONSIDERATION.equals(action)) {
            return checkInternalDocumentPropertiesForReview(document);
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
        } else if (Action.CUSTOM_REGISTER.equals(action)) {
            return setInternalRegistrationNumberOnOutDate(document);
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
        } else if (Action.REGISTER.equals(action)) {
            return setInternalRegistrationNumber(document);
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
        } else if (Action.TO_AGREEMENT.equals(action)) {
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
            return true;
        } else if (Action.TO_EXECUTION.equals(action)) {
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
            return true;
        } else if (Action.EXECUTE.equals(action)) {
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
            return true;
        } else if (Action.TO_ARCHIVE.equals(action)) {
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
            return true;
        } else if (Action.CANCEL.equals(action)) {
            //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
            return true;
        } else {
            log.warn("{} is only document state changing transition!", transition);
            return true;
        }
    }


    public boolean setInternalRegistrationNumber(InternalDocument doc) {
        log.info("Call->setInternalRegistrationNumber({})", doc);
        boolean result = true;
        if (doc.getController() == null) {
            addWarning("Необходимо выбрать Руководителя");
            result = false;
        }
        if (doc.getResponsible() == null) {
            addWarning("Необходимо выбрать Контроль исполнения");
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


    public boolean checkInternalDocumentPropertiesForReview(InternalDocument doc) {
        boolean result = true;
        if (doc.getController() == null) {
            addWarning("Необходимо указать руководителя");
            result = false;
        }
        if (doc.getResponsible() == null) {
            addWarning("Необходимо указать ответственного");
            result = false;
        }
        return result;
    }


    public boolean setInternalRegistrationNumberOnOutDate(InternalDocument doc) {
        boolean result = true;
        if (doc.getController() == null) {
            addWarning("Необходимо выбрать Руководителя");
            result = false;
        }
        if (doc.getResponsible() == null) {
            addWarning("Необходимо выбрать Контроль исполнения");
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
        if (doc.getRegistrationDate() == null || !doc.getRegistrationDate().toLocalDate().isBefore(LocalDate.now())) {
            addWarning("Дата регистрации должна быть указана до текущей даты");
            result = false;
        }
        Map<String, Object> in_filters = new HashMap<>();
        //TODO вычистить эту ересь при внедрении нумераторов
        in_filters.put("registrationNumber", doc.getRegistrationNumber());
        in_filters.put("DEPRECATED_REGISTRATION_DATE", doc.getRegistrationDate());
        in_filters.put(DocumentSearchMapKeys.FORM, doc.getForm());
        List<InternalDocument> copyDocuments = dao.findDocumentsByCriteria(in_filters, false);
        if (!copyDocuments.isEmpty()) {
            addWarning("Документ под таким номером уже существует");
            for (InternalDocument internalDocument : copyDocuments) {
                addWarning(internalDocument.getId() + "-\'" + internalDocument.getRegistrationNumber() + "\'");
            }
            result = false;
        }
        log.debug("valid: {}", result);
        return result;
    }
}
