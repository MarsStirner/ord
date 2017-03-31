package ru.hitsl.sql.dao.interfaces.mapped;

import org.hibernate.criterion.DetachedCriteria;
import ru.entity.model.mapped.DeletableEntity;
import ru.hitsl.sql.dao.interfaces.mapped.criteria.Accessible;
import ru.hitsl.sql.dao.interfaces.mapped.criteria.Draftable;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 22.03.2017, 18:42 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface DocumentDao<T extends DeletableEntity> extends CommonDao<T>, Accessible, Draftable {

    default List<T> getItems(
            final AuthorizationData authorizationData,
            final String filter,
            final Map<String, Object> filters,
            final String orderBy,
            final boolean isAscending,
            final int offset,
            final int limit,
            final boolean showDeleted,
            final boolean showDrafts) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        applyDraftRestriction(criteria, showDrafts);
        applyFilter(criteria, filter, filters);
        applyAccessCriteria(criteria, authorizationData);
        applyOrder(criteria, orderBy, isAscending);
        return getItems(criteria, offset, limit);
    }

    default int countItems(
            final AuthorizationData authorizationData,
            final String filter,
            final Map<String, Object> filters,
            final boolean showDeleted,
            final boolean showDrafts) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        applyDraftRestriction(criteria, showDrafts);
        applyFilter(criteria, filter, filters);
        applyAccessCriteria(criteria, authorizationData);
        return countItems(criteria);
    }

    int countDocumentListByFilters(
            AuthorizationData authData,
            String filter,
            Map<String, Object> filters,
            boolean showDeleted,
            boolean showDrafts
    );

    List<T> getPersonalDraftDocumentListByFilters(
            AuthorizationData authData,
            String filter,
            String sortField,
            boolean sortOrder,
            int first,
            int pageSize
    );

    int countPersonalDraftDocumentListByFilters(AuthorizationData authData, String filter);
}
