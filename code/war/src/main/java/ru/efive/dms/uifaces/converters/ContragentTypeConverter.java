package ru.efive.dms.uifaces.converters;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverter;
import ru.entity.model.referenceBook.ContactInfoType;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.interfaces.referencebook.ContactInfoTypeDao;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentTypeDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 11.02.2015, 4:42 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverter("ContragentTypeConverter")
public class ContragentTypeConverter extends AbstractReferenceBookConverter<ContragentType> {

    @Autowired
    public ContragentTypeConverter(@Qualifier("contragentTypeDao") ContragentTypeDao contragentTypeDao) {
        super(contragentTypeDao);
    }
}
