package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import ru.entity.model.referenceBook.DeliveryType;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.DeliveryTypeDao;

@Repository("deliveryTypeDao")
public class DeliveryTypeDaoImpl extends ReferenceBookDaoImpl<DeliveryType> implements DeliveryTypeDao{
    @Override
    public Class<DeliveryType> getEntityClass() {
        return DeliveryType.class;
    }
}