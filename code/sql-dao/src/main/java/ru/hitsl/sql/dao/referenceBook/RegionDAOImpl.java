package ru.hitsl.sql.dao.referenceBook;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.referenceBook.Region;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

public class RegionDAOImpl extends DictionaryDAOHibernate<Region> {

    @Override
    protected Class<Region> getPersistentClass() {
        return Region.class;
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