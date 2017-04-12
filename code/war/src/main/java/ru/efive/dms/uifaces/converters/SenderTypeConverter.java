package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.SenderType;
import ru.hitsl.sql.dao.interfaces.referencebook.SenderTypeDao;



@FacesConverterWithSpringSupport("SenderTypeConverter")
public class SenderTypeConverter extends AbstractReferenceBookConverter<SenderType> {
    @Autowired
    public SenderTypeConverter(@Qualifier("senderTypeDao") final SenderTypeDao senderTypeDao) {
        super(senderTypeDao);
    }
}