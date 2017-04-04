package ru.hitsl.sql.dao.interfaces.referencebook;

import ru.entity.model.referenceBook.Group;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:44 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface GroupDao extends ReferenceBookDao<Group> {
    Group findGroupByAlias(String alias);
}
