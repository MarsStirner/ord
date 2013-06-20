package ru.efive.dms.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.data.OfficeKeepingFile;
import ru.efive.dms.data.Task;

public class OfficeKeepingFileDAOImpl extends GenericDAOHibernate<OfficeKeepingFile> {

	@Override
	protected Class<OfficeKeepingFile> getPersistentClass() {
		return OfficeKeepingFile.class;
	}

	public OfficeKeepingFile findDocumentById(String id) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.eq("id",Integer.valueOf(id)));
		List<OfficeKeepingFile> in_results=getHibernateTemplate().findByCriteria(detachedCriteria);		
		if(in_results!=null && in_results.size()>0){			
			return in_results.get(0);
		}else{			
			return null;	
		}		
	}

	@SuppressWarnings("unchecked")
	public List<OfficeKeepingFile> findDocuments(boolean showDeleted) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		if (!showDeleted) {
			detachedCriteria.add(Restrictions.eq("deleted", false));
		}				

		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}

	@SuppressWarnings("unchecked")
	public List<OfficeKeepingFile> findDocumentsByNumber(String fileIndex) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.eq("deleted", false));

		detachedCriteria.add(Restrictions.eq("fileIndex", fileIndex));
		detachedCriteria.add(Restrictions.gt("statusId", 1));
		
		return getHibernateTemplate().findByCriteria(detachedCriteria);
	}

	@SuppressWarnings("unchecked")
	public long countDocumentsByNumber(String fileIndex) {
		DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
		detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

		detachedCriteria.add(Restrictions.eq("deleted", false));

		detachedCriteria.add(Restrictions.eq("fileIndex", fileIndex));
		detachedCriteria.add(Restrictions.gt("statusId", 1));
		return getCountOf(detachedCriteria);
		
	}
}