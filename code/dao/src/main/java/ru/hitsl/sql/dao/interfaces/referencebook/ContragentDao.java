package ru.hitsl.sql.dao.interfaces.referencebook;

import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:44 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface ContragentDao extends ReferenceBookDao<Contragent> {
    List<Contragent> getByType(ContragentType type);
}
