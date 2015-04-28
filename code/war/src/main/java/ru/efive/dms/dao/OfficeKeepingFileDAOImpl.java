package ru.efive.dms.dao;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.document.OfficeKeepingFile;
import ru.entity.model.enums.DocumentStatus;

import java.util.List;

public class OfficeKeepingFileDAOImpl extends GenericDAOHibernate<OfficeKeepingFile> {

    @Override
    protected Class<OfficeKeepingFile> getPersistentClass() {
        return OfficeKeepingFile.class;
    }

    public DetachedCriteria getSimplestCriteria(){
        return DetachedCriteria.forClass(OfficeKeepingFile.class, "this").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    public DetachedCriteria getListCriteria(){
        return getSimplestCriteria();
    }

    public DetachedCriteria getFullCriteria(){
        final DetachedCriteria result = getListCriteria();
        result.createAlias("history", "history", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("volumes", "volumes", CriteriaSpecification.LEFT_JOIN);
        return result;
    }

    public OfficeKeepingFile findDocumentById(Integer id) {
        DetachedCriteria detachedCriteria = getFullCriteria();
        detachedCriteria.add(Restrictions.eq("id",id));
        List<OfficeKeepingFile> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && in_results.size() > 0) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<OfficeKeepingFile> findDocuments(boolean showDeleted) {
        DetachedCriteria detachedCriteria = getListCriteria();
        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    @SuppressWarnings("unchecked")
    public List<OfficeKeepingFile> findDocumentsByNumber(String fileIndex) {
        DetachedCriteria detachedCriteria = getListCriteria();
        detachedCriteria.add(Restrictions.eq("deleted", false));
        detachedCriteria.add(Restrictions.eq("fileIndex", fileIndex));
        detachedCriteria.add(Restrictions.gt("statusId", DocumentStatus.ACTION_PROJECT.getId()));
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    @SuppressWarnings("unchecked")
    public long countDocumentsByNumber(String fileIndex) {
        DetachedCriteria detachedCriteria = getListCriteria();
        detachedCriteria.add(Restrictions.eq("deleted", false));
        detachedCriteria.add(Restrictions.eq("fileIndex", fileIndex));
        detachedCriteria.add(Restrictions.gt("statusId", DocumentStatus.ACTION_PROJECT.getId()));
        return getCountOf(detachedCriteria);
    }
}