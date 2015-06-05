package ru.hitsl.sql.dao;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.slf4j.LoggerFactory;
import ru.entity.model.document.Task;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.referenceBook.DocumentForm;
import ru.external.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;
import ru.util.ApplicationHelper;

import java.util.*;

import static ru.util.ApplicationHelper.getNextDayDate;

public class TaskDAOImpl extends DocumentDAO<Task> {

    static {
        logger = LoggerFactory.getLogger("TASK_DAO");
    }

    @Override
    protected Class<Task> getPersistentClass() {
        return Task.class;
    }

    /**
     * Может ли пользователь получить доступ к документу благодаря ассоциациям (поручениям)
     *
     * @param auth   Авторизационные данные
     * @param docKey идентификатор документа вида "incoming_000"
     */
    public boolean isAccessGrantedByAssociation(AuthorizationData auth, String docKey) {
        final DetachedCriteria searchCriteria = getListCriteria();
        applyAccessCriteria(searchCriteria, auth);
        searchCriteria.add(
                Restrictions.or(
                        Restrictions.eq("rootDocumentId", docKey),
                        Restrictions.eq("parent.id", ApplicationHelper.getIdFromUniqueIdString(docKey))
                )
        );
        return (getCountOf(searchCriteria) > 0);
    }

    public int countDocuments(final Map<String, Object> filters, final boolean showDeleted, final boolean showDrafts) {
        final DetachedCriteria criteria = getListCriteria();
        addDraftsAndDeletedRestrictions(criteria, showDeleted, showDrafts);
        applyFilterMapCriteria(criteria, filters);
        return (int)getCountOf(criteria);
    }

    /**
     * Получить список поручений, у которого в качесвте исходного документа идет ссылка на требуемый документ
     *
     * @param rootId      уникальный идентификатор исходного документа (например "incoming_0001")
     * @param showDeleted включать ли в список поручения с флагом deleted = true
     * @return список поручения у которых исходный документ равен заданному
     */
    public List<Task> getTaskListByRootDocumentId(final String rootId, final boolean showDeleted) {
        logger.debug("Call -> getTaskListByRootDocumentId(\"{}\", {})", rootId, showDeleted);
        final DetachedCriteria criteria = getListCriteria();
        addDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("rootDocumentId", rootId));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Получить список подпоручений рекурсивно, начиная с заданного
     *
     * @param parentId    ид основного поручения
     * @param showDeleted включать ли в результат удаленные
     * @return список подпоручений
     */
    public List<Task> getChildrenTaskByParentId(int parentId, boolean showDeleted) {
        logger.debug("Call -> getChildrenTaskByParentId({}, {})", parentId, showDeleted);
        if (parentId != 0) {
            final DetachedCriteria criteria = getListCriteria();
            addDeletedRestriction(criteria, showDeleted);
            criteria.add(Restrictions.eq("parent.id", parentId));
            //Sorting по дате создания а потом по номеру
            criteria.addOrder(Order.desc("creationDate"));
            criteria.addOrder(Order.asc("taskNumber"));

            final List<Task> subResult = getHibernateTemplate().findByCriteria(criteria);
            final List<Task> result = new ArrayList<Task>(subResult.size());
            for (Task task : subResult) {
                result.add(task);
                result.addAll(getChildrenTaskByParentId(task.getId(), showDeleted));
            }
            return result;
        } else {
            logger.warn("parentId  = 0. return empty list");
            return new ArrayList<Task>(0);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа со списками документов
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить самый простой критерий для отбора документов, без лишних FETCH
     *
     * @return обычно критерий для документов с DISTINCT
     */
    @Override
    public DetachedCriteria getSimplestCriteria() {
        return DetachedCriteria.forClass(Task.class, "this").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

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
        final DetachedCriteria result = getSimplestCriteria();
        result.createAlias("author", "author", CriteriaSpecification.INNER_JOIN);
        result.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
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
        result.createAlias("initiator", "initiator", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("history", "history", CriteriaSpecification.LEFT_JOIN);
        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Применитиь к текущим критериям огарничения сложного фильтра
     *
     * @param criteria текущий критерий, в который будут добавлены условия  (НЕ менее LIST_CRITERIA)
     * @param filters  сложный фильтр (карта)
     */
    @Override
    public void applyFilterMapCriteria(final DetachedCriteria criteria, final Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            logger.debug("FilterMapCriteria: null or empty. Skip.");
            return;
        }
        final Conjunction conjunction = Restrictions.conjunction();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (StringUtils.isEmpty(key)) {
                // Пропустить запись с пустым ключом
                logger.warn("FilterMapCriteria: empty key for \'{}\'", value);
            } else if ("rootDocumentId".equals(key)) {
                conjunction.add(Restrictions.eq("rootDocumentId", value));
            } else if ("taskDocumentId".equals(key)) {
                final Disjunction disjunction = Restrictions.disjunction();
                disjunction.add(Restrictions.ilike("rootDocumentId", "task_%"));
                disjunction.add(Restrictions.isEmpty("rootDocumentId"));
                disjunction.add(Restrictions.isNull("rootDocumentId"));
                conjunction.add(disjunction);
            } else if (DocumentSearchMapKeys.REGISTRATION_NUMBER_KEY.equals(key)) {
                conjunction.add(Restrictions.ilike("taskNumber", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.START_REGISTRATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("registrationDate", value));
            } else if (DocumentSearchMapKeys.END_REGISTRATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("registrationDate", getNextDayDate((Date) value)));
            } else if (DocumentSearchMapKeys.START_CREATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.ge("creationDate", value));
            } else if (DocumentSearchMapKeys.END_CREATION_DATE_KEY.equals(key)) {
                conjunction.add(Restrictions.le("creationDate", getNextDayDate((Date) value)));
            } else if (DocumentSearchMapKeys.SHORT_DESCRIPTION_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.ilike("shortDescription", (String) value, MatchMode.ANYWHERE));
            } else if (DocumentSearchMapKeys.STATUS_KEY.equals(key) && StringUtils.isNotEmpty((String) value)) {
                conjunction.add(Restrictions.eq("statusId", Integer.valueOf((String) value)));
            } else if (DocumentSearchMapKeys.CONTROLLER_KEY.equals(key)) {
                createUserEqRestriction(conjunction, key, "controller.id", value);
            } else if (DocumentSearchMapKeys.AUTHOR_KEY.equals(key)) {
                createUserEqRestriction(conjunction, key, "author.id", value);
            } else if (DocumentSearchMapKeys.AUTHORS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, key, "author.id", value);
            } else if (DocumentSearchMapKeys.EXECUTORS_KEY.equals(key)) {
                createUserListInRestriction(conjunction, key, "executors.id", value);
            } else if (DocumentSearchMapKeys.FORM_KEY.equals(key)) {
                try {
                    final DocumentForm form = (DocumentForm) value;
                    conjunction.add(Restrictions.eq("form.id", form.getId()));
                } catch (ClassCastException e) {
                    logger.error("Exception while forming FilterMapCriteria: [{}]=\'{}\' IS NOT DocumentForm. Non critical, continue...", key, value);
                }
            } else if (DocumentSearchMapKeys.FORM_VALUE_KEY.equals(key)) {
                conjunction.add(Restrictions.eq("form.value", value));
            } else if (DocumentSearchMapKeys.FORM_CATEGORY_KEY.equals(key)) {
                conjunction.add(Restrictions.eq("form.category", value));
            } else {
                logger.warn("FilterMapCriteria: Unknown key \'{}\' (value =\'{}\')", key, value);
            }
        }
        criteria.add(conjunction);
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
    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isEmpty(filter)) {
            logger.debug("FilterCriteria: empty or null filter. Skip.");
            return;
        }
        final Disjunction disjunction = Restrictions.disjunction();
        disjunction.add(createDateLikeTextRestriction("creationDate", filter));
        disjunction.add(createDateLikeTextRestriction("controlDate", filter));
        disjunction.add(Restrictions.ilike("taskNumber", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.lastName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.middleName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("executors.firstName", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));
        disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));

        //TODO справочник в БД
        final List<DocumentStatus> statuses = DocumentType.getTaskStatuses();
        final Set<Integer> statusIdList = new HashSet<Integer>(statuses.size());
        for (DocumentStatus current : statuses) {
            if (current.getName().contains(filter)) {
                statusIdList.add(current.getId());
            }
        }
        if (!statusIdList.isEmpty()) {
            disjunction.add(Restrictions.in("statusId", statusIdList));
        }
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

    /**
     * Получить список проектных статусов
     *
     * @return список идентифкаторов проектных статусов
     */
    @Override
    public Set<Integer> getDraftStatuses() {
        return ImmutableSet.of(DocumentStatus.DRAFT.getId());
    }


}