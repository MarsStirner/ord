package ru.efive.dms.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.dms.data.OfficeKeepingRecord;

public class OfficeKeepingRecordDAOImpl extends GenericDAOHibernate<OfficeKeepingRecord> {

	@Override
	protected Class<OfficeKeepingRecord> getPersistentClass() {
		return OfficeKeepingRecord.class;
	}
	
	@SuppressWarnings("unchecked")
	public List<OfficeKeepingRecord> findDocuments(boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}				

		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}


}