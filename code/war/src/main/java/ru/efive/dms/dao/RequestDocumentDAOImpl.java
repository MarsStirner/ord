package ru.efive.dms.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.document.DeliveryType;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;

import java.util.*;

import static ru.efive.dms.util.DocumentSearchMapKeys.*;
import static ru.util.ApplicationHelper.getNextDayDate;

public class RequestDocumentDAOImpl extends GenericDAOHibernate<RequestDocument> {

    @Override
    protected Class<RequestDocument> getPersistentClass() {
        return RequestDocument.class;
    }

    public List<RequestDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean
            showDeleted, boolean showDrafts) {
        return findAllDocumentsByUser(in_map, filter, user, showDeleted, showDrafts, -1, -1, null, false);
    }


    public long countAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted,
                                        boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
    }

    public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    public List<RequestDocument> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean
            showDrafts, int offset, int count) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);

        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset,
                count);
    }

    public long countAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(in_searchCriteria, filter));
    }

    public List<RequestDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean
            showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter));
    }

    public List<RequestDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean
            showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
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

    public long countAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean
            showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        applyAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    @SuppressWarnings("unchecked")
    public List<RequestDocument> findRegistratedDocuments() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        applyAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.isNotNull("registrationNumber"));
        Calendar calendar = Calendar.getInstance();
        detachedCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)",
                calendar.get(Calendar.YEAR) + "%", new StringType()));

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    @SuppressWarnings("unchecked")
    public List<RequestDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int
            offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        applyAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        addDeletedRestriction(detachedCriteria, showDeleted);
        int userId = user.getId();
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId()));

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
     * @param user        - пользователь
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDraftDocumentsByAuthor(User user, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        applyAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        int userId = user.getId();
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId()));
        return getCountOf(detachedCriteria);
    }


    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(deliveryDate, '%d.%m.%Y') like lower(?)", filter
                    + "%", new StringType()));
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(receivedDocumentDate, '%d.%m.%Y') like lower(?)" +
                    "", filter + "%", new StringType()));
            disjunction.add(Restrictions.ilike("receivedDocumentNumber", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("senderFirstName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("senderMiddleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("senderLastName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("executor.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executor.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executor.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("deliveryType.value", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));

            String docType = getPersistentClass().getName();
            List<Integer> statusIdList = DocumentType.getStatusIdListByStrKey((docType.contains(".") ? docType
                    .substring(docType.lastIndexOf(".") + 1) : docType), filter);
            if (statusIdList.size() > 0) {
                disjunction.add(Restrictions.in("statusId", statusIdList));
            }

            //TODO: поиск по адресатам

            criteria.add(disjunction);
        }
        return criteria;
    }

    protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
        if (in_map != null && !in_map.isEmpty()) {
            final Conjunction conjunction = Restrictions.conjunction();
            if (in_map.containsKey(REGISTRATION_NUMBER_KEY)) {
                conjunction.add(Restrictions.ilike("registrationNumber", in_map.get(REGISTRATION_NUMBER_KEY).toString
                        (), MatchMode.ANYWHERE));
            }

            if (in_map.containsKey(START_REGISTRATION_DATE_KEY)) {
                conjunction.add(Restrictions.ge("registrationDate", in_map.get(START_REGISTRATION_DATE_KEY)));
            }

            if (in_map.containsKey(END_REGISTRATION_DATE_KEY)) {
                conjunction.add(Restrictions.le("registrationDate", getNextDayDate((Date) in_map.get
                        (END_REGISTRATION_DATE_KEY))));
            }

            if (in_map.containsKey(START_CREATION_DATE_KEY)) {
                conjunction.add(Restrictions.ge("creationDate", in_map.get(START_CREATION_DATE_KEY)));
            }

            if (in_map.containsKey(END_CREATION_DATE_KEY)) {
                conjunction.add(Restrictions.le("creationDate", getNextDayDate((Date) in_map.get
                        (END_CREATION_DATE_KEY))));
            }

            if (in_map.containsKey(START_DELIVERY_DATE_KEY)) {
                conjunction.add(Restrictions.ge("deliveryDate", in_map.get(START_DELIVERY_DATE_KEY)));
            }

            if (in_map.containsKey(END_DELIVERY_DATE_KEY)) {
                conjunction.add(Restrictions.le("deliveryDate", getNextDayDate((Date) in_map.get
                        (END_DELIVERY_DATE_KEY))));
            }

            if (in_map.containsKey(SHORT_DESCRIPTION_KEY) && StringUtils.isNotEmpty((String) in_map.get
                    (SHORT_DESCRIPTION_KEY))) {
                conjunction.add(Restrictions.ilike("shortDescription", in_map.get(SHORT_DESCRIPTION_KEY).toString(),
                        MatchMode.ANYWHERE));
            }

            if (in_map.containsKey(STATUS_KEY) && StringUtils.isNotEmpty((String) in_map.get(STATUS_KEY))) {
                conjunction.add(Restrictions.eq("statusId", Integer.parseInt(in_map.get(STATUS_KEY).toString())));
            }


            if (in_map.containsKey(SENDER_LAST_NAME_KEY)) {
                conjunction.add(Restrictions.ilike("senderLastName", in_map.get(SENDER_LAST_NAME_KEY).toString(),
                        MatchMode.ANYWHERE));
            }

            if (in_map.containsKey(SENDER_FIRST_NAME_KEY)) {
                conjunction.add(Restrictions.ilike("senderFirstName", in_map.get(SENDER_FIRST_NAME_KEY).toString(),
                        MatchMode.ANYWHERE));
            }

            if (in_map.containsKey(SENDER_PATR_NAME_KEY)) {
                conjunction.add(Restrictions.ilike("senderMiddleName", in_map.get(SENDER_PATR_NAME_KEY).toString(),
                        MatchMode.ANYWHERE));
            }

            if (in_map.containsKey(RECIPIENTS_KEY)) {
                final List<User> recipients = (List<User>) in_map.get(RECIPIENTS_KEY);
                if (!recipients.isEmpty()) {
                    List<Integer> recipientsId = new ArrayList<Integer>(recipients.size());
                    for (User user : recipients) {
                        recipientsId.add(user.getId());
                    }
                    conjunction.add(Restrictions.in("recipientUsers.id", recipientsId));
                }
            }

            if (in_map.containsKey(AUTHOR_KEY)) {
                User author = (User) in_map.get(AUTHOR_KEY);
                conjunction.add(Restrictions.eq("author.id", author.getId()));
            }

            if (in_map.containsKey(RESPONSIBLE_KEY)) {
                User responsible = (User) in_map.get(RESPONSIBLE_KEY);
                //TODO rename column
                conjunction.add(Restrictions.eq("executor.id", responsible.getId()));
            }

            if (in_map.containsKey(STATUS_LIST_KEY)) {
                final Collection<Integer> statusIdList = (Collection<Integer>) in_map.get(STATUS_LIST_KEY);
                if(!statusIdList.isEmpty()) {
                    conjunction.add(Restrictions.in("statusId", statusIdList));
                }
            }

            if (in_map.containsKey(DELIVERY_TYPE_KEY)) {
                conjunction.add(Restrictions.eq("deliveryType.id", ((DeliveryType) in_map.get(DELIVERY_TYPE_KEY))
                        .getId()));
            }

            if (in_map.containsKey(START_EXECUTION_DATE_KEY)) {
                conjunction.add(Restrictions.ge("executionDate", in_map.get(START_EXECUTION_DATE_KEY)));
            }

            if (in_map.containsKey(END_EXECUTION_DATE_KEY)) {
                conjunction.add(Restrictions.le("executionDate", getNextDayDate((Date) in_map.get
                        (END_EXECUTION_DATE_KEY))));
            }

            if (in_map.containsKey(FORM_VALUE_KEY)) {
                conjunction.add(Restrictions.ilike("form.value", in_map.get(FORM_VALUE_KEY).toString()));
            }

            if (in_map.containsKey(FORM_CATEGORY_KEY)) {
                conjunction.add(Restrictions.ilike("form.category", in_map.get(FORM_CATEGORY_KEY).toString()));
            }

            criteria.add(conjunction);
        }
        return criteria;
    }

    protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        Disjunction disjunction = Restrictions.disjunction();

        int userId = user.getId();
        if (!user.isAdministrator()) {
            disjunction.add(Restrictions.eq("author.id", userId));
            disjunction.add(Restrictions.eq("executor.id", userId));
            disjunction.add(Restrictions.eq("controller.id", userId));
            disjunction.add(Restrictions.eq("recipients.id", userId));
            disjunction.add(Restrictions.eq("readers.id", userId));
            disjunction.add(Restrictions.eq("editors.id", userId));

            List<Integer> rolesId = new ArrayList<Integer>();
            List<Role> roles = user.getRoleList();
            if (roles.size() != 0) {
                for (Role role : roles) {
                    rolesId.add(role.getId());
                }
                disjunction.add(Restrictions.in("roleReaders.id", rolesId));
                disjunction.add(Restrictions.in("roleEditors.id", rolesId));
            }

            List<Integer> recipientGroupsId = new ArrayList<Integer>();
            if (!user.getGroups().isEmpty()) {
                for (Group group : user.getGroups()) {
                    recipientGroupsId.add(group.getId());
                }
                disjunction.add(Restrictions.in("recipientGroups.id", recipientGroupsId));
            }
            detachedCriteria.add(disjunction);
            //detachedCriteria.setProjection(Projections.groupProperty("id"));

            //DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass());
            //resultCriteria.add(Subqueries.propertyIn("id", detachedCriteria));
        }
        return detachedCriteria;

    }

    public RequestDocument findDocumentById(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        applyAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("id", Integer.valueOf(id)));
        List<RequestDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && in_results.size() > 0) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    public RequestDocument findDocumentByNumeratorId(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        applyAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("parentNumeratorId", Integer.valueOf(id)));
        List<RequestDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && in_results.size() > 0) {
            return in_results.get(0);
        } else {
            return null;
        }
    }


    public List<RequestDocument> findAllDocumentsByUser(Map<String, Object> filters, String filter, User user,
                                                        boolean showDeleted, boolean showDrafts, int offset, int
            pageSize, String orderBy, boolean asc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
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
        List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria
                (in_searchCriteria, filters), filter), offset, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<RequestDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, asc));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)  ************************************************************************************
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param in_searchCriteria запрос, куда будет добалено ограничение
     * @param showDrafts        false - в запрос будет добавлено ограничение на проверку статуса документа, так чтобы
     *                          его статус был НЕ "Проект документа"
     */
    private void addDraftsRestriction(final DetachedCriteria in_searchCriteria, final boolean showDrafts) {
        if (!showDrafts) {
            in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.DOC_PROJECT_1.getId())));
        }
    }

    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param in_searchCriteria запрос, куда будет добалено ограничение
     * @param showDeleted       true - в запрос будет добавлено ограничение на проверку флага, так чтобы документ не
     *                          был удален
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
    private void addDraftsAndDeletedRestrictions(final DetachedCriteria in_searchCriteria, final boolean showDeleted,
                                                 boolean showDrafts) {
        addDeletedRestriction(in_searchCriteria, showDeleted);
        addDraftsRestriction(in_searchCriteria, showDrafts);
    }

    private void applyAliases(DetachedCriteria criteria) {
        criteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("executor", "executor", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("deliveryType", "deliveryType", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("roleEditors", "roleEditors", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("personReaders", "readers", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("personEditors", "editors", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("recipientUsers", "recipients", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
    }

    /**
     * Формирование запроса на поиск документов по списку идентификаторов
     *
     * @param ids      список идентифкаторов документов
     * @param ords     список колонок для сортировки
     * @param orderBy  колонка для сортировки
     * @param orderAsc направление сортировки
     * @return запрос, с ограничениями на идентификаторы документов и сортировки
     */
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
        applyAliases(result);
        result.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
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
    public long countAllDocumentsByUserList(Map<String, Object> filters, String filter, List<User> userList, boolean
            showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUserList(userList);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        applyAliases(in_searchCriteria);
        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, filters), filter));
    }

    /**
     * Формирование ограничения на группу пользователей (доступ к документу)
     *
     * @param userList список пользователей, права которых будут проверяться
     * @return запрос с проверкой прав доступа для списка пользователей (ИЛИ)
     */
    private DetachedCriteria getAccessControlSearchCriteriaByUserList(List<User> userList) {
        final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        final Disjunction disjunction = Restrictions.disjunction();
        boolean isAdministrator = false;
        final List<Integer> userIdList = new ArrayList<Integer>(userList.size());
        final Set<Integer> roleList = new HashSet<Integer>();
        final Set<Integer> groupList = new HashSet<Integer>();
        //Сбор идшников всех пользователей в список и проверка админских прав
        //Также сбор всех ролей
        for (User current : userList) {
            if (current.isAdministrator()) {
                isAdministrator = true;
                break;
            } else {
                userIdList.add(current.getId());
                if (!current.getRoles().isEmpty()) {
                    for (Role currentRole : current.getRoles()) {
                        roleList.add(currentRole.getId());
                    }
                }
                if (!current.getGroups().isEmpty()) {
                    for (Group currentGroup : current.getGroups()) {
                        groupList.add(currentGroup.getId());
                    }
                }
            }
        }
        // Ежели админских прав нет, то проверять чтобы хоть один из идшников пользователей был в списках
        if (!isAdministrator) {
            disjunction.add(Restrictions.in("author.id", userIdList));
            disjunction.add(Restrictions.in("executor.id", userIdList));
            disjunction.add(Restrictions.in("controller.id", userIdList));
            disjunction.add(Restrictions.in("recipients.id", userIdList));
            disjunction.add(Restrictions.in("readers.id", userIdList));
            disjunction.add(Restrictions.in("editors.id", userIdList));
            disjunction.add(Restrictions.in("roleReaders.id", roleList));
            disjunction.add(Restrictions.in("roleEditors.id", roleList));
            if (!groupList.isEmpty()) {
                disjunction.add(Restrictions.in("recipientGroups.id", groupList));
            }
            detachedCriteria.add(disjunction);
        }
        return detachedCriteria;
    }


    /**
     * Получить список документов, к которым есть доступ у группы пользователей
     *
     * @param filters     карта ограничений
     * @param filter      поисковая строка
     * @param userList    список пользователей, права которых будут проверяться
     * @param showDeleted показывать удаленных
     * @param showDrafts  показывать незарегистрированные документы
     * @param offset      изначальное смещение для ранжирования по страницам
     * @param pageSize    размер страницы
     * @param orderBy     поле для сортировки
     * @param asc         направление сортировки
     * @return список документов, удовлетворяющих ограничения
     */
    public List<RequestDocument> findAllDocumentsByUserList(Map<String, Object> filters, String filter, List<User>
            userList, boolean showDeleted, boolean showDrafts, int offset, int pageSize, String orderBy, boolean asc) {
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
        List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria
                (in_searchCriteria, filters), filter), offset, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<RequestDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, asc));
    }

}