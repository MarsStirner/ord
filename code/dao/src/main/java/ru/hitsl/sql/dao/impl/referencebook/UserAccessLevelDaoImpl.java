package ru.hitsl.sql.dao.impl.referencebook;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.UserAccessLevelDao;

@Repository("userAccessLevelDao")
public class UserAccessLevelDaoImpl extends ReferenceBookDaoImpl<UserAccessLevel>  implements UserAccessLevelDao{

    @Override
    public UserAccessLevel findByLevel(int level) {
        DetachedCriteria detachedCriteria = getFullCriteria();
        detachedCriteria.add(Restrictions.eq("level", level));
        return getFirstItem(detachedCriteria);
    }

    @Override
    public Class<UserAccessLevel> getEntityClass() {
        return UserAccessLevel.class;
    }
}