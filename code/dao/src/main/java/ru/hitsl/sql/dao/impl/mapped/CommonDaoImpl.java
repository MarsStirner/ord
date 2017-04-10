package ru.hitsl.sql.dao.impl.mapped;

import org.hibernate.criterion.DetachedCriteria;
import ru.entity.model.mapped.DeletableEntity;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 27.03.2017, 15:28 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public abstract class CommonDaoImpl<T extends DeletableEntity> extends DeletableDaoImpl<T> implements CommonDao<T> {

    @Override
    public void applyFilter(DetachedCriteria criteria, Map<String, Object> filter) {
    }

    @Override
    public void applyFilter(DetachedCriteria criteria, String filter) {
    }

}
