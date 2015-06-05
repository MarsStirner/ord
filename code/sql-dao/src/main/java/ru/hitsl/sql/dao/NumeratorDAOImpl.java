package ru.hitsl.sql.dao;

import org.hibernate.FetchMode;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.document.Numerator;

import java.util.List;


public class NumeratorDAOImpl extends GenericDAOHibernate<Numerator> {


    @Override
    protected Class<Numerator> getPersistentClass() {
        return Numerator.class;
    }

    @SuppressWarnings("unchecked")
    public Numerator findDocumentById(Integer id) {
        final DetachedCriteria detachedCriteria = getEagerCriteria(false);
        detachedCriteria.add(Restrictions.eq("id", id));
        List<Numerator> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && !in_results.isEmpty()) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    private DetachedCriteria getEagerCriteria(boolean createAliases){
        final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.setFetchMode("author", FetchMode.JOIN);
        detachedCriteria.setFetchMode("nomenclature", FetchMode.JOIN);
        if(createAliases){
            detachedCriteria.createAlias("author", "author");
            detachedCriteria.createAlias("nomenclature", "nomenclature", CriteriaSpecification.LEFT_JOIN);
            detachedCriteria.createAlias("documentType", "documentType", CriteriaSpecification.LEFT_JOIN);
        }
        return detachedCriteria;
    }

    private DetachedCriteria getLazyCriteria(){
        final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return detachedCriteria;
    }

    @Override
    public List<Numerator> findDocuments(boolean showDeleted, int offset, int count, String orderBy, boolean orderAsc) {
        final DetachedCriteria detachedCriteria = getEagerCriteria(true);
        addDeletedRestriction(detachedCriteria, showDeleted);
        String[] ords = orderBy == null ? null : orderBy.split(",");
        if (ords != null) {
            if (ords.length > 1) {
                addOrder(detachedCriteria, ords, orderAsc);
            } else {
                addOrder(detachedCriteria, orderBy, orderAsc);
            }
        }
        return getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
    }

    @Override
    public long countDocument(boolean showDeleted) {
        final DetachedCriteria detachedCriteria = getLazyCriteria();
        addDeletedRestriction(detachedCriteria, showDeleted);
        return getCountOf(detachedCriteria);
    }


    /**
     * Добавление ограничения на удаленные документы в запрос
     *
     * @param in_searchCriteria запрос, куда будет добалено ограничение
     * @param showDeleted       true - в запрос будет добавлено ограничение на проверку флага, так чтобы документ не был удален
     */
    private void addDeletedRestriction(final DetachedCriteria in_searchCriteria, final boolean showDeleted) {
        if (!showDeleted) {
            in_searchCriteria.add(Restrictions.eq("deleted", false));
        }
    }

}
