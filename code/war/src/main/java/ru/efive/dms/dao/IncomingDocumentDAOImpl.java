package ru.efive.dms.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;

import ru.efive.crm.data.Contragent;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.DocumentType;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.DocumentForm;
import ru.efive.dms.data.IncomingDocument;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.data.PaperCopyDocument;

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
        if (in_results != null && in_results.size() > 0) {
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
        if (in_results != null && in_results.size() > 0) {
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

        Calendar calendar = Calendar.getInstance();
        detachedCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)", calendar.get(Calendar.YEAR) + "%", new StringType()));

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    public List<IncomingDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = getAccessControlSearchCriteriaByUser(user);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId())));
        }

        int userId = user.getId();
        if (userId > 0) {
            System.out.println("subSrart");
            return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map), filter), -1, 0);
        } else {
            return new ArrayList<IncomingDocument>(0);
        }
    }

    public long countAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);

        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId())));
        }

        int userId = user.getId();
        if (userId > 0) {
            return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
        } else {
            return 0;
        }
    }

    public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    public List<IncomingDocument> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts, int offset, int count) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset, count);
    }

    public List<IncomingDocument> findAllDocuments(Map<String, Object> in_map, String filter, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId())));
        }
        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map), filter), -1, 0);
    }

    public long countAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId())));
        }

        int userId = user.getId();
        if (userId > 0) {
            return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
        } else {
            return 0;
        }
    }

    public List<IncomingDocument> findAllDocumentsByUser(Map<String, Object> in_map, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId())));
        }

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
            return new ArrayList<IncomingDocument>(0);
        }
    }

    public long countAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }
        if (!showDrafts) {
            in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId())));
        }
        int userId = user.getId();
        if (userId > 0) {
            return getCountOf(getSearchCriteria(in_searchCriteria, filter));
        } else {
            return 0;
        }
    }

    public List<IncomingDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }
        if (!showDrafts) {
            in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId())));
        }
        int userId = user.getId();
        if (userId > 0) {
            return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter));
        } else {
            return new ArrayList<IncomingDocument>(0);
        }
    }


    public List<IncomingDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            in_searchCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId())));
        }

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

            in_searchCriteria.setProjection(Projections.distinct(Projections.id()));
            //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
            List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);
            if (ids.isEmpty()) {
                return new ArrayList<IncomingDocument>(0);
            }
            //Ищем только по этим ключам с упорядочиванием
            DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass()).add(Restrictions.in("id", ids));
            if (ords != null) {
                if (ords.length > 1) {
                    addOrder(resultCriteria, ords, orderAsc);
                } else {
                    addOrder(resultCriteria, orderBy, orderAsc);
                }
            }
            setCriteriaAliases(resultCriteria);
            resultCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
            return getHibernateTemplate().findByCriteria(resultCriteria);
        } else {
            return new ArrayList<IncomingDocument>(0);
        }
    }

    @SuppressWarnings("unchecked")
    public List<IncomingDocument> findControlledDocumentsByUser(
            final String filter,
            final User user,
            final boolean showDeleted,
            final int offset,
            final int count,
            final String order,
            final boolean asc) {
        if (user != null && user.getId() > 0) {
            DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
            in_searchCriteria = setCriteriaAliases(in_searchCriteria);
            in_searchCriteria.add(Restrictions.isNotNull("executionDate"));
            in_searchCriteria.add(Restrictions.in("statusId",
                            ImmutableList.of(
                                    DocumentStatus.ON_REGISTRATION.getId(),
                                    DocumentStatus.CHECK_IN_2.getId(),
                                    DocumentStatus.ON_EXECUTION_80.getId()
                            )
                    )
            );
            if(!showDeleted){
                in_searchCriteria.add(Restrictions.eq("deleted", false));
            }
            addOrder(in_searchCriteria, order, asc);
            in_searchCriteria.setProjection(Projections.distinct(Projections.id()));
            //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
            List ids = getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);
            if (ids.isEmpty()) {
                return new ArrayList<IncomingDocument>(0);
            }
            //Ищем только по этим ключам с упорядочиванием
            DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass()).add(Restrictions.in("id", ids));
            addOrder(resultCriteria, order, asc);
            setCriteriaAliases(resultCriteria);
            resultCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
            return getHibernateTemplate().findByCriteria(resultCriteria);
        } else {
            return new ArrayList<IncomingDocument>(0);
        }
    }

    @SuppressWarnings("unchecked")
    public long countControlledDocumentsByUser(final String filter, final User user, final boolean showDeleted) {
        if (user != null && user.getId() > 0) {
            DetachedCriteria in_searchCriteria = getAccessControlSearchCriteriaByUser(user);
            in_searchCriteria = setCriteriaAliases(in_searchCriteria);
            in_searchCriteria.add(Restrictions.isNotNull("executionDate"));
            in_searchCriteria.add(Restrictions.in("statusId",
                            ImmutableList.of(
                                    DocumentStatus.ON_REGISTRATION.getId(),
                                    DocumentStatus.CHECK_IN_2.getId(),
                                    DocumentStatus.ON_EXECUTION_80.getId()
                            )
                    )
            );
            if(!showDeleted){
                in_searchCriteria.add(Restrictions.eq("deleted", false));
            }
            return getCountOf(getSearchCriteria(in_searchCriteria, filter));
        } else {
            return 0;
        }
    }


    @SuppressWarnings("unchecked")
    public List<IncomingDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        int userId = user.getId();
        if (userId > 0) {
            detachedCriteria.add(Restrictions.eq("author.id", userId));
            detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId()));

            addOrderWithoutAliases(detachedCriteria, orderBy, orderAsc);

            return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
        } else {
            return new ArrayList<IncomingDocument>(0);
        }
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
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        int userId = user.getId();
        if (userId > 0) {
            detachedCriteria.add(Restrictions.eq("author.id", userId));
            detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.ON_REGISTRATION.getId()));
            return getCountOf(detachedCriteria);
        } else {
            return 0;
        }
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
        criteria.createAlias("roleEditors", "roleEditros", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("userAccessLevel", "userAccessLevel", CriteriaSpecification.LEFT_JOIN);
        return criteria;
    }


    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("registrationNumber", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(deliveryDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
            disjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(receivedDocumentDate, '%d.%m.%Y') like lower(?)", filter + "%", new StringType()));
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
                    getStatusIdListByStrKey((docType.contains(".") ? docType.substring(docType.lastIndexOf(".") + 1) : docType), filter);
            if (statusIdList.size() > 0) {
                disjunction.add(Restrictions.in("statusId", statusIdList));
            }

            criteria.add(disjunction);


        }
        return criteria;

    }

    protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
        if ((in_map != null) && (in_map.size() > 0)) {
            Conjunction conjunction = Restrictions.conjunction();
            String in_key = "";

            in_key = "parentNumeratorId";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.isNotNull(in_key));
            }

            in_key = "registrationNumber";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "startRegistrationDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase() + in_key.substring(6), in_map.get(in_key)));
            }

            in_key = "endRegistrationDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase() + in_key.substring(4), new Date(((Date) in_map.get(in_key)).getTime() + 86400000)));
            }

            in_key = "startCreationDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase() + in_key.substring(6), in_map.get(in_key)));
            }

            in_key = "endCreationDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase() + in_key.substring(4), new Date(((Date) in_map.get(in_key)).getTime() + 86400000)));
            }

            in_key = "startDeliveryDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase() + in_key.substring(6), in_map.get(in_key)));
            }

            in_key = "endDeliveryDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase() + in_key.substring(4), new Date(((Date) in_map.get(in_key)).getTime() + 86400000)));
            }

            in_key = "startReceivedDocumentDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase() + in_key.substring(6), in_map.get(in_key)));
            }

            in_key = "endReceivedDocumentDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase() + in_key.substring(4), new Date(((Date) in_map.get(in_key)).getTime() + 86400000)));
            }

            in_key = "receivedDocumentNumber";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "shortDescription";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "statusId";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.eq(in_key, Integer.parseInt(in_map.get(in_key).toString())));
            }

            in_key = "controller";
            if (in_map.get(in_key) != null) {
                User controller = (User) in_map.get(in_key);
                conjunction.add(Restrictions.eq(in_key + ".id", controller.getId()));
            }

            in_key = "recipientUsers";
            if (in_map.get(in_key) != null) {
                List<User> recipients = (List<User>) in_map.get(in_key);
                if (recipients.size() != 0) {
                    List<Integer> recipientsId = new ArrayList<Integer>();
                    Iterator itr = recipients.iterator();
                    while (itr.hasNext()) {
                        User user = (User) itr.next();
                        recipientsId.add(user.getId());
                    }
                    conjunction.add(Restrictions.in("recipientUsers.id", recipientsId));
                }
            }

            in_key = "author";
            if (in_map.get(in_key) != null) {
                User author = (User) in_map.get(in_key);
                conjunction.add(Restrictions.eq(in_key + ".id", author.getId()));
            }

            in_key = "executors";
            if (in_map.get(in_key) != null) {
                List<User> executors = (List<User>) in_map.get(in_key);
                if (executors.size() != 0) {
                    List<Integer> executorsId = new ArrayList<Integer>();
                    Iterator itr = executors.iterator();
                    while (itr.hasNext()) {
                        User user = (User) itr.next();
                        executorsId.add(user.getId());
                    }
                    conjunction.add(Restrictions.in("executors.id", executorsId));
                }
            }

            in_key = "deliveryType";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.eq(in_key + ".id", ((DeliveryType) in_map.get(in_key)).getId()));
            }

            in_key = "startExecutionDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase() + in_key.substring(6), in_map.get(in_key)));
            }

            in_key = "endExecutionDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase() + in_key.substring(4), in_map.get(in_key)));
            }

            in_key = "form";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.eq(in_key + ".id", ((DocumentForm) in_map.get(in_key)).getId()));
            }

            in_key = "templateFlag";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.eq(in_key, Boolean.parseBoolean(in_map.get(in_key).toString())));
            }

            in_key = "contragent";
            if (in_map.get(in_key) != null) {
                Contragent contragent = (Contragent) in_map.get(in_key);
                conjunction.add(Restrictions.eq(in_key + ".id", contragent.getId()));
            }

            in_key = "officeKeepingVolume";
            if (in_map.get(in_key) != null) {
                DetachedCriteria subCriterion = DetachedCriteria.forClass(PaperCopyDocument.class);
                subCriterion.add(Restrictions.eq("officeKeepingVolume.id", ((OfficeKeepingVolume) in_map.get(in_key)).getId()));
                subCriterion.add(Restrictions.ilike("parentDocumentId", "incoming_", MatchMode.ANYWHERE));
                List<PaperCopyDocument> in_results = getHibernateTemplate().findByCriteria(subCriterion);
                List<Integer> incomingDocumentsId = new ArrayList<Integer>();
                for (PaperCopyDocument paper : in_results) {
                    String parentId = paper.getParentDocumentId();
                    incomingDocumentsId.add(Integer.parseInt(parentId.substring(9)));
                }
                conjunction.add(Restrictions.in("id", incomingDocumentsId));
            }

            //TODO: поиск по адресатам

            criteria.add(conjunction);
        }
        setCriteriaAliases(criteria);
        return criteria;
    }

    protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user) {
        DetachedCriteria in_result = null;
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        Disjunction disjunction = Restrictions.disjunction();

        int userId = user.getId();
        if (userId > 0) {
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
                    disjunction.add(Restrictions.in("roleEditros.id", rolesId));
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
            int accessLevel = ((user.getCurrentUserAccessLevel() != null) ? user.getCurrentUserAccessLevel().getLevel() : 1);
            detachedCriteria.add(Restrictions.conjunction().add(Restrictions.le("userAccessLevel.level", accessLevel)));
            in_result = detachedCriteria;
        }
        return in_result;
    }

}
