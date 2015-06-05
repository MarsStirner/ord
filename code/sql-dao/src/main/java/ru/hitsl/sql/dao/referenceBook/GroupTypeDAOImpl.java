package ru.hitsl.sql.dao.referenceBook;

import ru.entity.model.referenceBook.GroupType;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

public class GroupTypeDAOImpl extends DictionaryDAOHibernate<GroupType> {

    @Override
    protected Class<GroupType> getPersistentClass() {
        return GroupType.class;
    }

}