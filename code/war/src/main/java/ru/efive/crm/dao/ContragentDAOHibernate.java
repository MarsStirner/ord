package ru.efive.crm.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.crm.Contragent;

import java.util.List;

public class ContragentDAOHibernate extends GenericDAOHibernate<Contragent> {

    @Override
    protected Class<Contragent> getPersistentClass() {
        return Contragent.class;
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


    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("fullName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("shortName", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
        return criteria;
    }
}