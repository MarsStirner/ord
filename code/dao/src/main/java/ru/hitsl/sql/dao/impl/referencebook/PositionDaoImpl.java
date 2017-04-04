package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.PositionDao;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 19:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Repository("positionDao")
public class PositionDaoImpl extends ReferenceBookDaoImpl<Position> implements PositionDao{
    @Override
    public Class<Position> getEntityClass() {
        return Position.class;
    }
}
