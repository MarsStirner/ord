package ru.hitsl.sql.dao.referenceBook;

import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

/**
 * Author: Upatov Egor <br>
 * Date: 11.02.2015, 4:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class ContragentTypeDAOImpl extends DictionaryDAOHibernate<ContragentType> {
    @Override
    protected Class<ContragentType> getPersistentClass() {
        return ContragentType.class;
    }
}