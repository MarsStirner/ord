package ru.hitsl.sql.dao.impl.referencebook;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.UserAccessLevelDao;

import java.util.List;

@Repository("userAccessLevelDao")
@Transactional(propagation = Propagation.MANDATORY)
public class UserAccessLevelDaoImpl extends ReferenceBookDaoImpl<UserAccessLevel> implements UserAccessLevelDao {

    @Override
    public UserAccessLevel findByLevel(int level) {
        DetachedCriteria detachedCriteria = getFullCriteria();
        detachedCriteria.add(Restrictions.eq("level", level));
        return getFirstItem(detachedCriteria);
    }

    @Override
    public List<UserAccessLevel> findLowerThenLevel(int level) {
        final DetachedCriteria detachedCriteria = getFullCriteria();
        detachedCriteria.add(Restrictions.le("level", level));
        return getItems(detachedCriteria);
    }

    @Override
    public Class<UserAccessLevel> getEntityClass() {
        return UserAccessLevel.class;
    }
}