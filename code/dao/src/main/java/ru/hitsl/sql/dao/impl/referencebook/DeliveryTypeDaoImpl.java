package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.DeliveryType;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.DeliveryTypeDao;

@Repository("deliveryTypeDao")
@Transactional(propagation = Propagation.MANDATORY)
public class DeliveryTypeDaoImpl extends ReferenceBookDaoImpl<DeliveryType> implements DeliveryTypeDao{
    @Override
    public Class<DeliveryType> getEntityClass() {
        return DeliveryType.class;
    }
}