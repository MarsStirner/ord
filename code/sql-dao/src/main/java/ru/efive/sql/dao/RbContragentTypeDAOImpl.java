package ru.efive.sql.dao;

import ru.entity.model.crm.ContragentType;

/**
 * Author: Upatov Egor <br>
 * Date: 11.02.2015, 4:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class RbContragentTypeDAOImpl extends DictionaryDAOHibernate<ContragentType> {

    @Override
    protected Class<ContragentType> getPersistentClass() {
        return ContragentType.class;
    }

}
