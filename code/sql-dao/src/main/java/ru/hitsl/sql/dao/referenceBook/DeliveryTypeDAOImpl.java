package ru.hitsl.sql.dao.referenceBook;

import ru.entity.model.referenceBook.DeliveryType;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

public class DeliveryTypeDAOImpl extends DictionaryDAOHibernate<DeliveryType> {

    @Override
    protected Class<DeliveryType> getPersistentClass() {
        return DeliveryType.class;
    }

}