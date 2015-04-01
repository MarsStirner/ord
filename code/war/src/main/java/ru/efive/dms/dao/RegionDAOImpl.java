package ru.efive.dms.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.entity.model.document.Region;

import java.util.List;

public class RegionDAOImpl extends DictionaryDAOHibernate<Region> {

    @Override
    protected Class<Region> getPersistentClass() {
        return Region.class;
    }

    @SuppressWarnings("unchecked")
    public List<Region> findDocuments(String filter, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        List<Region> in_results = getHibernateTemplate().findByCriteria(getSearchCriteria(detachedCriteria, filter));
        return in_results;
    }


    public long countDocuments(String filter, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        return getCountOf(getSearchCriteria(detachedCriteria, filter));

    }

    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("value", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("category", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("description", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
        return criteria;
    }
}