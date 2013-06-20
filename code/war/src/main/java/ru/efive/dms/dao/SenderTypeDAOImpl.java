package ru.efive.dms.dao;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.efive.dms.data.SenderType;

public class SenderTypeDAOImpl extends DictionaryDAOHibernate<SenderType> {

    @Override
    protected Class<SenderType> getPersistentClass() {
        return SenderType.class;
    }

}