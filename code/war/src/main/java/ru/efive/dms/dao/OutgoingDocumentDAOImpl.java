package ru.efive.dms.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.joda.time.LocalDate;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.crm.Contragent;
import ru.entity.model.document.DeliveryType;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.enums.RoleType;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;

import java.util.*;

import static ru.efive.dms.util.DocumentSearchMapKeys.*;
import static ru.util.ApplicationHelper.getNextDayDate;

//import ru.entity.model.document.IncomingDocument;

public class OutgoingDocumentDAOImpl extends GenericDAOHibernate<OutgoingDocument> {

    @Override
    protected Class<OutgoingDocument> getPersistentClass() {
        return OutgoingDocument.class;
    }

    public List<OutgoingDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(detachedCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map), filter));
    }

    public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    public List<OutgoingDocument> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts, int offset, int count) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset, count);
    }

    @SuppressWarnings("unchecked")
    public List<OutgoingDocument> findRegistratedDocuments() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.isNotNull("registrationNumber"));
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    @SuppressWarnings("unchecked")
    public List<OutgoingDocument> findRegistratedDocumentsByCriteria(String in_criteria) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.ilike("registrationNumber", "%" + in_criteria + "%"));
        final LocalDate currentDate = new LocalDate();
        detachedCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)", currentDate.getYear() + "%", new StringType()));
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    public List<OutgoingDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(in_searchCriteria, ords, orderAsc);
            } else {
                addOrder(in_searchCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter), offset, count);
    }

    public long countAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(in_searchCriteria, filter));
    }

    public List<OutgoingDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter));
    }

    public List<OutgoingDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
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
            return new ArrayList<OutgoingDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, orderAsc));
    }

    public long countAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
    }


    /**
     * Поиск документов по автору
     *
     * @param userId      - идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @param offset      смещение
     * @param count       кол-во результатов
     * @param orderBy     поле для сортировки
     * @param orderAsc    направление сортировки
     * @return список документов
     */
    @SuppressWarnings("unchecked")
    public List<OutgoingDocument> findDocumentsByAuthor(int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.eq("author.id", userId));
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
     * Кол-во документов по автору
     *
     * @param userId      - идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDocumentByAuthor(int userId, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        return getCountOf(detachedCriteria);
    }

    /**
     * Поиск документов по автору
     *
     * @param pattern     поисковый запрос
     * @param userId      - идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @param offset      смещение
     * @param count       кол-во результатов
     * @param orderBy     поле для сортировки
     * @param orderAsc    направление сортировки
     * @return список документов
     */
    @SuppressWarnings("unchecked")
    public List<OutgoingDocument> findDocumentsByAuthor(String pattern, int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, pattern), offset, count);

    }

    /**
     * Кол-во документов по автору
     *
     * @param pattern     поисковый запрос
     * @param userId      - идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDocumentByAuthor(String pattern, int userId, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        return getCountOf(getSearchCriteria(detachedCriteria, pattern));
    }

    @SuppressWarnings("unchecked")
    public List<OutgoingDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.eq("author.id", user.getId()));
        detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.DOC_PROJECT_1.getId()));

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
     * @param user        -  пользователь
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDraftDocumentsByAuthor(User user, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        int userId = user.getId();
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.DOC_PROJECT_1.getId()));
        return getCountOf(detachedCriteria);
    }

    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("contragents.fullName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(signatureDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
            disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("executor.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executor.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executor.firstName", filter, MatchMode.ANYWHERE));

            criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);

            disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));


            String docType = getPersistentClass().getName();
            List<Integer> statusIdList = DocumentType.getStatusIdListByStrKey((docType.contains(".") ? docType.substring(docType.lastIndexOf(".") + 1) : docType), filter);
            if (!statusIdList.isEmpty()) {
                disjunction.add(Restrictions.in("statusId", statusIdList));
            }
            //TODO: поиск по адресатам
            criteria.add(disjunction);
        }
        return criteria;
    }

    protected DetachedCriteria getConjunctionSearchCriteria(final DetachedCriteria criteria, final Map<String, Object> in_map) {
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

            if (in_map.containsKey(CONTRAGENT_KEY)) {
                Contragent contragent = (Contragent) in_map.get(CONTRAGENT_KEY);
                conjunction.add(Restrictions.eq("contragents.id", contragent.getId()));
            }

            if (in_map.containsKey(AUTHOR_KEY)) {
                User author = (User) in_map.get(AUTHOR_KEY);
                conjunction.add(Restrictions.eq("author.id", author.getId()));
            }

            if (in_map.containsKey(AUTHORS_KEY)) {
                final List<User> authors = (List<User>) in_map.get(AUTHORS_KEY);
                if (!authors.isEmpty()) {
                    List<Integer> authorsId = new ArrayList<Integer>(authors.size());
                    for (User user : authors) {
                        authorsId.add(user.getId());
                    }
                    conjunction.add(Restrictions.in("author.id", authorsId));
                }
            }

            if (in_map.containsKey(EXECUTORS_KEY)) {
                final List<User> executors = (List<User>) in_map.get(EXECUTORS_KEY);
                if (!executors.isEmpty()) {
                    List<Integer> executorsId = new ArrayList<Integer>(executors.size());
                    for (User user : executors) {
                        executorsId.add(user.getId());
                    }
                    conjunction.add(Restrictions.in("executor.id", executorsId));
                }
            }


            if (in_map.containsKey(DELIVERY_TYPE_KEY)) {
                conjunction.add(Restrictions.eq("deliveryType.id", ((DeliveryType) in_map.get(DELIVERY_TYPE_KEY))
                        .getId()));
            }

            if (in_map.containsKey(START_SIGNATURE_DATE_KEY)) {
                conjunction.add(Restrictions.ge("signatureDate", in_map.get(START_SIGNATURE_DATE_KEY)));
            }

            if (in_map.containsKey(END_SIGNATURE_DATE_KEY)) {
                conjunction.add(Restrictions.le("signatureDate", getNextDayDate((Date) in_map.get
                        (END_SIGNATURE_DATE_KEY))));
            }

            if (in_map.containsKey(FORM_KEY)) {
                conjunction.add(Restrictions.eq("form.id", ((DocumentForm) in_map.get(FORM_KEY)).getId()));
            }

            criteria.add(conjunction);
        }
        return criteria;
    }

    protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user) {
        final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        Disjunction disjunction = Restrictions.disjunction();

        int userId = user.getId();

        boolean isAdminRole = false;
        List<Role> in_roles = user.getRoleList();
        if (in_roles != null) {
            for (Role in_role : in_roles) {
                if (in_role.getRoleType().equals(RoleType.ADMINISTRATOR)) {
                    isAdminRole = true;
                    break;
                }
            }
        }
        applyAliases(detachedCriteria);
        if (!isAdminRole) {
            disjunction.add(Restrictions.eq("author.id", userId));
            disjunction.add(Restrictions.eq("executor.id", userId));
            disjunction.add(Restrictions.eq("controller.id", userId));
            disjunction.add(Restrictions.eq("readers.id", userId));
            disjunction.add(Restrictions.eq("editors.id", userId));
            disjunction.add(Restrictions.eq("agreementUsers.id", userId));

            List<Integer> rolesId = new ArrayList<Integer>();
            List<Role> roles = user.getRoleList();
            if (roles.size() != 0) {
                for (Role role : roles) {
                    rolesId.add(role.getId());
                }
                disjunction.add(Restrictions.in("roleReaders.id", rolesId));
                disjunction.add(Restrictions.in("roleEditors.id", rolesId));
            }
            detachedCriteria.add(disjunction);
        }
        int accessLevel = ((user.getCurrentUserAccessLevel() != null) ? user.getCurrentUserAccessLevel().getLevel() : 1);
        detachedCriteria.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.add(Restrictions.conjunction().add(Restrictions.le("userAccessLevel.level", accessLevel)));
        return detachedCriteria;

    }


    @SuppressWarnings("unchecked")
    public List<OutgoingDocument> findAllDocumentsByReasonDocumentId(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("reasonDocumentId", id));
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }


    @SuppressWarnings("unchecked")
    public OutgoingDocument findDocumentById(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("id", Integer.valueOf(id)));
        List<OutgoingDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!in_results.isEmpty()) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public OutgoingDocument findDocumentByNumeratorId(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("parentNumeratorId", Integer.valueOf(id)));
        List<OutgoingDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!in_results.isEmpty()) {
            return in_results.get(0);
        } else {
            return null;
        }
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
    public long countAllDocumentsByUserList(
            final Map<String, Object> filters,
            final String filter,
            final List<User> userList,
            final boolean showDeleted,
            final boolean showDrafts) {
        final DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUserList(userList);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, filters), filter));
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
    public List<OutgoingDocument> findAllDocumentsByUserList(
            final Map<String, Object> filters,
            final String filter,
            final List<User> userList,
            final boolean showDeleted,
            final boolean showDrafts,
            final int offset,
            final int pageSize,
            final String orderBy,
            final boolean asc) {
        final DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUserList(userList);
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
            return new ArrayList<OutgoingDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, asc));
    }

    /**
     * Формирование ограничения на группу пользователей (доступ к документу)
     *
     * @param userList список пользователей, права которых будут проверяться
     * @return запрос с проверкой прав доступа для списка пользователей (ИЛИ)
     */
    private DetachedCriteria getAccessControlSearchCriteriaByUserList(List<User> userList) {
        final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        applyAliases(detachedCriteria);
        final Disjunction disjunction = Restrictions.disjunction();
        boolean isAdministrator = false;
        final List<Integer> userIdList = new ArrayList<Integer>(userList.size());
        final Set<Integer> roleList = new HashSet<Integer>();
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
            }
        }
        // Ежели админских прав нет, то проверять чтобы хоть один из идшников пользователей был в списках
        if (!isAdministrator) {
            disjunction.add(Restrictions.in("author.id", userIdList));
            disjunction.add(Restrictions.in("executor.id", userIdList));
            disjunction.add(Restrictions.in("controller.id", userIdList));
            disjunction.add(Restrictions.in("readers.id", userIdList));
            disjunction.add(Restrictions.in("editors.id", userIdList));
            disjunction.add(Restrictions.in("agreementUsers.id", userIdList));
            disjunction.add(Restrictions.in("roleReaders.id", roleList));
            disjunction.add(Restrictions.in("roleEditors.id", roleList));
            detachedCriteria.add(disjunction);
        }
        //TODO уровень допуска
        //        int accessLevel = ((user.getCurrentUserAccessLevel() != null) ? user.getCurrentUserAccessLevel().getLevel() : 1);
        //        detachedCriteria.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.LEFT_JOIN);
        //        detachedCriteria.add(Restrictions.conjunction().add(Restrictions.le("userAccessLevel.level", accessLevel)));
        return detachedCriteria;
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
        applyAliases(result);
        result.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return result;
    }


    /**
     * Добавление алиасов и типа соединения
     *
     * @param detachedCriteria запрос, в который будут добавлены алиасы с нужными типами соединений
     */
    private void applyAliases(final DetachedCriteria detachedCriteria) {
        detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("roleEditors", "roleEditors", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("personReaders", "readers", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("executor", "executor", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("personEditors", "editors", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("agreementUsers", "agreementUsers", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("recipientContragents", "contragents", CriteriaSpecification.LEFT_JOIN);
    }

    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param in_searchCriteria запрос, куда будет добалено ограничение
     * @param showDrafts        false - в запрос будет добавлено ограничение на проверку статуса документа, так чтобы его статус был НЕ "Проект документа"
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
}