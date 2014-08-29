package ru.efive.sql.dao.user;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.efive.sql.entity.user.Position;
/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 19:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class PositionDAO extends DictionaryDAOHibernate<Position> {
    @Override
    protected Class<Position> getPersistentClass() {
        return Position.class;
    }
}
