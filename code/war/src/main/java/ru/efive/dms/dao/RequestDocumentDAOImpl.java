package ru.efive.dms.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;

import ru.entity.model.crm.Contragent;
import ru.entity.model.document.DeliveryType;
import ru.entity.model.document.DocumentForm;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.document.RequestDocument;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;

public class RequestDocumentDAOImpl extends GenericDAOHibernate<RequestDocument> {

    @Override
    protected Class<RequestDocument> getPersistentClass() {
        return RequestDocument.class;
    }

    public List<RequestDocument> findAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
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
            return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
        } else {
            return Collections.emptyList();
        }
    }


    public long countAllDocumentsByUser(Map<String, Object> in_map, String filter, User user, boolean showDeleted, boolean showDrafts) {
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
            return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), filter));
        } else {
            return 0;
        }
    }

    public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
    }

    public List<RequestDocument> findAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts, int offset, int count) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        in_searchCriteria = setCriteriaAliases(in_searchCriteria);
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(in_searchCriteria, in_map), offset, count);
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

    public List<RequestDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts) {
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
            return Collections.emptyList();
        }

    }

    public List<RequestDocument> findAllDocumentsByUser(String filter, User user, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
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
            return getHibernateTemplate().findByCriteria(getSearchCriteria(in_searchCriteria, filter), offset, count);

        } else {
            return Collections.emptyList();
        }
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

    @SuppressWarnings("unchecked")
    public List<RequestDocument> findRegistratedDocuments() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.isNotNull("registrationNumber"));
        Calendar calendar = Calendar.getInstance();
        detachedCriteria.add(Restrictions.sqlRestriction("DATE_FORMAT(this_.registrationDate, '%Y') like lower(?)", calendar.get(Calendar.YEAR) + "%", new StringType()));

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    @SuppressWarnings("unchecked")
    public List<RequestDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
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

            String[] ords = orderBy == null ? null : orderBy.split(",");
            if (ords != null) {
                if (ords.length > 1) {
                    addOrder(detachedCriteria, ords, orderAsc);
                } else {
                    addOrder(detachedCriteria, orderBy, orderAsc);
                }
            }
            return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Кол-во документов по автору
     *
     * @param user      - пользователь
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDraftDocumentsByAuthor(User user, boolean showDeleted) {
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
            return getCountOf(detachedCriteria);
        } else {
            return 0;
        }
    }

    private DetachedCriteria setCriteriaAliases(DetachedCriteria criteria) {
        criteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("executor", "executor", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("deliveryType", "deliveryType", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("roleEditors", "roleEditros", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("personReaders", "readers", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("personEditors", "editors", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("recipientUsers", "recipients", CriteriaSpecification.LEFT_JOIN);
        criteria.createAlias("recipientGroups", "recipientGroups", CriteriaSpecification.LEFT_JOIN);
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
            List<Integer> statusIdList = DocumentType.getStatusIdListByStrKey((docType.indexOf(".") >= 0 ? docType.substring(docType.lastIndexOf(".") + 1) : docType), filter);
            if (statusIdList.size() > 0) {
                disjunction.add(Restrictions.in("statusId", statusIdList));
            }

            //TODO: поиск по адресатам

            criteria.add(disjunction);
        }
        return criteria;
    }

    protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
        if ((in_map != null) && (in_map.size() > 0)) {
            Conjunction conjunction = Restrictions.conjunction();
            String in_key = "";

            in_key = "contragent";
            if (in_map.get(in_key) != null) {
                Contragent contragent = (Contragent) in_map.get(in_key);
                conjunction.add(Restrictions.eq(in_key + ".id", contragent.getId()));
            }

            in_key = "registrationNumber";
            if (in_map.get(in_key) != null && !in_map.get(in_key).toString().isEmpty()) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

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

            in_key = "shortDescription";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "senderFirstName";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "senderLastName";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "senderMiddleName";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
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
                conjunction.add(Restrictions.ilike(in_key + ".lastName", author.getLastName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".middleName", author.getMiddleName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".firstName", author.getFirstName(), MatchMode.ANYWHERE));
            }

            in_key = "executor";
            if (in_map.get(in_key) != null) {
                User executor = (User) in_map.get(in_key);
                conjunction.add(Restrictions.ilike(in_key + ".lastName", executor.getLastName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".middleName", executor.getMiddleName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".firstName", executor.getFirstName(), MatchMode.ANYWHERE));
            }

            in_key = "statusId";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.eq(in_key, Integer.parseInt(in_map.get(in_key).toString())));
            }

            in_key = "statusesId";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.in("statusId", (ArrayList<Integer>) in_map.get(in_key)));
            }

            in_key = "deliveryType";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ilike(in_key + ".value", ((DeliveryType) in_map.get(in_key)).getValue(), MatchMode.ANYWHERE));
            }

            in_key = "startExecutionDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ge(in_key.substring(5, 6).toLowerCase() + in_key.substring(6), in_map.get(in_key)));
            }

            in_key = "executionDate";
            if (in_map.get(in_key) != null) {
                if (in_map.get(in_key).toString().equals("%")) {
                    conjunction.add(Restrictions.isNotNull(in_key));
                } else {
                    conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(" + in_key + ", '%d.%m.%Y') like lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
                }
            }

            in_key = "endExecutionDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.le(in_key.substring(3, 4).toLowerCase() + in_key.substring(4), in_map.get(in_key)));
            }

            in_key = "form";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ilike(in_key + ".value", ((DocumentForm) in_map.get(in_key)).getValue(), MatchMode.ANYWHERE));
            }
            in_key = "formValue";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ilike("form.value", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }
            in_key = "formCategory";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ilike("form.category", in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "officeKeepingVolume";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.eq(in_key + ".id", ((OfficeKeepingVolume) in_map.get(in_key)).getId()));
            }

            criteria.add(conjunction);
        }
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
                    disjunction.add(Restrictions.in("roleEditros.id", rolesId));
                }

                List<Integer> recipientGroupsId = new ArrayList<Integer>();
                if(!user.getGroups().isEmpty()) {
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
            in_result = detachedCriteria;//resultCriteria;
        }
        return in_result;

    }

    public RequestDocument findDocumentById(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria = setCriteriaAliases(detachedCriteria);
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
        detachedCriteria = setCriteriaAliases(detachedCriteria);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("parentNumeratorId", Integer.valueOf(id)));
        List<RequestDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && in_results.size() > 0) {
            return in_results.get(0);
        } else {
            return null;
        }
    }
}