package ru.hitsl.sql.dao.impl;

import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.numerator.Numerator;
import ru.entity.model.numerator.NumeratorUsage;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Repository("numeratorDao")
@Transactional(propagation = Propagation.MANDATORY)
public class NumeratorDaoImpl extends CommonDaoImpl<Numerator> implements NumeratorDao {
    @Override
    public Class<Numerator> getEntityClass() {
        return Numerator.class;
    }

    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimpleCriteria();
        result.createAlias("documentType", "documentType", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("documentForm", "documentForm", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("controller", "controller", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("contragent", "contragent", JoinType.LEFT_OUTER_JOIN);
        return result;
    }

    @Override
    public Numerator findBestNumerator(DocumentType type, DocumentForm form, User controller, Contragent contragent) {
        final DetachedCriteria criteria = getFullCriteria();
        final LocalDate now = LocalDate.now();
        criteria.add(Restrictions.le("begDate", now));
        criteria.add(Restrictions.or(Restrictions.isNull("endDate"), Restrictions.gt("endDate", now)));

        criteria.add(Restrictions.or(Restrictions.isNull("documentType.code"), Restrictions.eqOrIsNull("documentType", type)));
        criteria.add(Restrictions.or(Restrictions.isNull("documentForm"), Restrictions.eqOrIsNull("documentForm", form)));
        criteria.add(Restrictions.or(Restrictions.isNull("controller"), Restrictions.eqOrIsNull("controller", controller)));
        criteria.add(Restrictions.or(Restrictions.isNull("contragent"), Restrictions.eqOrIsNull("contragent", contragent)));
        criteria.addOrder(Order.desc("priority"));
        final List<Numerator> items = getItems(criteria, 0, 10);
        if (log.isDebugEnabled()) {
            log.debug("Top 10 numerators found:");
            for (Numerator item : items) {
                log.debug("{}", item);
            }
        }
        return !items.isEmpty() ? items.get(0) : null;
    }

    @Override
    public NumeratorUsage getUsage(String documentId) {
        DetachedCriteria criteria = DetachedCriteria.forClass(NumeratorUsage.class).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.createAlias("numerator", "numerator", JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("documentId", documentId));
        criteria.addOrder(Order.desc("registrationDate"));
        return (NumeratorUsage) criteria.getExecutableCriteria(em.unwrap(Session.class)).setMaxResults(1).uniqueResult();
    }

    @Override
    public NumeratorUsage createUsage(DocumentEntity doc, Numerator numerator) {
        final NumeratorUsage result = new NumeratorUsage();
        result.setDocumentId(doc.getUniqueId());
        result.setNumerator(numerator);
        result.setRegistrationDate(LocalDateTime.now());
        result.setNumber(getCurrentNumber(numerator) + 1);
        em.persist(result);
        return result;
    }

    @Override
    public void deleteUsage(NumeratorUsage usage) {
        em.remove(usage);
    }

    private int getCurrentNumber(Numerator numerator) {
        final NumeratorUsage last = getLastUsage(numerator);
        return last != null ? last.getNumber() : numerator.getStartNumber();
    }

    private NumeratorUsage getLastUsage(Numerator numerator) {
        DetachedCriteria criteria = DetachedCriteria.forClass(NumeratorUsage.class).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.createAlias("numerator", "numerator", JoinType.INNER_JOIN);
        criteria.add(Restrictions.eq("numerator", numerator));
        criteria.addOrder(Order.desc("number"));
        return (NumeratorUsage) criteria.getExecutableCriteria(em.unwrap(Session.class)).setMaxResults(1).uniqueResult();
    }
}
