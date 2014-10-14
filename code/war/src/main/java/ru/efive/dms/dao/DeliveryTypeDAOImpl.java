package ru.efive.dms.dao;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.entity.model.document.DeliveryType;

public class DeliveryTypeDAOImpl extends DictionaryDAOHibernate<DeliveryType> {

    @Override
    protected Class<DeliveryType> getPersistentClass() {
        return DeliveryType.class;
    }

}