package ru.hitsl.sql.dao.interfaces.mapped.criteria;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.mapped.DeletableEntity;
import ru.hitsl.sql.dao.interfaces.mapped.AbstractDao;

/**
 * Author: Upatov Egor <br>
 * Date: 21.03.2017, 18:16 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface Deletable<T extends DeletableEntity> extends AbstractDao<T> {

    boolean delete(T entity);

    default void applyDeletedRestriction(DetachedCriteria criteria, boolean showDeleted) {
        if (!showDeleted) {
            filterDeleted(criteria);
        }
    }

    default void filterDeleted(DetachedCriteria criteria) {
        criteria.add(Restrictions.eq("deleted", false));
    }
}
