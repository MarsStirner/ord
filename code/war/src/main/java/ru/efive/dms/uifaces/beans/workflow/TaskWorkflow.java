package ru.efive.dms.uifaces.beans.workflow;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.document.HistoryEntry;
import ru.entity.model.document.Task;
import ru.entity.model.enums.DocumentAction;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.DocumentEntity;

import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.document.*;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.util.ApplicationHelper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.entity.model.enums.DocumentStatus.*;

/**
 * Created by EUpatov on 13.04.2017.
 */
@ViewScopedController("taskWorkflow")
public class TaskWorkflow extends AbstractWorkflow<Task, TaskDao> {


    @Autowired
    @Qualifier("numerationService")
    private NumerationService numerationService;

    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;
    @Autowired
    @Qualifier("incomingDocumentDao")
    private IncomingDocumentDao incomingDocumentDao;
    @Autowired
    @Qualifier("internalDocumentDao")
    private InternalDocumentDao internalDocumentDao;
    @Autowired
    @Qualifier("requestDocumentDao")
    private RequestDocumentDao requestDocumentDao;


    @Autowired
    public TaskWorkflow(
            @Qualifier("authData") final AuthorizationData authData,
            @Qualifier("taskDao") final TaskDao dao) {
        super(authData, dao);
    }

    @Override
    public List<WorkflowAction> getActions(Task document, AuthorizationData authData) {
        log.debug("getActions by {} with document: {}", authData.getLogString(), document.getUniqueId());
        final List<WorkflowAction> result = new ArrayList<>();
        switch (document.getDocumentStatus()) {
            // Проект документа
            case DRAFT: {
                // Черновик - На исполнении
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.REDIRECT_TO_EXECUTION_1);
                action.setAvailable(true);
                action.setInitialStatus(DRAFT);
                action.setTargetStatus(ON_EXECUTION_2);
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            // На исполнении - Исполнен
            case ON_EXECUTION_2: {
                final WorkflowAction action2 = new WorkflowAction();
                action2.setAction(DocumentAction.EXECUTED);
                action2.setAvailable(true);
                action2.setInitialStatus(ON_EXECUTION_2);
                action2.setTargetStatus(EXECUTED);
                action2.setNeedHistory(true);
                result.add(action2);

                //На рассмотрении - Отказ
                final WorkflowAction action = new WorkflowAction();
                action.setAction(DocumentAction.CANCEL_25);
                action.setAvailable(document.getForm().getValue().equals("exercise") || document.getForm().getValue().equals("task"));
                action.setInitialStatus(ON_EXECUTION_2);
                action.setTargetStatus(CANCEL_4);
                action.setNeedHistory(true);
                result.add(action);
                break;
            }
            default:
                log.error("NON processed STATE!!!! {}", document.getDocumentStatus());
                break;
        }
        log.info("Total action available = {}", result);
        return result;
    }


    @Override
    public boolean process(WorkflowAction action, Task document) {
        switch (action.getAction()) {
            case REDIRECT_TO_EXECUTION_1: {
                return setTaskRegistrationNumber(document) && cloneTasks(document);
                //spammer.sendMail(document.getExecutors(), document, Spammer.TEMPLATE_INCOMING_EXECUTORS);
            }
            default:
                log.warn("{} is only document state changing action!", action.getAction());
                return true;
        }
    }

    private boolean setTaskRegistrationNumber(Task doc) {
        log.info("Call->setTaskRegistrationNumber({})", doc);
        boolean result = true;
        if (doc.getExecutors() == null || doc.getExecutors().isEmpty()) {
            addWarning("Необходимо выбрать Исполнителя");
            result = false;
        } else {
            for (User user : doc.getExecutors()) {
                if (StringUtils.isEmpty(user.getEmail())) {
                    addWarning("У исполнителя по поручению отсутствует адрес электронной почты");
                    result = false;
                }
            }
        }
        if (StringUtils.isEmpty(doc.getShortDescription())) {
            addWarning("Необходимо заполнить Текст поручения");
            result = false;
        }
        if (!result) {
            log.warn("End. Validation failed: '{}'", getWarnings());
            return false;
        }
        log.debug("validation success");
        if (StringUtils.isNotEmpty(doc.getRegistrationNumber())) {
            //Номер задан - > Нихера не делаем?!
            return true;
        }
        final String key = doc.getRootDocumentId();
        if (StringUtils.isEmpty(key)) {
            //Нет документа-основания -> сквозная нумерация
            return numerationService.fillDocumentNumber(doc);
        } else {
            //Номер не задан
            Map<String, Object> in_filters = Collections.singletonMap("rootDocumentId", key);
            final Integer idInt = ApplicationHelper.getIdFromUniqueIdString(key);
            DocumentEntity rootDocument = null;
            if (key.contains(DocumentType.IncomingDocument.getName())) {
                rootDocument = incomingDocumentDao.getItemBySimpleCriteria(idInt);
            } else if (key.contains(DocumentType.OutgoingDocument.getName())) {
                rootDocument = outgoingDocumentDao.getItemBySimpleCriteria(idInt);
            } else if (key.contains(DocumentType.InternalDocument.getName())) {
                rootDocument = internalDocumentDao.getItemBySimpleCriteria(idInt);
            } else if (key.contains(DocumentType.RequestDocument.getName())) {
                rootDocument = requestDocumentDao.getItemBySimpleCriteria(idInt);
            }
            //TODO hardcoded numeration by rootDocumentId
            //не надо увлеичивать на единицу так как сам документ удже в БД
            final int nextNumber = dao.countItems(null, in_filters, false);
            doc.setRegistrationNumber(rootDocument != null ? rootDocument.getRegistrationNumber() + "/" + nextNumber : String.valueOf(nextNumber));
            doc.setRegistrationDate(LocalDateTime.now());
            return true;
        }
    }


    public boolean cloneTasks(Task doc) {
        try {

            //TODO когда-нибудь, когда мир станет вновь светлым и ясным, когда прекратятся войны, исчезнет коррупция,
            //TODO ну или что более вероятно когда перепишут эту фабрику действий - то этот жуткий костыль превратится в красивое и элегантное решение
            //TODO когда-нибудь... но только не сегодня. @27-10-2014
            if (doc.getExecutors().size() > 1) {
                log.debug("Group task start clone");
                //Групповое поручение
                final Task templateTask = (Task) doc.clone();
                templateTask.setId(null);
                templateTask.getExecutors().clear();
                templateTask.setParent(doc);
                templateTask.getHistory().clear();

                final Map<String, Object> in_filters = new HashMap<>();
                final Matcher matcher = Pattern.compile("(.*)([0-9]+)$").matcher(doc.getRegistrationNumber());
                if (matcher.find()) {
                    templateTask.setRegistrationNumber(matcher.group(1));
                    in_filters.put("rootDocumentId", doc.getRootDocumentId());
                } else {
                    in_filters.put("taskDocumentId", "");
                }

                for (User currentExecutor : doc.getExecutors()) {
                    final Task currentTask = (Task) templateTask.clone();
                    final Set<User> executorsSet = new HashSet<>(1);
                    executorsSet.add(currentExecutor);
                    currentTask.setExecutors(executorsSet);
                    //+1 потому что жизнь-боль и первое поручение еще не сохранено в БД с корректным номером
                    int numberOffset = dao.countItems(null, in_filters, false) + 1;
                    currentTask.setRegistrationNumber(currentTask.getRegistrationNumber().concat(String.valueOf(numberOffset)));
                    Set<HistoryEntry> history = new HashSet<>(1);
                    final HistoryEntry entry = new HistoryEntry();
                    entry.setActionId(DocumentAction.REDIRECT_TO_EXECUTION_1.getId());
                    entry.setCreated(LocalDateTime.now());
                    entry.setCommentary("Создано из группового поручения");
                    entry.setFromStatusId(1);
                    entry.setToStatusId(DocumentStatus.ON_EXECUTION_2.getId());
                    currentTask.setHistory(history);
                    currentTask.setStatus(DocumentStatus.ON_EXECUTION_2);
                    dao.save(currentTask);
                    log.debug("Sub-task[{}] {}", currentTask.getId(), currentTask.getRegistrationNumber());
                    if (log.isTraceEnabled()) {
                        log.trace("Sub-task Info: {}", currentTask);
                    }
                }
                return true;
            } else {
                log.debug("NOT need to clone task");
                return true;
            }
        } catch (Exception e) {
            log.error("Error while cloning tasks:", e);
            return false;
        }
    }

}
