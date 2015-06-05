package ru.hitsl.sql.dao.referenceBook;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

import java.util.List;

@SuppressWarnings("unchecked")
public class DocumentFormDAOImpl extends DictionaryDAOHibernate<DocumentForm> {
    @Override
    protected Class<DocumentForm> getPersistentClass() {
        return DocumentForm.class;
    }

    public List<DocumentForm> findByDocumentTypeCode(final String documentTypeCode) {
        final DetachedCriteria criteria = getCriteria();
        criteria.createAlias("documentType", "documentType", CriteriaSpecification.INNER_JOIN);
        criteria.add(Restrictions.eq("documentType.code", documentTypeCode));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List<DocumentForm> findByDocumentTypeCodeAndValue(final String documentTypeCode, final String value) {
        final DetachedCriteria criteria = getCriteria();
        criteria.createAlias("documentType", "documentType", CriteriaSpecification.INNER_JOIN);
        criteria.add(Restrictions.eq("documentType.code", documentTypeCode));
        criteria.add(Restrictions.eq("value", value));
        return getHibernateTemplate().findByCriteria(criteria);
    }
}