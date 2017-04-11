package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentTypeDao;

/**
 * Author: Upatov Egor <br>
 * Date: 11.02.2015, 4:42 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverterWithSpringSupport("ContragentTypeConverter")
public class ContragentTypeConverter extends AbstractReferenceBookConverter<ContragentType> {

    @Autowired
    public ContragentTypeConverter(@Qualifier("contragentTypeDao") ContragentTypeDao contragentTypeDao) {
        super(contragentTypeDao);
    }
}
