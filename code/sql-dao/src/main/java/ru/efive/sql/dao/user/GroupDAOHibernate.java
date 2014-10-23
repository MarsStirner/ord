package ru.efive.sql.dao.user;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.user.Group;
import ru.util.ApplicationHelper;

public class GroupDAOHibernate extends GenericDAOHibernate<Group> {

    @Override
    protected Class<Group> getPersistentClass() {
        return Group.class;
    }

    /**
     * Возвращает группу по наименованию.
     *
     * @param name наименование
     */
    @SuppressWarnings("unchecked")
    public List<Group> findAllDocuments(String filter, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Group.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        detachedCriteria.add(Restrictions.isNotNull("alias"));
        List<Group> groups = getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter));
        return groups;
    }

    public long countAllDocuments(String filter, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Group.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        detachedCriteria.add(Restrictions.isNotNull("alias"));
        return getCountOf(getSearchCriteria(detachedCriteria, filter));

    }

    @SuppressWarnings("unchecked")
    public Group findGroupByAlias(String alias) {
        Group group = null;
        if (StringUtils.isNotEmpty(alias)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Group.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

            detachedCriteria.add(Restrictions.eq("alias", alias));
            List<Group> groups = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 0);
            if ((groups != null) && (!groups.isEmpty())) {
                group = groups.get(0);
            }
        }
        return group;
    }


    /*@SuppressWarnings("unchecked")
     public Group findByNameUsingUsersFilter(String name, String filter) {
         if (StringUtils.isNotEmpty(name)) {
             DetachedCriteria detachedCriteria=DetachedCriteria.forClass(Group.class);
             detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

             detachedCriteria.add(Restrictions.eq("name", name));

             if(!filter.isEmpty()){
                 Disjunction disjunction = Restrictions.disjunction();
                 detachedCriteria.createAlias("members", "members", CriteriaSpecification.LEFT_JOIN);
                 disjunction.add(Restrictions.ilike("members.lastName", filter + "%",MatchMode.ANYWHERE ));
                 disjunction.add(Restrictions.ilike("members.middleName", filter + "%",MatchMode.ANYWHERE));
                 disjunction.add(Restrictions.ilike("members.firstName", filter + "%",MatchMode.ANYWHERE));
                 disjunction.add(Restrictions.ilike("members.email", filter + "%",MatchMode.ANYWHERE));
                 detachedCriteria.add(disjunction);
             }

             List<Group> groups = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 0);
             if ((groups != null) && (!groups.isEmpty())) {
                 // don't modify this routine work! (for proxy caching such objects as Personage, Location)
                 Group group=groups.get(0);
                 if (group != null) {}
                 return group;
             }
             else {
                 return null;
             }
         }
         else {
             return null;
         }
     }
      */

    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("alias", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("description", filter, MatchMode.ANYWHERE));

            criteria.add(disjunction);
        }
        return criteria;
    }
}