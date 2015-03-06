package ru.efive.dms.dao;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.joda.time.LocalDate;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.crm.Contragent;
import ru.entity.model.document.*;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;

import java.util.*;

import static ru.efive.dms.util.DocumentSearchMapKeys.*;
import static ru.util.ApplicationHelper.getNextDayDate;

public class IncomingDocumentDAOImpl extends GenericDAOHibernate<IncomingDocument> {

    @Override
    protected Class<IncomingDocument> getPersistentClass() {
        return IncomingDocument.class;
    }

    @SuppressWarnings("unchecked")
    public IncomingDocument findDocumentById(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("id", Integer.valueOf(id)));
        List<IncomingDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && !in_results.isEmpty()) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public IncomingDocument findDocumentByNumeratorId(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("parentNumeratorId", Integer.valueOf(id)));
        List<IncomingDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && !in_results.isEmpty()) {
            return in_results.get(0);
        } else {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public List<IncomingDocument> findRegistratedDocuments() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.isNotNull("registrationNumber"));

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }


    @SuppressWarnings("unchecked")
    public List<IncomingDocument> findRegistratedDocumentsByCriteria(String in_criteria) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.ilike("registrationNumber", "%" + in_criteria + "%"));

        LocalDate currentDate = new LocalDate();
        detachedCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)",
                currentDate.getYear() + "%", new StringType()));

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    public List<IncomingDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user,
                                                         boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(detachedCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria,
                in_map), filter), -1, 0);

    }

    public long countAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted,
                                        boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
    }

    public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        addDeletedRestriction(in_searchCriteria, showDeleted);

        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    public List<IncomingDocument> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean
            showDrafts, int offset, int count) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset,
                count);
    }

    public List<IncomingDocument> findAllDocuments(Map<String, Object> in_map, String filter, boolean showDeleted,
                                                   boolean showDrafts) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        addDraftsAndDeletedRestrictions(detachedCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria,
                in_map), filter), -1, 0);
    }

    public long countAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean
            showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    public List<IncomingDocument> findAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted,
                                                         boolean showDrafts, int offset, int count, String orderBy,
                                                         boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(in_searchCriteria, ords, orderAsc);
            } else {
                addOrder(in_searchCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset,
                count);
    }

    public long countAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(in_searchCriteria, filter));
    }

    public List<IncomingDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean
            showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter));
    }


    public List<IncomingDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean
            showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
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
        List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);
        if (ids.isEmpty()) {
            return new ArrayList<IncomingDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, orderAsc));
    }


    //// Запросы с проверкой на руководителя документа /////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    public List<IncomingDocument> findControlledDocumentsByUser(final String filter, final User user, final boolean
            showDeleted) {
        DetachedCriteria resultCriteria = getControlledDocumentsByUserCriteria(user, showDeleted, filter);
        resultCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return getHibernateTemplate().findByCriteria(resultCriteria);
    }


    @SuppressWarnings("unchecked")
    public List<IncomingDocument> findControlledDocumentsByUser(final String filter, final User user, final boolean
            showDeleted, final int offset, final int count, final String order, final boolean asc) {
        DetachedCriteria in_searchCriteria = getControlledDocumentsByUserCriteria(user, showDeleted, filter);
        addOrder(in_searchCriteria, order, asc);
        in_searchCriteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(in_searchCriteria, offset, count);
        if (ids.isEmpty()) {
            return new ArrayList<IncomingDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, null, order, asc));
    }

    @SuppressWarnings("unchecked")
    public List<IncomingDocument> findControlledDocumentsByUser(final String filter, final User user, final boolean
            showDeleted, final Date beforeDate, final int offset, final int count, final String order, final boolean
            asc) {
        DetachedCriteria in_searchCriteria = getControlledDocumentsByUserCriteria(user, showDeleted, filter,
                beforeDate);
        addOrder(in_searchCriteria, order, asc);
        in_searchCriteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(in_searchCriteria, offset, count);
        if (ids.isEmpty()) {
            return new ArrayList<IncomingDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, null, order, asc));

    }

    public long countControlledDocumentsByUser(final String filter, final User user, final boolean showDeleted) {
        return getCountOf(getControlledDocumentsByUserCriteria(user, showDeleted, filter));
    }

    public long countControlledDocumentsByUser(final String filter, final User user, final boolean showDeleted, final
    Date beforeDate) {
        return getCountOf(getControlledDocumentsByUserCriteria(user, showDeleted, filter, beforeDate));
    }

    /**
     * Генерирование критериев для отбора Вхоядщих документов, у которых заданный пользователь руководитель (если не
     * админ)
     *
     * @param user        пользователь, для которого генерируем критерии
     * @param showDeleted включать ли удаленные документы в результат
     * @param filter      поисковый шаблон
     * @return набор критериев для поиска требуемых документов
     */
    private DetachedCriteria getControlledDocumentsByUserCriteria(final User user, final boolean showDeleted, final
    String filter) {
        final DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass());
        resultCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        setCriteriaAliases(resultCriteria);
        if (!user.isAdministrator()) {
            resultCriteria.add(Restrictions.eq("controller.id", user.getId()));
        }
        resultCriteria.add(Restrictions.isNotNull("executionDate"));
        resultCriteria.add(Restrictions.in("statusId", ImmutableList.of(DocumentStatus.ON_REGISTRATION.getId(),
                DocumentStatus.CHECK_IN_2.getId(), DocumentStatus.ON_EXECUTION_80.getId())));
        addDeletedRestriction(resultCriteria, showDeleted);
        getSearchCriteria(resultCriteria, filter);
        return resultCriteria;
    }

    /**
     * Генерирование критериев для отбора Вхоядщих документов, у которых заданный пользователь руководитель (если не
     * админ)
     *
     * @param user        пользователь, для которого генерируем критерии
     * @param showDeleted включать ли удаленные документы в результат
     * @param filter      поисковый шаблон
     * @param beforeDate  Ограничение по дате, до которой должен быть срок исполнения
     * @return набор критериев для поиска требуемых документов
     */
    private DetachedCriteria getControlledDocumentsByUserCriteria(final User user, final boolean showDeleted, final
    String filter, final Date beforeDate) {
        return getControlledDocumentsByUserCriteria(user, showDeleted, filter).add(Restrictions.lt("executionDate",
                beforeDate));
    }

    /**
     * Генерирование критериев для отбора Вхоядщих документов, у которых один из заданных пользователей -
     * руководитель (если не админ)
     *
     * @param userList    список пользователей, для которых генерируем критерии
     * @param showDeleted включать ли удаленные документы в результат
     * @param filter      поисковый шаблон
     * @param beforeDate  Ограничение по дате, до которой должен быть срок исполнения
     * @return набор критериев для поиска требуемых документов
     */
    private DetachedCriteria getControlledDocumentsByUserListCriteria(List<User> userList, boolean showDeleted,
                                                                      String filter, Date beforeDate) {
        return getControlledDocumentsByUserListCriteria(userList, showDeleted, filter).add(Restrictions.lt
                ("executionDate", beforeDate));
    }

    /**
     * Генерирование критериев для отбора Вхоядщих документов, у которых один из заданных пользователей -
     * руководитель (если не админ)
     *
     * @param userList    список пользователей, для которых генерируем критерии
     * @param showDeleted включать ли удаленные документы в результат
     * @param filter      поисковый шаблон
     * @return набор критериев для поиска требуемых документов
     */
    private DetachedCriteria getControlledDocumentsByUserListCriteria(List<User> userList, boolean showDeleted,
                                                                      String filter) {
        final DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass());
        resultCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        setCriteriaAliases(resultCriteria);
        boolean isAdministrator = false;
        final List<Integer> userIdList = new ArrayList<Integer>(userList.size());
        for (User user : userList) {
            if (user.isAdministrator()) {
                isAdministrator = true;
                break;
            } else {
                userIdList.add(user.getId());
            }
        }
        if (!isAdministrator) {
            resultCriteria.add(Restrictions.in("controller.id", userIdList));
        }
        resultCriteria.add(Restrictions.isNotNull("executionDate"));
        resultCriteria.add(Restrictions.in("statusId", ImmutableList.of(DocumentStatus.ON_REGISTRATION.getId(),
                DocumentStatus.CHECK_IN_2.getId(), DocumentStatus.ON_EXECUTION_80.getId())));
        addDeletedRestriction(resultCriteria, showDeleted);
        getSearchCriteria(resultCriteria, filter);
        return resultCriteria;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @SuppressWarnings("unchecked")
    public List<IncomingDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int
            offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.eq("author.id", user.getId()));
        detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId()));
        addOrderWithoutAliases(detachedCriteria, orderBy, orderAsc);
        return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
    }

    private void addOrderWithoutAliases(final DetachedCriteria criteria, final String orderBy, final boolean asc) {
        final String[] ords = orderBy.split(",");
        for (String currentOrder : ords) {
            criteria.addOrder(asc ? Order.asc(currentOrder) : Order.desc(currentOrder));
        }
    }

    /**
     * Кол-во документов по автору
     *
     * @param user        - идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDraftDocumentsByAuthor(User user, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.eq("author.id", user.getId()));
        detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId()));
        return getCountOf(detachedCriteria);

    }


    private DetachedCriteria setCriteriaAliases(DetachedCriteria criteria) {
        criteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("deliveryType", "deliveryType", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("contragent", "contragent", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("personReaders", "readers", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("personEditors", "editors", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("roleEditors", "roleEditors", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.LEFT_JOIN);
        return criteria;
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

            disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("executors.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executors.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executors.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("deliveryType.value", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("contragent.fullName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("contragent.shortName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("contragent.fullName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("recipientUsers.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("recipientUsers.firstName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("recipientUsers.middleName", filter, MatchMode.ANYWHERE));

            String docType = getPersistentClass().getName();
            List<Integer> statusIdList = DocumentType.
                    getStatusIdListByStrKey((docType.contains(".") ? docType.substring(docType.lastIndexOf(".") + 1)
                            : docType), filter);
            if (statusIdList.size() > 0) {
                disjunction.add(Restrictions.in("statusId", statusIdList));
            }

            criteria.add(disjunction);


        }
        return criteria;

    }

    protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
        if (in_map != null && !in_map.isEmpty()) {
            Conjunction conjunction = Restrictions.conjunction();

            if (in_map.containsKey("parentNumeratorId")) {
                conjunction.add(Restrictions.isNotNull("parentNumeratorId"));
            }

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

            if (in_map.containsKey(START_RECEIVED_DATE_KEY)) {
                conjunction.add(Restrictions.ge("receivedDate", in_map.get(START_RECEIVED_DATE_KEY)));
            }

            if (in_map.containsKey(END_RECEIVED_DATE_KEY)) {
                conjunction.add(Restrictions.le("receivedDate", getNextDayDate((Date) in_map.get(END_RECEIVED_DATE_KEY))));
            }

            if (in_map.containsKey(RECEIVED_DOCUMENT_NUMBER_KEY) && StringUtils.isNotEmpty((String) in_map.get
                    (RECEIVED_DOCUMENT_NUMBER_KEY))) {
                conjunction.add(Restrictions.ilike("receivedDocumentNumber", in_map.get(RECEIVED_DOCUMENT_NUMBER_KEY)
                        .toString(), MatchMode.ANYWHERE));
            }

            if (in_map.containsKey(SHORT_DESCRIPTION_KEY) && StringUtils.isNotEmpty((String) in_map.get
                    (SHORT_DESCRIPTION_KEY))) {
                conjunction.add(Restrictions.ilike("shortDescription", in_map.get(SHORT_DESCRIPTION_KEY).toString(), MatchMode.ANYWHERE));
            }

            if (in_map.containsKey(STATUS_KEY) && StringUtils.isNotEmpty((String) in_map.get(STATUS_KEY))) {
                conjunction.add(Restrictions.eq("statusId", Integer.parseInt(in_map.get(STATUS_KEY).toString())));
            }

            if (in_map.containsKey(CONTROLLER_KEY)) {
                User controller = (User) in_map.get(CONTROLLER_KEY);
                conjunction.add(Restrictions.eq("controller.id", controller.getId()));
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

            if (in_map.containsKey(EXECUTORS_KEY)) {
                final List<User> executors = (List<User>) in_map.get(EXECUTORS_KEY);
                if (!executors.isEmpty()) {
                    List<Integer> executorsId = new ArrayList<Integer>(executors.size());
                    for (User user : executors) {
                        executorsId.add(user.getId());
                    }
                    conjunction.add(Restrictions.in("executors.id", executorsId));
                }
            }

            if (in_map.containsKey(DELIVERY_TYPE_KEY)) {
                conjunction.add(Restrictions.eq("deliveryType.id", ((DeliveryType) in_map.get(DELIVERY_TYPE_KEY)).getId()));
            }

            if (in_map.containsKey(START_EXECUTION_DATE_KEY)) {
                conjunction.add(Restrictions.ge("executionDate", in_map.get(START_EXECUTION_DATE_KEY)));
            }

            if (in_map.containsKey(END_EXECUTION_DATE_KEY)) {
                conjunction.add(Restrictions.le("executionDate", getNextDayDate((Date) in_map.get
                        (END_EXECUTION_DATE_KEY))));
            }

            if (in_map.containsKey(FORM_KEY)) {
                conjunction.add(Restrictions.eq("form.id", ((DocumentForm) in_map.get(FORM_KEY)).getId()));
            }

            if (in_map.containsKey(CONTRAGENT_KEY)) {
                Contragent contragent = (Contragent) in_map.get(CONTRAGENT_KEY);
                conjunction.add(Restrictions.eq("contragent.id", contragent.getId()));
            }

            if (in_map.containsKey(OFFICE_KEEPING_VOLUME_KEY)) {
                DetachedCriteria subCriterion = DetachedCriteria.forClass(PaperCopyDocument.class);
                subCriterion.add(Restrictions.eq("officeKeepingVolume.id", ((OfficeKeepingVolume) in_map.get(OFFICE_KEEPING_VOLUME_KEY))
                        .getId()));
                subCriterion.add(Restrictions.ilike("parentDocumentId", "incoming_", MatchMode.ANYWHERE));
                List<PaperCopyDocument> in_results = getHibernateTemplate().findByCriteria(subCriterion);
                List<Integer> incomingDocumentsId = new ArrayList<Integer>();
                for (PaperCopyDocument paper : in_results) {
                    String parentId = paper.getParentDocumentId();
                    incomingDocumentsId.add(Integer.parseInt(parentId.substring(9)));
                }
                conjunction.add(Restrictions.in("id", incomingDocumentsId));
            }
            criteria.add(conjunction);
        }
        setCriteriaAliases(criteria);
        return criteria;
    }

    protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        Disjunction disjunction = Restrictions.disjunction();

        int userId = user.getId();
        if (!user.isAdministrator()) {
            disjunction.add(Restrictions.eq("author.id", userId));
            disjunction.add(Restrictions.eq("executors.id", userId));
            disjunction.add(Restrictions.eq("controller.id", userId));
            disjunction.add(Restrictions.eq("recipientUsers.id", userId));
            disjunction.add(Restrictions.eq("readers.id", userId));
            disjunction.add(Restrictions.eq("editors.id", userId));
            if (!user.getRoleList().isEmpty()) {
                List<Integer> rolesId = new ArrayList<Integer>();
                for (Role role : user.getRoleList()) {
                    rolesId.add(role.getId());
                }
                disjunction.add(Restrictions.in("roleReaders.id", rolesId));
                disjunction.add(Restrictions.in("roleEditors.id", rolesId));
            }
            if (!user.getGroups().isEmpty()) {
                final List<Integer> recipientGroupsId = new ArrayList<Integer>();
                for (Group group : user.getGroups()) {
                    recipientGroupsId.add(group.getId());
                }
                disjunction.add(Restrictions.in("recipientGroups.id", recipientGroupsId));
            }
            detachedCriteria.add(disjunction);
        }
        int accessLevel = ((user.getCurrentUserAccessLevel() != null) ? user.getCurrentUserAccessLevel().getLevel() :
                1);
        detachedCriteria.add(Restrictions.conjunction().add(Restrictions.le("userAccessLevel.level", accessLevel)));
        return detachedCriteria;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с группами пользователей (для режима замещения)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Поиск документов, доступных хотя-бы одному из пользователй в списке
     *
     * @param filter      поисковая строка
     * @param userList    список пользователей
     * @param showDeleted показывать удаленные документы
     * @param showDrafts  показывать незарегистрированные документы
     * @param offset      смещение для разбивки запроса на страницы
     * @param pageSize    размер страницы
     * @param orderBy     сортировка
     * @param orderAsc    сортировка по возрастанию?
     * @return список документов, удовлетворяющий всем ограничениям
     */
    public List<IncomingDocument> findAllDocumentsByUserList(String filter, List<User> userList, boolean showDeleted,
                                                             boolean showDrafts, int offset, int pageSize, String
            orderBy, boolean orderAsc) {
        if (userList.size() == 1) {
            // Если в списке один пользователь - то вызов запроса по одному пользоваетлю
            return findAllDocumentsByUser(filter, userList.get(0), showDeleted, showDrafts, offset, pageSize,
                    orderBy, orderAsc);
        }
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUserList(userList);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
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
        List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset,
                pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<IncomingDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, orderAsc));
    }


    /**
     * Формирование ограничений на список входящих документов для группы пользователей (обычно используется для
     * замещений)
     *
     * @param userList
     * @return
     */
    private DetachedCriteria getAccessControlSearchCriteriaByUserList(List<User> userList) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        Disjunction disjunction = Restrictions.disjunction();
        boolean isAdministrator = false;
        final List<Integer> userIdList = new ArrayList<Integer>(userList.size());
        final Set<Integer> roleList = new HashSet<Integer>();
        final Set<Integer> groupList = new HashSet<Integer>();
        //Сбор идшников всех пользователей в список и проверка админских прав
        //Также сбор всех групп и ролей
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
            disjunction.add(Restrictions.in("executors.id", userIdList));
            disjunction.add(Restrictions.in("controller.id", userIdList));
            disjunction.add(Restrictions.in("recipientUsers.id", userIdList));
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
     * Подсчет количества документов, доступных группе пользователей
     */
    public long countAllDocumentsByUserList(String filter, List<User> userList, boolean showDeleted, boolean
            showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUserList(userList);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(in_searchCriteria, filter));
    }


    public List<IncomingDocument> findControlledDocumentsByUserList(String filter, List<User> userList, boolean
            showDeleted, Date beforeDate, int offset, int pageSize, String orderBy, boolean asc) {
        DetachedCriteria in_searchCriteria = getControlledDocumentsByUserListCriteria(userList, showDeleted, filter,
                beforeDate);
        addOrder(in_searchCriteria, orderBy, asc);
        in_searchCriteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(in_searchCriteria, offset, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<IncomingDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, null, orderBy, asc));
    }

    public List<IncomingDocument> findControlledDocumentsByUserList(String filter, List<User> userList, boolean
            showDeleted, int offset, int pageSize, String orderBy, boolean asc) {
        DetachedCriteria in_searchCriteria = getControlledDocumentsByUserListCriteria(userList, showDeleted, filter);
        addOrder(in_searchCriteria, orderBy, asc);
        in_searchCriteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(in_searchCriteria, offset, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<IncomingDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, null, orderBy, asc));
    }

    public long countControlledDocumentsByUserList(String filter, List<User> userList, boolean showDeleted, Date
            beforeDate) {
        return getCountOf(getControlledDocumentsByUserListCriteria(userList, showDeleted, filter, beforeDate));
    }

    public long countControlledDocumentsByUserList(String filter, List<User> userList, boolean showDeleted) {
        return getCountOf(getControlledDocumentsByUserListCriteria(userList, showDeleted, filter));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)  ************************************************************************************
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        setCriteriaAliases(result);
        result.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return result;
    }


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

}
