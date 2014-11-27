package ru.efive.dms.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.document.Task;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import ru.entity.model.document.IncomingDocument;

public class TaskDAOImpl extends GenericDAOHibernate<Task> {
    private static final Logger logger = LoggerFactory.getLogger("TASK_DAO");

    @Override
    protected Class<Task> getPersistentClass() {
        return Task.class;
    }

    public List<Task> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        final LocalDate currentDate = new LocalDate();
        in_searchCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)", currentDate.getYear() + "%", new StringType()));
        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    public long countAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(in_searchCriteria, filter));
    }

    public List<Task> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(in_searchCriteria, ords, orderAsc);
            } else {
                addOrder(in_searchCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);
    }

    public List<Task> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
    }

    public List<Task> findAllDocumentsOnExecutionByUser(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDeletedRestriction(in_searchCriteria, showDeleted);
        in_searchCriteria.add(Restrictions.eq("statusId", DocumentStatus.ON_EXECUTION_2.getId()));
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(in_searchCriteria, ords, orderAsc);
            } else {
                addOrder(in_searchCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);
    }

    public List<Task> findAllExecutedDocumentsByUser(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDeletedRestriction(in_searchCriteria, showDeleted);
        in_searchCriteria.add(Restrictions.eq("statusId", DocumentStatus.EXECUTED.getId()));
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(in_searchCriteria, ords, orderAsc);
            } else {
                addOrder(in_searchCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);
    }

    /**
     * Поиск резолюций
     *
     * @param parentId - идентификатор родительского документа
     * @return - список резолюций
     */
    @SuppressWarnings("unchecked")
    public List<Task> findResolutionsByParent(int parentId) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (parentId != 0) {
            detachedCriteria.add(Restrictions.eq("parent.id", parentId));
        }
        addOrder(detachedCriteria, "id", true);
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    /**
     * Поиск резолюций по исполнителю
     *
     * @param executorId  - идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @param offset      смещение
     * @param count       кол-во результатов
     * @param orderBy     поле для сортировки
     * @param orderAsc    направление сортировки
     * @return - список резолюций
     */
    @SuppressWarnings("unchecked")
    public List<Task> findResolutionsByExecutor(int executorId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);
        if (executorId > 0) {
            detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executors.id", executorId)));
        }
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
    }

    /**
     * Кол-во резолюций по исполнителю
     *
     * @param executorId  - идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во резолюций
     */
    public long countResolutionsByExecutor(int executorId, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);
        if (executorId > 0) {
            detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executors.id", executorId)));
        }

        return getCountOf(detachedCriteria);
    }

    /**
     * Поиск резолюций по исполнителю
     *
     * @param filter      поисковый фильтр
     * @param executorId  идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @param offset      смещение
     * @param count       кол-во результатов
     * @param orderBy     поле для сортировки
     * @param orderAsc    направление сортировки
     * @return - список резолюций
     */
    @SuppressWarnings("unchecked")
    public List<Task> findResolutionsByExecutor(String filter, int executorId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);
        if (executorId > 0) {
            detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executors.id", executorId)));
        }
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
    }

    /**
     * Кол-во резолюций по исполнителю
     *
     * @param filter      поисковый фильтр
     * @param executorId  идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во резолюций
     */
    public long countResolutionsByExecutor(String filter, int executorId, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);
        if (executorId > 0) {
            detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executors.id", executorId)));
        }

        return getCountOf(getSearchCriteria(detachedCriteria, filter));
    }

    /**
     * Поиск резолюций
     *
     * @param executorId - идентификатор пользователя
     * @param parentId   - идентификатор родительского документа
     * @return - список резолюций
     */
    @SuppressWarnings("unchecked")
    public List<Task> findResolutionsByParent(int executorId, String parentId) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        if (StringUtils.isNotEmpty(parentId)) {
            detachedCriteria.add(Restrictions.eq("parent.id", parentId));
        }
        if (executorId > 0) {
            detachedCriteria.add(Restrictions.disjunction().add(Restrictions.eq("author.id", executorId)).add(Restrictions.eq("executors.id", executorId)));
        }

        addOrder(detachedCriteria, "id", true);

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }


    @SuppressWarnings("unchecked")
    public List<Task> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);
        int userId = user.getId();
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.DRAFT.getId()));

        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
    }

    /**
     * Кол-во документов по автору
     *
     * @param user        пользователь
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDraftDocumentsByAuthor(User user, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);
        int userId = user.getId();
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.DRAFT.getId()));
        return getCountOf(detachedCriteria);
    }

    @SuppressWarnings("unchecked")
    public List<Task> findAllRegistratedDocuments(String filter, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);

        detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.DRAFT.getId())));

        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
    }

    @SuppressWarnings("unchecked")
    public List<Task> findAllRegistratedDocumentsByRootFormat(String key, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.DRAFT.getId())));
        if (key == null || key.isEmpty() || !key.contains("task")) {
            detachedCriteria.add(Restrictions.ilike("rootDocumentId", key + "%"));
        } else {
            detachedCriteria.add(Restrictions.eq("rootDocumentId", key));
        }
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    /**
     * Кол-во всех зарегистрированных документов по
     *
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countAllRegistratedDocuments(String filter, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.DRAFT.getId())));
        return getCountOf(getSearchCriteria(detachedCriteria, filter));
    }


    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            //TODO this_ надо бы как- нибудь попроще, без привязки к Hibernate
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.creationDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.controlDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
            disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("taskNumber", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("executors.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executors.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executors.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("initiator.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("initiator.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("initiator.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
        return criteria;
    }


    protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        Disjunction disjunction = Restrictions.disjunction();
        int userId = user.getId();
        if (!user.isAdministrator()) {
            disjunction.add(Restrictions.eq("author.id", userId));
            disjunction.add(Restrictions.eq("executors.id", userId));
            disjunction.add(Restrictions.eq("initiator.id", userId));
            disjunction.add(Restrictions.eq("controller.id", userId));
            detachedCriteria.add(disjunction);
        }
        return detachedCriteria;
    }

    public Task findDocumentById(String id) {
        logger.debug("Call findDocumentById({})", id);
        int identifier;
        try {
            identifier = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            logger.warn("Identifier is not integer: \"{}\" return null", id);
            return null;
        }
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("id", identifier));
        List<Task> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!in_results.isEmpty()) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
        if ((in_map != null) && (in_map.size() > 0)) {
            Conjunction conjunction = Restrictions.conjunction();
            String in_key = "taskNumber";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "rootDocumentId";
            if (in_map.containsKey(in_key) && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.eq(in_key, in_map.get(in_key).toString()));
            }

            in_key = "taskDocumentId";
            if (in_map.containsKey(in_key) && in_map.get(in_key).toString().length() > 0) {
                Disjunction disjunction = Restrictions.disjunction();
                disjunction.add(Restrictions.ilike("rootDocumentId", "task_%"));
                disjunction.add(Restrictions.isEmpty("rootDocumentId"));
                disjunction.add(Restrictions.isNull("rootDocumentId"));
                conjunction.add(disjunction);
            }

            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
            in_key = "creationDate";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(" + in_key + ", '%d.%m.%Y') like lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
            }
            if ((in_map.get("form") != null) || (in_map.get("formValue") != null) || (in_map.get("formCategory") != null)) {
                criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
            }
            in_key = "form";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike(in_key + ".value", ((DocumentForm) in_map.get(in_key)).getValue(), MatchMode.ANYWHERE));
            }
            in_key = "formValue";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike("form.value", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }
            in_key = "formCategory";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike("form.category", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }
            in_key = "formDescription";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike("form.description", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }
            if ((in_map.get("exerciseType") != null) || (in_map.get("exerciseTypeValue") != null) || (in_map.get("exerciseTypeCategory") != null)) {
                criteria.createAlias("exerciseType", "exerciseType", CriteriaSpecification.LEFT_JOIN);
            }
            in_key = "exerciseType";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike(in_key + ".value", ((DocumentForm) in_map.get(in_key)).getValue(), MatchMode.ANYWHERE));
            }
            in_key = "exerciseTypeValue";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike("exerciseType.value", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }
            in_key = "exerciseTypeCategory";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike("exerciseType.category", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }
            in_key = "exerciseTypeDescription";
            if (in_map.containsKey(in_key)) {
                conjunction.add(Restrictions.ilike("exerciseType.description", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }
            //TODO: поиск по адресатам

            criteria.add(conjunction);
        }
        return criteria;
    }

    /**
     * Может ли пользователь получить доступ к документу благодаря ассоциациям (поручениям)
     *
     * @param user   пользователь
     * @param docKey идентификатор документа вида "incoming_000"
     */
    public boolean isAccessGrantedByAssociation(User user, String docKey) {
        DetachedCriteria searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(searchCriteria, false, false);
        applyAliases(searchCriteria);
        searchCriteria.add(Restrictions.or(Restrictions.eq("rootDocumentId", docKey), Restrictions.eq("parent.id", ApplicationHelper.getIdFromUniqueIdString(docKey))));
        searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return (getCountOf(searchCriteria) > 0);
    }


    public Task getTask(int id) {
        logger.debug("Call -> getTask({})", id);
        final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.idEq(id));
        detachedCriteria.setFetchMode("history", FetchMode.JOIN);
        final List<Task> resultList = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!resultList.isEmpty()) {
            return resultList.iterator().next();
        }
        return null;
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
        final DetachedCriteria criteria = DetachedCriteria.forClass(getPersistentClass());
        criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
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
            final DetachedCriteria criteria = DetachedCriteria.forClass(getPersistentClass());
            criteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
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


    public long countAllDocumentsByUser(Map<String, Object> filters, String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getConjunctionSearchCriteria(getSearchCriteria(in_searchCriteria, filter), filters));
    }

    public List<Task> findAllDocumentsByUser(Map<String, Object> filters, String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(in_searchCriteria, ords, orderAsc);
            } else {
                addOrder(in_searchCriteria, orderBy, orderAsc);
            }
        }
        in_searchCriteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, filters), filter), offset, count);
        if (ids.isEmpty()) {
            return new ArrayList<Task>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, orderAsc));
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)  ************************************************************************************
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param in_searchCriteria запрос, куда будет добалено ограничение
     * @param showDrafts        false - в запрос будет добавлено ограничение на проверку статуса документа, так чтобы его статус был НЕ "Проект документа"
     */
    private void addDraftsRestriction(final DetachedCriteria in_searchCriteria, final boolean showDrafts) {
        if (!showDrafts) {
            in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.DRAFT.getId())));
        }
    }

    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param in_searchCriteria запрос, куда будет добалено ограничение
     * @param showDeleted       true - в запрос будет добавлено ограничение на проверку флага, так чтобы документ не был удален
     */
    private void addDeletedRestriction(final DetachedCriteria in_searchCriteria, final boolean showDeleted) {
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }
    }

    /**
     * Добавление ограничений на статус докуменита и флаг удаленя
     *
     * @param in_searchCriteria запрос, в который будут добавлены ограничения
     * @param showDeleted       включать в результат удаленные документы
     * @param showDrafts        включать в результат документы, на прошедшие регистрацию
     */
    private void addDraftsAndDeletedRestrictions(final DetachedCriteria in_searchCriteria, final boolean showDeleted, boolean showDrafts) {
        addDeletedRestriction(in_searchCriteria, showDeleted);
        addDraftsRestriction(in_searchCriteria, showDrafts);
    }

    private void applyAliases(DetachedCriteria criteria) {
        criteria.createAlias("author", "author", CriteriaSpecification.INNER_JOIN)
                .createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN)
                .createAlias("initiator", "initiator", CriteriaSpecification.LEFT_JOIN)
                .createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
    }

    private DetachedCriteria getIDListCriteria(List ids, String[] ords, String orderBy, boolean orderAsc) {
        final DetachedCriteria result = DetachedCriteria.forClass(getPersistentClass()).add(Restrictions.in("id", ids));
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(result, ords, orderAsc);
            } else {
                addOrder(result, orderBy, orderAsc);
            }
        } else {
            addOrder(result, orderBy, orderAsc);
        }
        result.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(result);
        return result;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа со списком пользователей (замещение)  ********************************************************************
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить кол-во документов, к которым есть доступ у  группы пользователей
     *
     * @param filters     карта ограничений
     * @param filter      поисковая строка
     * @param userList    список пользователей, права которых будут проверяться
     * @param showDeleted показывать удаленных
     * @param showDrafts  показывать незарегистрированные документы
     * @return список документов, удовлетворяющих ограничения
     */
    public long countAllDocumentsByUserList(Map<String, Object> filters, String filter, List<User> userList, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUserList(userList);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getConjunctionSearchCriteria(getSearchCriteria(in_searchCriteria, filter), filters));
    }

    /**
     * Формирование ограничения на группу пользователей (доступ к документу)
     *
     * @param userList список пользователей, права которых будут проверяться
     * @return запрос с проверкой прав доступа для списка пользователей (ИЛИ)
     */
    protected DetachedCriteria getAccessControlSearchCriteriaByUserList(List<User> userList) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        Disjunction disjunction = Restrictions.disjunction();
        boolean isAdministrator = false;
        final List<Integer> userIdList = new ArrayList<Integer>(userList.size());
        //Сбор идшников всех пользователей в список и проверка админских прав
        for (User current : userList) {
            if (current.isAdministrator()) {
                isAdministrator = true;
                break;
            } else {
                userIdList.add(current.getId());
            }
        }
        // Ежели админских прав нет, то проверять чтобы хоть один из идшников пользователей был в списках
        if (!isAdministrator) {
            disjunction.add(Restrictions.in("author.id", userIdList));
            disjunction.add(Restrictions.in("executors.id", userIdList));
            disjunction.add(Restrictions.in("initiator.id", userIdList));
            disjunction.add(Restrictions.in("controller.id", userIdList));
            detachedCriteria.add(disjunction);
        }
        return detachedCriteria;
    }

    public List<Task> findAllDocumentsByUserList(Map<String, Object> filters, String filter, List<User> userList, boolean showDeleted, boolean showDrafts, int offset, int pageSize, String orderBy, boolean asc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUserList(userList);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(in_searchCriteria, ords, asc);
            } else {
                addOrder(in_searchCriteria, orderBy, asc);
            }
        }
        in_searchCriteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, filters), filter), offset, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<Task>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, asc));
    }
}