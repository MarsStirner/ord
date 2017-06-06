package ru.efive.dms.uifaces.beans.workflow;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.enums.DocumentAction;
import ru.hitsl.sql.dao.interfaces.document.InternalDocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.entity.model.enums.DocumentStatus.*;


@ViewScopedController("internalWorkflow")
public class InternalDocumentWorkflow extends AbstractWorkflow<InternalDocument, InternalDocumentDao> {

    @Autowired
    @Qualifier("numerationService")
    private NumerationService numerationService;

    @Autowired
    public InternalDocumentWorkflow(
            @Qualifier("authData") final AuthorizationData authData,
            @Qualifier("internalDocumentDao") final InternalDocumentDao dao) {
        super(authData, dao);
    }

    @Override
    public List<WorkflowAction> getActions(InternalDocument document, AuthorizationData authData) {
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
                action3.setUi("/component/workflow/internalClosePeriodRegistration.xhtml");
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
                action3.setUi("/component/workflow/internalClosePeriodRegistration.xhtml");
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
                action3.setUi("/component/workflow/internalClosePeriodRegistration.xhtml");
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
            case EXECUTE: {
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

    @Override
    public boolean process(WorkflowAction workflowAction, InternalDocument document) {
        switch (workflowAction.getAction()) {
            case REDIRECT_TO_CONSIDERATION_1: {
                return checkInternalDocumentPropertiesForReview(document);
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
            }
            case REGISTRATION_CLOSE_PERIOD_551: {
                return setInternalRegistrationNumberOnOutDate(document);
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
            }
            case CHECK_IN_5: {
                return setInternalRegistrationNumber(document);
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
            }
            case REDIRECT_TO_AGREEMENT: {
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                return true;
            }
            case REDIRECT_TO_EXECUTION_80: {
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                return true;
            }
            case EXECUTE_80: {
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                return true;
            }
            case IN_ARCHIVE_90: {
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                return true;
            }
            case CANCEL_150: {
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_internal_EXECUTORS);
                return true;
            }
            default:
                log.warn("{} is only document state changing action!", workflowAction.getAction());
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
        return result && numerationService.fillDocumentNumber(doc);
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
        in_filters.put(DocumentSearchMapKeys.FORM_KEY, doc.getForm());
        List<InternalDocument> copyDocuments = dao.findDocumentsByCriteria(in_filters, false, true);
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
