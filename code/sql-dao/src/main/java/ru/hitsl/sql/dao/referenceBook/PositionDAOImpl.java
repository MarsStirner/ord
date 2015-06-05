package ru.hitsl.sql.dao.referenceBook;

import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 19:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class PositionDAOImpl extends DictionaryDAOHibernate<Position> {
    @Override
    protected Class<Position> getPersistentClass() {
        return Position.class;
    }
}
