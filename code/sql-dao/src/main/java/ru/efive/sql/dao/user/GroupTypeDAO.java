package ru.efive.sql.dao.user;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.entity.model.enums.GroupType;

public class GroupTypeDAO extends DictionaryDAOHibernate<GroupType> {

    @Override
    protected Class<GroupType> getPersistentClass() {
        return GroupType.class;
    }

}