package ru.hitsl.sql.dao.referenceBook;

import ru.entity.model.referenceBook.SenderType;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

public class SenderTypeDAOImpl extends DictionaryDAOHibernate<SenderType> {
    @Override
    protected Class<SenderType> getPersistentClass() {
        return SenderType.class;
    }
}