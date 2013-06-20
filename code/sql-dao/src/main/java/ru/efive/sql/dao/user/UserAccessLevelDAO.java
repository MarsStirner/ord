package ru.efive.sql.dao.user;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.efive.sql.entity.user.UserAccessLevel;

public class UserAccessLevelDAO extends DictionaryDAOHibernate<UserAccessLevel> {

    @Override
    protected Class<UserAccessLevel> getPersistentClass() {
        return UserAccessLevel.class;
    }

    @SuppressWarnings("unchecked")
    public UserAccessLevel findByLevel(int level) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("level", level));
        List<UserAccessLevel> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && in_results.size() > 0) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

}