package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.RbContactPointSystem;
import ru.hitsl.sql.dao.interfaces.referencebook.ContactInfoTypeDao;

/**
 * Author: Upatov Egor <br>
 * Date: 09.09.2014, 16:43 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverterWithSpringSupport(value = "ContactTypeConverter", transactionManager = "ordTransactionmanager")
public class ContactTypeConverter extends AbstractReferenceBookConverter<RbContactPointSystem> {

    @Autowired
    public ContactTypeConverter(@Qualifier("contactInfoTypeDao") ContactInfoTypeDao contactInfoTypeDao) {
        super(contactInfoTypeDao);
    }
}
