package ru.hitsl.sql.dao.impl.mapped;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.mapped.DocumentEntity;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;
import ru.hitsl.sql.dao.util.DocumentSearchMapKeys;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

import static org.hibernate.sql.JoinType.INNER_JOIN;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2015, 18:57 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class DocumentDaoImpl<T extends DocumentEntity> extends CommonDaoImpl<T> implements DocumentDao<T> {

    /**
     * Применитиь к текущим критериям ограничения сложного фильтра
     * @param criteria текущий критерий, в который будут добавлены условия  (НЕ менее LIST_CRITERIA)
     * @param filters  сложный фильтр (карта)
     */
    @Override
    public void applyFilter(
            final DetachedCriteria criteria,
            final Map<String, Object> filters
    ) {
        if (filters == null || filters.isEmpty()) {
            return;
        }
        final Conjunction conjunction = Restrictions.conjunction();
        for (Map.Entry<String, Object> entry : filters.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (StringUtils.isEmpty(key)) {
                // Пропустить запись с пустым ключом
                log.warn("FilterMapCriteria: SKIP entry cause empty key for \'{}\'", value);
                continue;
            }
            switch (key) {
                case DocumentSearchMapKeys.STATUS_NOT:
                    //Статус
                    conjunction.add(Restrictions.ne("status", value));
                    break;
                case DocumentSearchMapKeys.STATUS:
                    //Статус
                    conjunction.add(Restrictions.eq("status", value));
                    break;
                case DocumentSearchMapKeys.STATUSES:
                    //Список статусов
                    conjunction.add(Restrictions.in("status", value));
                    break;
                case DocumentSearchMapKeys.STATUS_CODE:
                    //Статус документа в строковом представлении
                    conjunction.add(Restrictions.eq("status.code", value));
                    break;
                case DocumentSearchMapKeys.STATUSES_CODE:
                    //Список статусов документа в строковом представлении
                    conjunction.add(Restrictions.in("status.code", value));
                    break;
                case DocumentSearchMapKeys.AUTHOR:
                    //Автор документа
                    conjunction.add(Restrictions.eq("author", value));
                    break;
                case DocumentSearchMapKeys.AUTHORS:
                    //Список авторов документа
                    conjunction.add(Restrictions.in("author", value));
                    break;
                case DocumentSearchMapKeys.CONTROLLER:
                    //Руководитель документа
                    conjunction.add(Restrictions.eq("controller", value));
                    break;
                case DocumentSearchMapKeys.FORM:
                    //Вид документа
                    conjunction.add(Restrictions.eq("form", value));
                    break;
                case DocumentSearchMapKeys.FORM_VALUE:
                    // Вид документа (наименование)
                    conjunction.add(Restrictions.eq("form.value", value));
                    break;
                case DocumentSearchMapKeys.FORM_DOCUMENT_TYPE_CODE:
                    criteria.createAlias("form.documentType", "documentType", INNER_JOIN);
                    conjunction.add(Restrictions.eq("documentType.code", value));
                    break;
                case DocumentSearchMapKeys.REGISTRATION_NUMBER:
                    //регистрационный номер
                    conjunction.add(Restrictions.eq("registrationNumber", value));
                    break;
                case DocumentSearchMapKeys.REGISTRATION_DATE_START:
                    // Дата регистрации от
                    conjunction.add(Restrictions.ge("registrationDate", value));
                    break;
                case DocumentSearchMapKeys.REGISTRATION_DATE_END:
                    //Дата регистрации до
                    conjunction.add(Restrictions.le("registrationDate", ((Temporal) value).plus(Duration.ofDays(1))));
                    break;
                case DocumentSearchMapKeys.CREATION_DATE_START:
                    // Дата создания от
                    conjunction.add(Restrictions.ge("creationDate", value));
                    break;
                case DocumentSearchMapKeys.CREATION_DATE_END:
                    // Дата создания до
                    conjunction.add(Restrictions.le("creationDate", ((Temporal) value).plus(Duration.ofDays(1))));
                    break;
                case DocumentSearchMapKeys.SHORT_DESCRIPTION:
                    conjunction.add(Restrictions.ilike("shortDescription", (String) value, MatchMode.ANYWHERE));
                    break;
                default:
                    addDocumentMapFilter(conjunction, key, value);
                    break;
            }
        }
        criteria.add(conjunction);
    }

    public void addDocumentMapFilter(Conjunction conjunction, String key, Object value) {
        log.warn("FilterMapCriteria: Unknown key \'{}\' (value =\'{}\')", key, value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа с критериями (общая)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить список документов, доступных пользователю, согласно фильтрам (LIST_CRITERIA)
     * (может использоваться сразу два способа фильтрации одновременно)
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
            final boolean showDeleted
    ) {
        log.debug(
                "Search documents[{}-{}] order by[{} {}] with filter='{}', filterMap={}",
                offset, offset + limit, orderBy, isAscending ? "ASC" : "DESC", filter, filters
        );
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        applyFilter(criteria, filter, filters);
        applyAccessCriteria(criteria, authorizationData);
        applyOrder(criteria, orderBy, isAscending);
        return getWithCorrectLimitings(criteria, orderBy, isAscending, offset, limit);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Методы для работы со списками
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = super.getListCriteria();
        result.createAlias("author", "author", INNER_JOIN);
        result.createAlias("form", "form", INNER_JOIN);
        result.createAlias("controller", "controller", LEFT_OUTER_JOIN);
        result.createAlias("status", "status", INNER_JOIN);
        return result;
    }

}
