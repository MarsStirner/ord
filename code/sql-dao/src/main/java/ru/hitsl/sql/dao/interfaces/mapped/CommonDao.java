package ru.hitsl.sql.dao.interfaces.mapped;

import org.hibernate.criterion.DetachedCriteria;
import ru.entity.model.mapped.DeletableEntity;
import ru.hitsl.sql.dao.interfaces.mapped.criteria.Deletable;
import ru.hitsl.sql.dao.interfaces.mapped.criteria.Orderable;
import ru.hitsl.sql.dao.interfaces.mapped.criteria.Searchable;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 22.03.2017, 19:34 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface CommonDao<T extends DeletableEntity> extends Deletable<T>, Searchable, Orderable {

    default List<T> getItems(
            final String filter,
            final Map<String, Object> filters,
            final String orderBy,
            final boolean isAscending,
            final int offset,
            final int limit,
            final boolean showDeleted) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        applyFilter(criteria, filter, filters);
        applyOrder(criteria, orderBy, isAscending);
        return getItems(criteria, offset, limit);
    }

    default int countItems(
            final String filter,
            final Map<String, Object> filters,
            final boolean showDeleted) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        applyFilter(criteria, filter, filters);
        return countItems(criteria);
    }
}
