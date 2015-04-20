package ru.efive.sql.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import ru.entity.model.crm.ContragentType;

import java.util.List;

import static org.hibernate.criterion.MatchMode.ANYWHERE;

/**
 * Author: Upatov Egor <br>
 * Date: 11.02.2015, 4:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class RbContragentTypeDAOImpl extends DictionaryDAOHibernate<ContragentType> {

    @Override
    protected Class<ContragentType> getPersistentClass() {
        return ContragentType.class;
    }

    /**
     * Получить самый простой критерий для отбора документов, без лишних FETCH
     *
     * @return обычно критерий для документов с DISTINCT
     */
    public DetachedCriteria getSimplestCriteria() {
        return DetachedCriteria.forClass(ContragentType.class, "this").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    /**
     * Получить критерий для отбора Документов и их показа в расширенных списках
     * Обычно:
     * Автор - INNER
     * Руководитель - LEFT
     * Вид документа - LEFT
     * ++ В зависимости от вида
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    public DetachedCriteria getListCriteria() {
        return getSimplestCriteria();
    }

    /**
     * Получить критерий для отбора Документов с максимальным количеством FETCH
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    public DetachedCriteria getFullCriteria() {
        return getListCriteria();
    }


    public int countItems(final String filter, boolean showDeleted) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteria(criteria, filter);
        if (!showDeleted) {
            criteria.add(Restrictions.eq("deleted", false));
        }
        return (int) getCountOf(criteria);
    }

    public List<ContragentType> findItems(
            final String filter, final boolean showDeleted, final int first, final int pageSize, final String orderBy, final boolean orderAsc
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteria(criteria, filter);
        if (!showDeleted) {
            criteria.add(Restrictions.eq("deleted", false));
        }
        if (StringUtils.isNotEmpty(orderBy)) {
            criteria.addOrder(orderAsc ? Order.asc(orderBy) : Order.desc(orderBy));
        }
        return getHibernateTemplate().findByCriteria(criteria, first, pageSize);
    }

    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("value", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("code", filter, ANYWHERE));
            criteria.add(disjunction);
        }
    }

    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        applyFilterCriteria(criteria, filter);
        return criteria;
    }


}
