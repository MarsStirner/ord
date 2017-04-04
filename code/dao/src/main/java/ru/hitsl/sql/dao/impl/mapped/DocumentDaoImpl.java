package ru.hitsl.sql.dao.impl.mapped;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.mapped.DocumentEntity;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2015, 18:57 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class DocumentDaoImpl<T extends DocumentEntity> extends CommonDaoImpl<T> implements DocumentDao<T> {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Методы для работы со списками
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить список документов, доступных пользователю, согласно фильтрам (LIST_CRITERIA)
     * (может использоваться сразу два способа фильтрации одновременно)
     *
     * @param authorizationData авторизационные данные пользователя
     * @param filter            строковый фильтр, учитывается только если заполнен (смотри getFilteringListCriteria ->
     *                          applyFilterCriteria)
     * @param filters           фильтр-карта с наборами параметров (смотри getFilteringListCriteria -> applyFilterMapCriteria)
     * @param orderBy           поле, по которому будет происходить сортировка результатов
     * @param isAscending       порядок сортировки (TRUE = ASC \ FALSE = DESC)
     * @param offset            начальное смещение страницы (ранжирование по страницам)
     * @param limit             размер страницы
     * @return требуемая часть отсортированного списка документов, удовлетворяющих фильтрам.
     */
    @Override
    public List<T> getItems(
            final AuthorizationData authorizationData,
            final String filter,
            final Map<String, Object> filters,
            final String orderBy,
            final boolean isAscending,
            final int offset,
            final int limit,
            final boolean showDeleted,
            final boolean showDrafts
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        applyDraftRestriction(criteria, showDrafts);
        applyFilter(criteria, filter, filters);
        applyAccessCriteria(criteria, authorizationData);
        applyOrder(criteria, orderBy, isAscending);
        return getWithCorrectLimitings(criteria, orderBy, isAscending, offset, limit);
    }

    /**
     * Получить количество документов, удовлетворяющих фильтрам, доступных пользователю
     *
     * @param authData авторизационные данные пользователя
     * @param filter   простой строковый фильтр
     * @param filters  сложный фильтр
     */
    @Override
    public int countDocumentListByFilters(
            final AuthorizationData authData,
            final String filter,
            final Map<String, Object> filters,
            final boolean showDeleted,
            final boolean showDrafts
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilter(criteria, filter, filters);
        applyAccessCriteria(criteria, authData);
        applyDraftRestriction(criteria, showDrafts);
        applyDeletedRestriction(criteria, showDeleted);
        return countItems(criteria);
    }


    /**
     * Получить персональные документы, находящиеся в проектном статусе
     *
     * @param authData  авторизационные данные
     * @param filter    простой строковый фильтр
     * @param sortField поле, по которому будет происходить сортировка результатов
     * @param sortOrder порядок сортировки (TRUE = ASC \ FALSE = DESC)
     * @param first     начальное смещение страницы (ранжирование по страницам)
     * @param pageSize  размер страницы
     * @return требуемая часть отсортированного списка документов, удовлетворяющих фильтрам.
     */
    @Override
    public List<T> getPersonalDraftDocumentListByFilters(
            final AuthorizationData authData,
            final String filter,
            final String sortField,
            final boolean sortOrder,
            final int first,
            final int pageSize
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilter(criteria, filter);
        criteria.add(Restrictions.in("author.id", authData.getUserIds()));
        criteria.add(Restrictions.in("statusId", getDraftStatuses()));
        applyOrder(criteria, sortField, sortOrder);
        return getWithCorrectLimitings(criteria, sortField, sortOrder, first, pageSize);
    }


    /**
     * Получить количество персональных документы, находящихся в проектном статусе
     *
     * @param authData авторизационные данные пользователя
     * @param filter   простой строковый фильтр
     */
    @Override
    public int countPersonalDraftDocumentListByFilters(final AuthorizationData authData, final String filter) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilter(criteria, filter);
        criteria.add(Restrictions.in("author.id", authData.getUserIds()));
        criteria.add(Restrictions.in("statusId", getDraftStatuses()));
        return countItems(criteria);
    }


}
