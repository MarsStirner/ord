package ru.hitsl.sql.dao.referenceBook;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

import java.util.List;

public class UserAccessLevelDAOImpl extends DictionaryDAOHibernate<UserAccessLevel> {

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