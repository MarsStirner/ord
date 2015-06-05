package ru.hitsl.sql.dao.referenceBook;

import ru.entity.model.referenceBook.ContactInfoType;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 15:42 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class ContactInfoTypeDAOImpl extends DictionaryDAOHibernate<ContactInfoType> {
    @Override
    protected Class<ContactInfoType> getPersistentClass() {
        return ContactInfoType.class;
    }
}
