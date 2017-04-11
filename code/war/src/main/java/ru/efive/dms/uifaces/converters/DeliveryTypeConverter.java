package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverter;
import ru.entity.model.referenceBook.DeliveryType;
import ru.hitsl.sql.dao.interfaces.referencebook.DeliveryTypeDao;

@FacesConverter("DeliveryTypeConverter")
public class DeliveryTypeConverter extends AbstractReferenceBookConverter<DeliveryType> {
    @Autowired
    public DeliveryTypeConverter(@Qualifier("deliveryTypeDao") DeliveryTypeDao deliveryTypeDao) {
        super(deliveryTypeDao);
    }
}