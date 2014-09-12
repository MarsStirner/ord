package ru.efive.sql.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.entity.DictionaryEntity;
import ru.efive.sql.entity.enums.RoleType;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.util.ApplicationHelper;


/**
 * Интерфейс для управления справочными записями.
 *
 * @param <T> Класс наследующийся от DictionaryEntity
 * @author Alexey Vagizov
 */
public class DictionaryDAOHibernate<T extends DictionaryEntity> extends GenericDAOHibernate<T> implements DictionaryDAO<T> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findByValue(String value) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (ApplicationHelper.nonEmptyString(value)) {
            detachedCriteria.add(Restrictions.ilike("value", value));
        }

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    public List<T> findByCategory(String category) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (ApplicationHelper.nonEmptyString(category)) {
            detachedCriteria.add(Restrictions.ilike("category", category));
        }

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    public List<T> findByCategoryAndValue(String category, String value) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (ApplicationHelper.nonEmptyString(category)) {
            detachedCriteria.add(Restrictions.ilike("category", category));
            detachedCriteria.add(Restrictions.ilike("value", value));
        }

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }


    public List<T> findByCategoryAndDescription(String category, String description) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (ApplicationHelper.nonEmptyString(category)) {
            detachedCriteria.add(Restrictions.ilike("category", category));
            detachedCriteria.add(Restrictions.ilike("description", description));
        }

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> findDocuments() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    public List<T> findAllDocumentsByUser(Map<String, Object> in_map, User user) {
        DetachedCriteria detachedCriteria = getAccessControlSearchCriteriaByUser(user);

        int userId = user.getId();
        if (userId > 0) {
            if (user.getRoles().size() > 0) {
                return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map));
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countDocuments() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return getCountOf(detachedCriteria);
    }

    protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {
        //DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        if ((in_map != null) && (in_map.size() > 0)) {
            Conjunction conjunction = Restrictions.conjunction();
            String in_key = "";

            in_key = "category";
            if (in_map.get(in_key) != null) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "roleReaders";
            if (in_map.get(in_key) != null) {
                Set<Role> roles = (Set<Role>) in_map.get(in_key);
                if (roles.size() != 0) {
                    List<Integer> rolesId = new ArrayList<Integer>();
                    Iterator itr = roles.iterator();
                    while (itr.hasNext()) {
                        Role role = (Role) itr.next();
                        rolesId.add(role.getId());
                    }
                    conjunction.add(Restrictions.in("roleReaders.id", rolesId));
                }
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
            detachedCriteria.createAlias("roleReaders", "roleReaders", CriteriaSpecification.LEFT_JOIN);
            if (!user.isAdministrator()) {
                List<Integer> rolesId = new ArrayList<Integer>();
                List<Role> roles = user.getRoleList();
                if (roles.size() != 0) {
                    for (Role role : roles) {
                        rolesId.add(role.getId());
                    }
                    disjunction.add(Restrictions.in("roleReaders.id", rolesId));
                }
                detachedCriteria.add(disjunction);
            }
            in_result = detachedCriteria;
        }
        return in_result;
    }

    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (filter != null && !filter.isEmpty()) {
            criteria.add(Restrictions.ilike("value", filter, MatchMode.ANYWHERE));
        }
        return criteria;
    }


}