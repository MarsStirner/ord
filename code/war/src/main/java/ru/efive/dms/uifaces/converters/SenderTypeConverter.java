package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.entity.model.referenceBook.SenderType;
import ru.hitsl.sql.dao.interfaces.referencebook.SenderTypeDao;

import javax.faces.convert.FacesConverter;

@FacesConverter("SenderTypeConverter")
public class SenderTypeConverter extends AbstractReferenceBookConverter<SenderType> {
    @Autowired
    public SenderTypeConverter(@Qualifier("senderTypeDao") final SenderTypeDao senderTypeDao) {
        super(senderTypeDao);
    }
}