package ru.hitsl.sql.dao.impl.referencebook;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.Region;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;

@Repository("regionDao")
@Transactional(propagation = Propagation.MANDATORY)
public class RegionDaoImpl extends ReferenceBookDaoImpl<Region> {

    @Override
    public void applyFilter(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("value", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("category", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("description", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
    }

    @Override
    public Class<Region> getEntityClass() {
        return Region.class;
    }
}