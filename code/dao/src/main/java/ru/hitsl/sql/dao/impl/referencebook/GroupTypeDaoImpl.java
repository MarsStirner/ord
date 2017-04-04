package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import ru.entity.model.referenceBook.GroupType;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupTypeDao;

@Repository("groupTypeDao")
public class GroupTypeDaoImpl extends ReferenceBookDaoImpl<GroupType> implements GroupTypeDao{

    @Override
    public Class<GroupType> getEntityClass() {
        return GroupType.class;
    }
}