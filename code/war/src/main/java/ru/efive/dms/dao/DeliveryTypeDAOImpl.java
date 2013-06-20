package ru.efive.dms.dao;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.efive.dms.data.DeliveryType;

public class DeliveryTypeDAOImpl extends DictionaryDAOHibernate<DeliveryType> {

    @Override
    protected Class<DeliveryType> getPersistentClass() {
        return DeliveryType.class;
    }

}