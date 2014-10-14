package ru.efive.wf.core.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.mapped.Document;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.entity.model.wf.RouteTemplate;

public class EngineDAOImpl extends GenericDAOHibernate<Document> {

    @Override
    public Class<Document> getPersistentClass() {
        return Document.class;
    }

    public <T extends Document> T get(Class<T> clazz, Serializable id) {
        return (T) getHibernateTemplate().get(clazz, id);
    }

    @SuppressWarnings("unchecked")
    public <T extends Document> T save(Class<T> clazz, T document) {
        return (T) save(document);
    }

    /*
     @SuppressWarnings("unchecked")
     public List<HumanTaskTemplate> findTasksByParent(String parentType, int parentId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(HumanTaskTemplate.class);
         detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

         if (!showDeleted) {
             detachedCriteria.add(Restrictions.eq("deleted", false));
         }
         detachedCriteria.add(Restrictions.eq("parentType", parentType));
         detachedCriteria.add(Restrictions.eq("parentId", parentId));

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

     @SuppressWarnings("unchecked")
     public List<HumanTaskTemplate> findTasksByParent(User user, String parentType, int parentId, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(HumanTaskTemplate.class);
         detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

         if (!showDeleted) {
             detachedCriteria.add(Restrictions.eq("deleted", false));
         }
         detachedCriteria.add(Restrictions.eq("parentType", parentType));
         detachedCriteria.add(Restrictions.eq("parentId", parentId));

         Disjunction disjunction = Restrictions.disjunction();
         int userId = user.getId();
         if (userId > 0) {
             detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
             disjunction.add(Restrictions.eq("author.id", userId));
             detachedCriteria.createAlias("executor", "executor", CriteriaSpecification.LEFT_JOIN);
             disjunction.add(Restrictions.eq("executor.id", userId));

             detachedCriteria.add(disjunction);

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
         else {
             return Collections.emptyList();
         }
     }

     @SuppressWarnings("unchecked")
     public List<HumanTaskTemplate> findTasksByUser(User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
         DetachedCriteria detachedCriteria = DetachedCriteria.forClass(HumanTaskTemplate.class);
         detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

         if (!showDeleted) {
             detachedCriteria.add(Restrictions.eq("deleted", false));
         }

         Disjunction disjunction = Restrictions.disjunction();
         int userId = user.getId();
         if (userId > 0) {
             detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
             disjunction.add(Restrictions.eq("author.id", userId));
             detachedCriteria.createAlias("executor", "executor", CriteriaSpecification.LEFT_JOIN);
             disjunction.add(Restrictions.eq("executor.id", userId));

             detachedCriteria.add(disjunction);

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
         else {
             return Collections.emptyList();
         }
     }
     */
    @SuppressWarnings("unchecked")
    public List<RouteTemplate> findRouteTemplates(User user, boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(RouteTemplate.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        Disjunction disjunction = Restrictions.disjunction();
        int userId = user.getId();

        if (userId > 0) {
            detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("author.id", userId));
            detachedCriteria.createAlias("readers", "readers", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("readers.id", userId));
            detachedCriteria.createAlias("editors", "editors", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("editors.id", userId));

            List<Integer> rolesId = new ArrayList<Integer>();
            List<Role> roles = user.getRoleList();
            if (roles != null && roles.size() > 0) {
                for (Role role : roles) {
                    rolesId.add(role.getId());
                }
                detachedCriteria.createAlias("readerRoles", "readerRoles", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("readerRoles.id", rolesId));
                detachedCriteria.createAlias("editorRoles", "editorRoles", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("editorRoles.id", rolesId));
            }

            detachedCriteria.add(disjunction);
            detachedCriteria.setProjection(Projections.groupProperty("id"));

            DetachedCriteria resultCriteria = DetachedCriteria.forClass(RouteTemplate.class);
            resultCriteria.add(Subqueries.propertyIn("id", detachedCriteria));

            String[] ords = orderBy == null ? null : orderBy.split(",");
            if (ords != null) {
                if (ords.length > 1) {
                    addOrder(detachedCriteria, ords, orderAsc);
                } else {
                    addOrder(detachedCriteria, orderBy, orderAsc);
                }
            }

            return getHibernateTemplate().findByCriteria(resultCriteria, offset, count);
        } else {
            return Collections.emptyList();
        }
    }

    public long countRouteTemplates(User user, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(RouteTemplate.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        Disjunction disjunction = Restrictions.disjunction();
        int userId = user.getId();

        if (userId > 0) {
            detachedCriteria.createAlias("author", "author", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("author.id", userId));
            detachedCriteria.createAlias("readers", "readers", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("readers.id", userId));
            detachedCriteria.createAlias("editors", "editors", CriteriaSpecification.LEFT_JOIN);
            disjunction.add(Restrictions.eq("editors.id", userId));

            List<Integer> rolesId = new ArrayList<Integer>();
            List<Role> roles = user.getRoleList();
            if (roles != null && roles.size() > 0) {
                for (Role role : roles) {
                    rolesId.add(role.getId());
                }
                detachedCriteria.createAlias("readerRoles", "readerRoles", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("readerRoles.id", rolesId));
                detachedCriteria.createAlias("editorRoles", "editorRoles", CriteriaSpecification.LEFT_JOIN);
                disjunction.add(Restrictions.in("editorRoles.id", rolesId));
            }

            detachedCriteria.add(disjunction);
            detachedCriteria.setProjection(Projections.groupProperty("id"));

            DetachedCriteria resultCriteria = DetachedCriteria.forClass(RouteTemplate.class);
            resultCriteria.add(Subqueries.propertyIn("id", detachedCriteria));

            return getCountOf(resultCriteria);
        } else {
            return 0;
        }
    }


}