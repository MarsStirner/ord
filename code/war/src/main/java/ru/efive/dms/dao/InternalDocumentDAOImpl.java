package ru.efive.dms.dao;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.joda.time.LocalDate;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.enums.RoleType;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;

import java.util.*;

import static ru.efive.dms.util.DocumentSearchMapKeys.*;
import static ru.util.ApplicationHelper.getNextDayDate;


public class InternalDocumentDAOImpl extends GenericDAOHibernate<InternalDocument> {

    @Override
    protected Class<InternalDocument> getPersistentClass() {
        return InternalDocument.class;
    }

    public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }


    public List<InternalDocument> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts, int offset, int count) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset, count);
    }

    public List<InternalDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
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
        List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter), offset, count);
        if (ids.isEmpty()) {
            return new ArrayList<InternalDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, orderAsc));
    }

    public List<InternalDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        int userId = user.getId();
        if (userId > 0) {
            return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
        } else {
            return Collections.emptyList();
        }
    }


    public long countAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
    }

    public List<InternalDocument> findAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        int userId = user.getId();
        if (userId > 0) {
            String[] ords = orderBy == null ? null : orderBy.split(",");
            if (ords != null) {
                if (ords.length > 1) {
                    addOrder(in_searchCriteria, ords, orderAsc);
                } else {
                    addOrder(in_searchCriteria, orderBy, orderAsc);
                }
            }
            return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset, count);

        } else {
            return Collections.emptyList();
        }
    }

    public long countAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        int userId = user.getId();
        if (userId > 0) {
            return getCountOf(getSearchCriteria(in_searchCriteria, filter));
        } else {
            return 0;
        }
    }

    public List<InternalDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        int userId = user.getId();
        if (userId > 0) {
            return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter));

        } else {
            return Collections.emptyList();
        }

    }

    public List<InternalDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        int userId = user.getId();
        if (userId > 0) {
            String[] ords = orderBy == null ? null : orderBy.split(",");
            if (ords != null) {
                if (ords.length > 1) {
                    addOrder(in_searchCriteria, ords, orderAsc);
                } else {
                    addOrder(in_searchCriteria, orderBy, orderAsc);
                }
            }
            return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);

        } else {
            return Collections.emptyList();
        }
    }

    public long countAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        int userId = user.getId();
        if (userId > 0) {
            return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
        } else {
            return 0;
        }
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
    public List<InternalDocument> findDocumentsByAuthor(int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
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
     * @param filter      фильтр поиска
     * @param userId      идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @param offset      смещение
     * @param count       кол-во результатов
     * @param orderBy     поле для сортировки
     * @param orderAsc    направление сортировки
     * @return список документов
     */
    @SuppressWarnings("unchecked")
    public List<InternalDocument> findDocumentsByAuthor(String filter, int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
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
        return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
    }

    /**
     * Кол-во документов по автору
     *
     * @param filter      фильтр поиска
     * @param userId      идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDocumentByAuthor(String filter, int userId, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        return getCountOf(getSearchCriteria(detachedCriteria, filter));
    }

    @SuppressWarnings("unchecked")
    public List<InternalDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        addDeletedRestriction(detachedCriteria, showDeleted);
        int userId = user.getId();
        detachedCriteria.add(Restrictions.eq("author.id", userId));
        final ImmutableList<Integer> statuses = ImmutableList.of(
                DocumentStatus.DOC_PROJECT_1.getId(),
                DocumentStatus.CANCEL_150.getId()
        );
        detachedCriteria.add(Restrictions.in("statusId", statuses));

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
     * @param user        - идентификатор пользователя
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
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(creationDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(executionDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
            disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));
            //
            disjunction.add(Restrictions.ilike("responsible.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("responsible.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("responsible.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("signer.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("signer.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("signer.firstName", filter, MatchMode.ANYWHERE));

            disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));

            String docType = getPersistentClass().getName();
            List<Integer> statusIdList = DocumentType.getStatusIdListByStrKey((docType.contains(".") ? docType.substring(docType.lastIndexOf(".") + 1) : docType), filter);
            if (statusIdList.size() > 0) {
                disjunction.add(Restrictions.in("statusId", statusIdList));
            }
            //TODO: поиск по адресатам

            criteria.add(disjunction);
        }
        return criteria;
    }

    @SuppressWarnings("unchecked")
    public List<InternalDocument> findRegistratedDocumentsByCriteria(String in_criteria) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.ilike("registrationNumber", "%" + in_criteria + "%"));
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    @SuppressWarnings("unchecked")
    public List<InternalDocument> findDocumentsByCriteria(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getInitiateCriteriaForPersistentClass();
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        final LocalDate currentDate = new LocalDate();
        in_searchCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)",
                currentDate.getYear() + "%", new StringType()));
        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    @SuppressWarnings("unchecked")
    public long countDocumentsByCriteria(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getInitiateCriteriaForPersistentClass();
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    @SuppressWarnings("unchecked")
    public List<InternalDocument> findRegistratedDocumentsByForm(String in_criteria) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.isNotNull("registrationNumber"));
        detachedCriteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.add(Restrictions.eq("form.value", in_criteria));

        return getHibernateTemplate().findByCriteria(detachedCriteria);
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

            if (in_map.containsKey(SHORT_DESCRIPTION_KEY) && StringUtils.isNotEmpty((String) in_map.get
                    (SHORT_DESCRIPTION_KEY))) {
                conjunction.add(Restrictions.ilike("shortDescription", in_map.get(SHORT_DESCRIPTION_KEY).toString(), MatchMode.ANYWHERE));
            }

            if (in_map.containsKey(STATUS_KEY) && StringUtils.isNotEmpty((String) in_map.get(STATUS_KEY))) {
                conjunction.add(Restrictions.eq("statusId", Integer.parseInt(in_map.get(STATUS_KEY).toString())));
            }

            if (in_map.containsKey(CONTROLLER_KEY)) {
                User controller = (User) in_map.get(CONTROLLER_KEY);
                //TODO rename field to controller
                conjunction.add(Restrictions.eq("signer.id", controller.getId()));
            }

            if (in_map.containsKey(RESPONSIBLE_KEY)) {
                User responsible = (User) in_map.get(RESPONSIBLE_KEY);
                conjunction.add(Restrictions.eq("responsible.id", responsible.getId()));
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

            if (in_map.containsKey(START_SIGNATURE_DATE_KEY)) {
                conjunction.add(Restrictions.ge("signatureDate", in_map.get(START_SIGNATURE_DATE_KEY)));
            }

            if (in_map.containsKey(END_SIGNATURE_DATE_KEY)) {
                conjunction.add(Restrictions.le("signatureDate", getNextDayDate((Date) in_map.get
                        (END_SIGNATURE_DATE_KEY))));
            }
            //TODO fix
            if (in_map.get("closePeriodRegistrationFlag") != null && in_map.get("closePeriodRegistrationFlag").toString().length() > 0) {
                conjunction.add(Restrictions.eq("closePeriodRegistrationFlag", Boolean.valueOf(in_map.get("closePeriodRegistrationFlag").toString())));
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
        DetachedCriteria detachedCriteria = getInitiateCriteriaForPersistentClass();
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
        if (!isAdminRole) {
            disjunction.add(Restrictions.eq("author.id", userId));
            disjunction.add(Restrictions.eq("signer.id", userId));
            disjunction.add(Restrictions.eq("responsible.id", userId));
            disjunction.add(Restrictions.eq("recipientUsers.id", userId));
            disjunction.add(Restrictions.eq("readers.id", userId));
            disjunction.add(Restrictions.eq("editors.id", userId));
            List<Role> roles = user.getRoleList();
            if (!roles.isEmpty()) {
                final List<Integer> rolesId = new ArrayList<Integer>(roles.size());
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
        }
        int accessLevel = ((user.getCurrentUserAccessLevel() != null) ? user.getCurrentUserAccessLevel().getLevel() : 1);
        detachedCriteria.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.add(Restrictions.conjunction().add(Restrictions.le("userAccessLevel.level", accessLevel)));
        return detachedCriteria;
    }

    @SuppressWarnings("unchecked")
    public InternalDocument findDocumentById(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("id", Integer.valueOf(id)));
        List<InternalDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (!in_results.isEmpty()) {
            return in_results.get(0);
        } else {
            return null;
        }
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

    protected DetachedCriteria getInitiateCriteriaForPersistentClass() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        detachedCriteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("signer", "signer", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("responsible", "responsible", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("personEditors", "editors", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("personReaders", "readers", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
        detachedCriteria.createAlias("roleEditors", "roleEditors", CriteriaSpecification.LEFT_JOIN);
        return detachedCriteria;
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
        final DetachedCriteria result = getInitiateCriteriaForPersistentClass().add(Restrictions.in("id", ids));
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
        addDraftsAndDeletedRestrictions(in_searchCriteria, showDeleted, showDrafts);
        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, filters), filter));
    }

    /**
     * Формирование ограничения на группу пользователей (доступ к документу)
     *
     * @param userList список пользователей, права которых будут проверяться
     * @return запрос с проверкой прав доступа для списка пользователей (ИЛИ)
     */
    private DetachedCriteria getAccessControlSearchCriteriaByUserList(List<User> userList) {
        final DetachedCriteria detachedCriteria = getInitiateCriteriaForPersistentClass();
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
            disjunction.add(Restrictions.in("signer.id", userIdList));
            disjunction.add(Restrictions.in("responsible.id", userIdList));
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
        //TODO уровень допуска
        //        int accessLevel = ((user.getCurrentUserAccessLevel() != null) ? user.getCurrentUserAccessLevel().getLevel() : 1);
        //        detachedCriteria.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.LEFT_JOIN);
        //        detachedCriteria.add(Restrictions.conjunction().add(Restrictions.le("userAccessLevel.level", accessLevel)));
        return detachedCriteria;
    }

    /**
     * Получить список документов, к которым есть доступ у группы пользователей
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
    public List<InternalDocument> findAllDocumentsByUserList(Map<String, Object> filters, String filter, List<User> userList, boolean showDeleted, boolean showDrafts, int offset, int pageSize, String orderBy, boolean asc) {
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
            return new ArrayList<InternalDocument>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        return getHibernateTemplate().findByCriteria(getIDListCriteria(ids, ords, orderBy, asc));
    }
}