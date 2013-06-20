package ru.efive.dms.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.data.ScanCopyDocument;

public class ScanCopyDocumentDAOImpl extends GenericDAOHibernate<ScanCopyDocument> {

    @Override
    protected Class<ScanCopyDocument> getPersistentClass() {
        return ScanCopyDocument.class;
    }

    public List<ScanCopyDocument> findAllDocuments(String filter, boolean showDeleted, boolean showDrafts, int offset, int count) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
        }

        if (showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.NEW.getId())));
        }

        return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter), offset, count);
    }

    public List<ScanCopyDocument> findAllDocuments(String filter, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
        } else {
            detachedCriteria.add(Restrictions.or(Restrictions.eq("deleted", false), Restrictions.isNull("deleted")));
        }

        if (showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.NEW.getId())));
        }

        return getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter));
    }

    public long countAllDocuments(String filter, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
        }

        if (showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.NEW.getId())));
        }

        return getCountOf(getSearchCriteria(detachedCriteria, filter));
    }

    @SuppressWarnings("unchecked")
    public ScanCopyDocument findDocumentById(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("id", Integer.valueOf(id)));
        List<ScanCopyDocument> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && in_results.size() > 0) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    public long countAllDocuments(boolean showDeleted, boolean showDrafts) {
        DetachedCriteria in_searchCriteria = DetachedCriteria.forClass(getPersistentClass());
        in_searchCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", true));
        }

        return getCountOf(in_searchCriteria);
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
    public List<ScanCopyDocument> findDocumentsByAuthor(int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
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

        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
        }

        if (userId > 0) {
            detachedCriteria.add(Restrictions.eq("author.id", userId));
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
    public List<ScanCopyDocument> findDocumentsByAuthor(String pattern, int userId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
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

        if (showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", true));
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
        //DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        //detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            //detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
            criteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));
            //TODO: поиск по адресатам

            criteria.add(disjunction);
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
            in_result = detachedCriteria;
        }
        return in_result;

    }

}
