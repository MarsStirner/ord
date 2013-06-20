package ru.efive.crm.dao;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.sql.util.ApplicationHelper;
import ru.efive.crm.data.Contragent;

public class ContragentDAOHibernate extends GenericDAOHibernate<Contragent> {
	
	@Override
	protected Class<Contragent> getPersistentClass() {
		return Contragent.class;
	}
	
	public Contragent getByFullName(String fullname)
	  {
	    if (ApplicationHelper.nonEmptyString(fullname)) {
	      DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Contragent.class);
	      detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

	      detachedCriteria.add(Restrictions.eq("fullName", fullname));

	      List<Contragent> contragents = getHibernateTemplate().findByCriteria(detachedCriteria, -1, 1);
	      if ((contragents != null) && (!contragents.isEmpty())) {
	        return (Contragent)contragents.get(0);
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