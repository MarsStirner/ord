package ru.hitsl.sql.dao.impl.referencebook;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;

import java.util.List;

import static org.hibernate.sql.JoinType.INNER_JOIN;

@Repository("documentFormDao")
@Transactional(propagation = Propagation.MANDATORY)
public class DocumentFormDaoImpl extends ReferenceBookDaoImpl<DocumentForm> implements DocumentFormDao{
    @Override
    public Class<DocumentForm> getEntityClass() {
        return DocumentForm.class;
    }

    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria criteria = getSimpleCriteria();
        criteria.createAlias("documentType", "documentType", INNER_JOIN);
        return criteria;
    }

    @Override
    public List<DocumentForm> findByDocumentTypeCode(final String documentTypeCode) {
        final DetachedCriteria criteria = getListCriteria();
        criteria.add(Restrictions.eq("documentType.code", documentTypeCode));
        return getItems(criteria);
    }

    @Override
    public List<DocumentForm> findByDocumentTypeCodeAndValue(final String documentTypeCode, final String value) {
        final DetachedCriteria criteria = getListCriteria();
        criteria.add(Restrictions.eq("documentType.code", documentTypeCode));
        criteria.add(Restrictions.eq("value", value));
        return getItems(criteria);
    }


}