package ru.hitsl.sql.dao.interfaces.referencebook;

import ru.entity.model.referenceBook.UserAccessLevel;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:32 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface UserAccessLevelDao extends ReferenceBookDao<UserAccessLevel>{
    UserAccessLevel findByLevel(int level);

    List<UserAccessLevel> findLowerThenLevel(int level);
}
