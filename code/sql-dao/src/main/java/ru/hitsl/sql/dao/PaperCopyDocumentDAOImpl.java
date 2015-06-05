package ru.hitsl.sql.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import ru.entity.model.document.PaperCopyDocument;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;

import java.text.SimpleDateFormat;
import java.util.*;

//import ru.efive.sql.dao.user.UserDAOHibernate;

public class PaperCopyDocumentDAOImpl extends GenericDAOHibernate<PaperCopyDocument> {

    @Override
    protected Class<PaperCopyDocument> getPersistentClass() {
        return PaperCopyDocument.class;
    }

    public List<PaperCopyDocument> findAllDocumentsByParentId(String parentId) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (parentId != null && !parentId.isEmpty()) {
            detachedCriteria.add(Restrictions.eq("parentDocumentId", parentId));
        }

        //addOrder(detachedCriteria, "registrationNumber", true);
        List<PaperCopyDocument> result = getHibernateTemplate().findByCriteria(detachedCriteria);

        return result;
    }

    public List<PaperCopyDocument> findAllDocuments(Map<String, Object> in_map, String filter, boolean showDeleted, boolean showDrafts, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.PROJECT.getId())));
        }

        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map), filter), offset, count);
    }

    public List<PaperCopyDocument> findAllDocuments(Map<String, Object> in_map, String filter, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.PROJECT.getId())));
        }

        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map), filter));
    }

    public long countAllDocuments(Map<String, Object> in_map, String filter, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.PROJECT.getId())));
        }

        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map), filter));
    }

    @SuppressWarnings("unchecked")
    public PaperCopyDocument findDocumentById(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("id", Integer.valueOf(id)));
        List<PaperCopyDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && in_results.size() > 0) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    public long countAllDocuments(Map<String, Object> in_map, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }

        return getCountOf(getConjunctionSearchCriteria(in_searchCriteria, in_map));
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
    public List<PaperCopyDocument> findDocumentsByAuthor(int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (userId > 0) {
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
        } else {
            return Collections.emptyList();
        }
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

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (userId > 0) {
            detachedCriteria.add(Restrictions.eq("author.id", userId));
            return getCountOf(detachedCriteria);
        } else {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    public List<PaperCopyDocument> findDraftDocumentsByAuthor(String filter, User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        int userId = user.getId();
        if (userId > 0) {
            detachedCriteria.add(Restrictions.eq("author.id", userId));
            detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.PROJECT.getId()));

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
     * @param userId      - идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDraftDocumentsByAuthor(User user, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        int userId = user.getId();
        if (userId > 0) {
            detachedCriteria.add(Restrictions.eq("author.id", userId));
            detachedCriteria.add(Restrictions.eq("statusId", DocumentStatus.PROJECT.getId()));
            return getCountOf(detachedCriteria);
        } else {
            return 0;
        }
    }

    /**
     * Поиск документов по автору
     *
     * @param pattern     поисковый запрос
     * @param userId      идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @param offset      смещение
     * @param count       кол-во результатов
     * @param orderBy     поле для сортировки
     * @param orderAsc    направление сортировки
     * @return список документов
     */
    @SuppressWarnings("unchecked")
    public List<PaperCopyDocument> findDocumentsByAuthor(String pattern, int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (userId > 0) {
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
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Кол-во документов по автору
     *
     * @param pattern     поисковый запрос
     * @param userId      идентификатор пользователя
     * @param showDeleted true - show deleted, false - hide deleted
     * @return кол-во результатов
     */
    public long countDocumentByAuthor(String pattern, int userId, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (userId > 0) {
            detachedCriteria.add(Restrictions.eq("author.id", userId));
            return getCountOf(getSearchCriteria(detachedCriteria, pattern));
        } else {
            return 0;
        }
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
            //detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));
            //detachedCriteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("controller.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("controller.firstName", filter, MatchMode.ANYWHERE));
            //detachedCriteria.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("executors.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executors.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("executors.firstName", filter, MatchMode.ANYWHERE));
            criteria.createAlias("deliveryType", "deliveryType", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("deliveryType.value", filter, MatchMode.ANYWHERE));
            criteria.createAlias("form", "form", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("form.value", filter, MatchMode.ANYWHERE));
            criteria.createAlias("contragent", "contragent", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("contragent.value", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("contragent.shortName", filter, MatchMode.ANYWHERE));
            //criteria.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
            //List<String> recipientsName=new ArrayList<String>();
            //recipientsName.add(filter);
            disjunction.add(Restrictions.ilike("recipientUsers.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("recipientUsers.firstName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("recipientUsers.middleName", filter, MatchMode.ANYWHERE));
            //TODO: поиск по адресатам

            criteria.add(disjunction);


        }
        return criteria;
        //if(conjunction!=null){
        //return criteria.add(conjunction.add(disjunction));
        //}else{
        //criteria.add(disjunction);
        //return criteria;
        //}

    }

    protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
        //DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        if ((in_map != null) && (in_map.size() > 0)) {
            Conjunction conjunction = Restrictions.conjunction();
            String in_key = "";

            in_key = "registrationNumber";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

            in_key = "creationDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(" + in_key + ", '%d.%m.%Y') like lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
            }

            in_key = "deliveryDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(" + in_key + ", '%d.%m.%Y') like lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
            }

            in_key = "receivedDocumentDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(" + in_key + ", '%d.%m.%Y') like lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
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
                //detachedCriteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
                conjunction.add(Restrictions.ilike(in_key + ".lastName", controller.getLastName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".middleName", controller.getMiddleName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".firstName", controller.getFirstName(), MatchMode.ANYWHERE));
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
                    //criteria.createAlias("recipientUsers", "recipients", CriteriaSpecification.LEFT_JOIN);
                    conjunction.add(Restrictions.in("recipientUsers.id", recipientsId));
                }
            }

            in_key = "author";
            if (in_map.get(in_key) != null) {
                User author = (User) in_map.get(in_key);
                //detachedCriteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
                conjunction.add(Restrictions.ilike(in_key + ".lastName", author.getLastName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".middleName", author.getMiddleName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".firstName", author.getFirstName(), MatchMode.ANYWHERE));
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
                    //detachedCriteria.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
                    conjunction.add(Restrictions.in("executors.id", executorsId));
                }
            }

            in_key = "deliveryType";
            //if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){
            if (in_map.get(in_key) != null) {
                //detachedCriteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
                conjunction.add(Restrictions.eq(in_key + ".value", ((DeliveryType) in_map.get(in_key)).getValue()));
            }

            in_key = "executionDate";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT(" + in_key + ", '%d.%m.%Y') like lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
            }

            in_key = "form";
            //if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){
            if (in_map.get(in_key) != null) {
                //detachedCriteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
                conjunction.add(Restrictions.eq(in_key + ".value", ((DocumentForm) in_map.get(in_key)).getValue()));
            }

            in_key = "contragent";
            if (in_map.get(in_key) != null) {
                Contragent contragent = (Contragent) in_map.get(in_key);
                //detachedCriteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
                conjunction.add(Restrictions.eq(in_key + ".id", contragent.getId()));
            }

            in_key = "officeKeepingVolume";
            //if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){
            if (in_map.get(in_key) != null) {
                //conjunction.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
                conjunction.add(Restrictions.eq(in_key + ".id", (Integer) in_map.get(in_key)));
            }

            //TODO: поиск по адресатам

            criteria.add(conjunction);
        }
        return criteria;
    }

    protected DetachedCriteria getAccessControlSearchCriteriaByUser(User user) {
        DetachedCriteria in_result = null;
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        //detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        //in_result=detachedCriteria;
        //if(true){return in_result;}
        Disjunction disjunction = Restrictions.disjunction();

        int userId = user.getId();
        if (userId > 0) {
            detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("author.id", userId));
            detachedCriteria.createAlias("executors", "executors", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("executors.id", userId));
            detachedCriteria.createAlias("controller", "controller", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("controller.id", userId));
            detachedCriteria.createAlias("recipientUsers", "recipientUsers", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("recipientUsers.id", userId));
            detachedCriteria.createAlias("personReaders", "readers", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("readers.id", userId));
            detachedCriteria.createAlias("personEditors", "editors", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("editors.id", userId));

            List<Integer> rolesId = new ArrayList<Integer>();
            List<Role> roles = user.getRoleList();
            if (roles.size() != 0) {
                Iterator itr = roles.iterator();
                while (itr.hasNext()) {
                    Role role = (Role) itr.next();
                    rolesId.add(role.getId());
                }
                detachedCriteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("roleReaders.id", rolesId));
                detachedCriteria.createAlias("roleEditors", "roleEditros", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("roleEditros.id", rolesId));
            }
            detachedCriteria.add(disjunction);
            //detachedCriteria.setProjection(Projections.groupProperty("id"));

            //DetachedCriteria resultCriteria = DetachedCriteria.forClass(getPersistentClass());
            //resultCriteria.add(Subqueries.propertyIn("id", detachedCriteria));
            in_result = detachedCriteria;
        }
        return in_result;

    }

}
