package ru.hitsl.sql.dao;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import ru.entity.model.mapped.DictionaryEntity;
import ru.hitsl.sql.dao.interfaces.DictionaryDAO;

import java.util.List;


/**
 * Интерфейс для управления справочными записями.
 *
 * @param <T> Класс наследующийся от DictionaryEntity
 * @author Alexey Vagizov
 */
@SuppressWarnings("unchecked")
public class DictionaryDAOHibernate<T extends DictionaryEntity> extends GenericDAOHibernate<T> implements DictionaryDAO<T> {


    public DetachedCriteria getCriteria(){
        return DetachedCriteria.forClass(getPersistentClass(), "this").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    private void addUndeletedRestriction(final DetachedCriteria criteria) {
        criteria.add(Restrictions.eq("deleted", false));
    }

    /**
     * Получить запись справочника по уникальному коду (если не найдено - NULL)
     *
     * @param code Уникальный код записи справочника
     * @return запись справочника (в том числе и удаленную)\ NULL
     */
    @Override
    public T getByCode(final String code) {
        final DetachedCriteria criteria = getCriteria();
        criteria.add(Restrictions.eq("code", code));
        final List result = getHibernateTemplate().findByCriteria(criteria);
        if(result != null && !result.isEmpty()){
            return (T) result.iterator().next();
        }  else {
            return null;
        }
    }

    /**
     * Получить запись справочника по ее значению (если не найдено - NULL)
     *
     * @param value Значение записи справочника
     * @return записи справочника (только не удаленные) \ NULL
     */
    @Override
    public List<T> getByValue(final String value) {
        final DetachedCriteria criteria = getCriteria();
        criteria.add(Restrictions.eq("value", value));
        addUndeletedRestriction(criteria);
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Получить все неудаленные записи справочника
     *
     * @return список неудаленных записей справочника
     */
    @Override
    public List<T> getItems() {
        return getItems(null, -1, -1, null, false);
    }

    /**
     * Получить количество неудаленных записей справочника
     *
     * @return количество неудаленных записей справочника
     */
    @Override
    public int countItems() {
        return countItems(null);
    }

    /**
     * Применить к переданному запросу условия простого поиска
     * IMPL = value LIKE '%{filter}%'
     * @param criteria изначальный запрос
     * @param filter   простой поисковый фильтр
     */
    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter) {
        if(StringUtils.isNotEmpty(filter)) {
            criteria.add(Restrictions.ilike("value", filter, MatchMode.ANYWHERE));
        }
    }

    /**
     * Получить заданную страницу с неудаленными записи справочника с учетом фильтра
     *
     * @param filter простой строковый фильтр
     * @param first  начальная запись страницы
     * @param count  количество элементов на странице
     * @param orderBy поле сортировки
     * @param orderAsc направление сортировки
     * @return страница со списком неудаленных записей справочника
     */
    @Override
    public List<T> getItems(final String filter, final int first, final int count, final String orderBy, final boolean orderAsc) {
        final DetachedCriteria criteria = getCriteria();
        applyFilterCriteria(criteria, filter);
        addUndeletedRestriction(criteria);
        if(StringUtils.isNotEmpty(orderBy)){
            criteria.addOrder(orderAsc ? Order.asc(orderBy) : Order.desc(orderBy));
        }
        return getHibernateTemplate().findByCriteria(criteria, first, count);
    }

    /**
     * Получить количество неудаленными записей справочника с учетом фильтра
     *
     * @param filter простой строковый фильтр
     * @return количество неудаленными записей справочника с учетом фильтра
     */
    @Override
    public int countItems(final String filter) {
        final DetachedCriteria criteria = getCriteria();
        applyFilterCriteria(criteria, filter);
        addUndeletedRestriction(criteria);
        return (int) getCountOf(criteria);
    }
}