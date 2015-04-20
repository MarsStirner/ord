package ru.efive.crm.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.crm.Contragent;
import ru.entity.model.crm.ContragentType;

import java.util.List;

import static org.hibernate.criterion.MatchMode.ANYWHERE;

public class ContragentDAOHibernate extends GenericDAOHibernate<Contragent> {

    /**
     * Получить самый простой критерий для отбора документов, без лишних FETCH
     *
     * @return обычно критерий для документов с DISTINCT
     */
    public DetachedCriteria getSimplestCriteria() {
        return DetachedCriteria.forClass(Contragent.class, "this").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
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
        final DetachedCriteria result = getSimplestCriteria();
        result.createAlias("type", "type", CriteriaSpecification.LEFT_JOIN);
        return result;
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

    public List<Contragent> findItems(
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

    public Contragent getByFullName(final String fullname) {
        if (StringUtils.isNotEmpty(fullname)) {
            DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Contragent.class);
            detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

            detachedCriteria.add(Restrictions.eq("fullName", fullname));

            final List contragents = getHibernateTemplate().findByCriteria(detachedCriteria);
            if ((contragents != null) && (!contragents.isEmpty())) {
                return (Contragent) contragents.get(0);
            }
            return null;
        }
        return null;
    }

    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("fullName", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("shortName", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("type.value", filter, ANYWHERE));
            criteria.add(disjunction);
        }
    }

    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        applyFilterCriteria(criteria, filter);
        return criteria;
    }

    @Override
    protected Class<Contragent> getPersistentClass() {
        return Contragent.class;
    }

    public List<Contragent> getByType(final ContragentType type) {
       final DetachedCriteria detachedCriteria = getListCriteria();
        detachedCriteria.add(Restrictions.eq("type", type));
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }


}