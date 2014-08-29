package ru.efive.sql.dao.user;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.efive.sql.entity.user.RbContactInfoType;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 15:42 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class RbContactTypeDAO extends DictionaryDAOHibernate<RbContactInfoType> {
    @Override
    protected Class<RbContactInfoType> getPersistentClass() {
        return RbContactInfoType.class;
    }
}
