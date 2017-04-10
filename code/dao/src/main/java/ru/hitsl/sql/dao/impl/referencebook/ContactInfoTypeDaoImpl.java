package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.ContactInfoType;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.ContactInfoTypeDao;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 15:42 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Repository("contactInfoTypeDao")
@Transactional(propagation = Propagation.MANDATORY)
public class ContactInfoTypeDaoImpl extends ReferenceBookDaoImpl<ContactInfoType> implements ContactInfoTypeDao {
    @Override
    public Class<ContactInfoType> getEntityClass() {
        return ContactInfoType.class;
    }
}
