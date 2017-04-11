package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.interfaces.referencebook.PositionDao;

/**
 * Author: Upatov Egor <br>
 * Date: 08.09.2014, 17:45 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverterWithSpringSupport("positionConverter")
public class PositionConverter extends AbstractReferenceBookConverter<Position> {
    @Autowired
    public PositionConverter(@Qualifier("positionDao") final PositionDao positionDao) {
        super(positionDao);
    }
}
