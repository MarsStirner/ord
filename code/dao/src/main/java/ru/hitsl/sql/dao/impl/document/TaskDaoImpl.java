package ru.hitsl.sql.dao.impl.document;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.Task;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.workflow.Status;
import ru.hitsl.sql.dao.impl.mapped.DocumentDaoImpl;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;
import ru.util.ApplicationHelper;
import ru.util.Node;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hibernate.criterion.MatchMode.ANYWHERE;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

@Repository("taskDao")
@Transactional(propagation = Propagation.MANDATORY)
public class TaskDaoImpl extends DocumentDaoImpl<Task> implements TaskDao {

    /**
     * Ограничение на максимальную глубину рекурсии
     */
    private static final int MAX_RECURSIVE_DEPTH = 10;


    @Override
    public Class<Task> getEntityClass() {
        return Task.class;
    }

    /**
     * Может ли пользователь получить доступ к документу благодаря ассоциациям (поручениям)
     *
     * @param auth   Авторизационные данные
     * @param docKey идентификатор документа вида "incoming_000"
     */
    @Override
    public boolean isAccessGrantedByAssociation(AuthorizationData auth, String docKey) {
        final DetachedCriteria searchCriteria = getListCriteria();
        applyAccessCriteria(searchCriteria, auth);
        searchCriteria.add(
                Restrictions.or(
                        Restrictions.eq("rootDocumentId", docKey),
                        Restrictions.eq("parent.id", ApplicationHelper.getIdFromUniqueIdString(docKey))
                )
        );
        return (countItems(searchCriteria) > 0);
    }

    /**
     * Получить список поручений, у которого в качесвте исходного документа идет ссылка на требуемый документ
     *
     * @param rootId      уникальный идентификатор исходного документа (например "incoming_0001")
     * @param showDeleted включать ли в список поручения с флагом deleted = true
     * @return список поручения у которых исходный документ равен заданному
     */
    @Override
    public List<Task> getTaskListByRootDocumentId(final String rootId, final boolean showDeleted) {
        log.debug("Call -> getTaskListByRootDocumentId(\"{}\", {})", rootId, showDeleted);
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("rootDocumentId", rootId));
        return getItems(criteria);
    }

    /**
     * Получить список подпоручений рекурсивно, начиная с заданного
     *
     * @param parentId    ид основного поручения
     * @param showDeleted включать ли в результат удаленные
     * @return список подпоручений
     */
    @Override
    public List<Task> getChildrenTaskByParentId(int parentId, boolean showDeleted) {
        log.debug("Call -> getChildrenTaskByParentId({}, {})", parentId, showDeleted);
        if (parentId != 0) {
            final DetachedCriteria criteria = getListCriteria();
            applyDeletedRestriction(criteria, showDeleted);
            criteria.add(Restrictions.eq("parent.id", parentId));
            //Sorting по дате создания а потом по номеру
            applyOrder(criteria, "creationDate", false);
            applyOrder(criteria, "registrationNumber", true);

            final List<Task> subResult = getItems(criteria);
            final List<Task> result = new ArrayList<>(subResult.size());
            for (Task task : subResult) {
                result.add(task);
                result.addAll(getChildrenTaskByParentId(task.getId(), showDeleted));
            }
            return result;
        } else {
            log.warn("parentId  = 0. return empty list");
            return new ArrayList<>(0);
        }
    }

    @Override
    public void fetchTreeRecursive(String documentId, Node<Task> node, int depth) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, false);
        criteria.add(Restrictions.eq("rootDocumentId", documentId));
        criteria.add(Restrictions.eqOrIsNull("parent.id", node.getData() == null ? null : node.getData().getId()));
        //Not lambda cause IDEA not mark recursive call in lambdas at left-side panel =)
        for (Task x : getItems(criteria)) {
            Node<Task> subNode = new Node<>(x);
            node.addChild(subNode);
            if (depth <= MAX_RECURSIVE_DEPTH) {
                fetchTreeRecursive(documentId, subNode, depth + 1);
            } else {
                log.warn("MAX_RECURSIVE_DEPTH[{}] reached. Not fetch childrens under {}", MAX_RECURSIVE_DEPTH, x);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа со списками документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить критерий для отбора Документов и их показа в расширенных списках
     * Обычно:
     * Автор - INNER
     * Руководитель - LEFT
     * Вид документа - LEFT
     * ++ В зависимости от вида
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = super.getListCriteria();
        result.createAlias("executors", "executors", LEFT_OUTER_JOIN);
        return result;
    }

    /**
     * Получить критерий для отбора Документов с максимальным количеством FETCH
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    @Override
    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        result.createAlias("initiator", "initiator", LEFT_OUTER_JOIN);
        result.createAlias("history", "history", LEFT_OUTER_JOIN);
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void addDocumentMapFilter(Conjunction conjunction, String key, Object value) {
        switch (key) {
            case DocumentSearchMapKeys.EXECUTORS:
                //Исполнители
                conjunction.add(Restrictions.in("executors", value));
                break;
            case DocumentSearchMapKeys.ROOT_DOCUMENT_ID:
                //Документ-основание
                conjunction.add(Restrictions.eq("rootDocumentId", value));
                break;
            case DocumentSearchMapKeys.TASK_DOCUMENT_ID:
                //Есть поручение-основание
                final Disjunction disjunction = Restrictions.disjunction();
                disjunction.add(Restrictions.ilike("rootDocumentId", "task_%"));
                disjunction.add(Restrictions.eq("rootDocumentId", ""));
                disjunction.add(Restrictions.isNull("rootDocumentId"));
                conjunction.add(disjunction);
                break;
            default:
                log.warn("FilterMapCriteria: Unknown key \'{}\' (value =\'{}\')", key, value);
        }
    }


    /**
     * Производит поиск заданной строки в (по условию ИЛИ [дизъюнкция]):
     * заданных полях сущности
     * - дата создания
     * - номер
     * - исполнитель (ФИО)
     * - вид документа
     * - срок исполнения
     * - статус
     * - краткое содержание
     *
     * @param criteria критерий отбора в который будет добавлено поисковое условие (НЕ менее LIST_CRITERIA)
     * @param filter   условие поиска
     */
    @Override
    public void applyFilter(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isEmpty(filter)) {
            log.debug("FilterCriteria: empty or null filter. Skip.");
            return;
        }
        final Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(createDateLikeTextRestriction("creationDate", filter));
        disjunction.add(createDateLikeTextRestriction("controlDate", filter));
        disjunction.add(Restrictions.ilike("registrationNumber", filter, ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.lastName", filter, ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.middleName", filter, ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.firstName", filter, ANYWHERE));
        disjunction.add(Restrictions.ilike("form.value", filter, ANYWHERE));
        disjunction.add(Restrictions.ilike("shortDescription", filter, ANYWHERE));

//        //TODO справочник в БД
//        final List<DocumentStatus> statuses = DocumentType.getTaskStatuses();
//        final Set<Integer> statusIdList = new HashSet<>(statuses.size());
//        for (DocumentStatus current : statuses) {
//            if (current.getName().contains(filter)) {
//                statusIdList.add(current.getId());
//            }
//        }
//        if (!statusIdList.isEmpty()) {
//            disjunction.add(Restrictions.in("statusId", statusIdList));
//        }
        criteria.add(disjunction);
    }

    /**
     * Применить ограничения допуска для документов
     *
     * @param criteria исходный критерий   (минимум LIST_CRITERIA)
     * @param auth     данные авторизации
     */
    @Override
    public void applyAccessCriteria(final DetachedCriteria criteria, final AuthorizationData auth) {
        if (!auth.isAdministrator()) {
            final Disjunction disjunction = Restrictions.disjunction();
            final Set<Integer> userIds = auth.getUserIds();
            disjunction.add(Restrictions.in("author.id", userIds));
            disjunction.add(Restrictions.in("executors.id", userIds));
            disjunction.add(Restrictions.in("initiator.id", userIds));
            disjunction.add(Restrictions.in("controller.id", userIds));
            criteria.add(disjunction);
        }
    }
}