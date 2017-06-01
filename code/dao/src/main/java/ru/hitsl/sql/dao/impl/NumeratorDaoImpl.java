package ru.hitsl.sql.dao.impl;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.Numerator;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;

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
        criteria.add(Restrictions.or(Restrictions.isNull("documentType.code"), Restrictions.eqOrIsNull("documentType.code", type.getName())));
        criteria.add(Restrictions.or(Restrictions.isNull("documentForm"), Restrictions.eqOrIsNull("documentForm", form)));
        criteria.add(Restrictions.or(Restrictions.isNull("controller"), Restrictions.eqOrIsNull("controller", controller)));
        criteria.add(Restrictions.or(Restrictions.isNull("contragent"), Restrictions.eqOrIsNull("contragent", contragent)));
        criteria.addOrder(Order.desc("priority"));
        final List<Numerator> items = getItems(criteria, 0, 10);
        if(log.isDebugEnabled()){
            log.debug("Top 10 numerators found:");
            for (Numerator item : items) {
               log.debug("{}", item);
            }
        }
        return !items.isEmpty() ? items.get(0) : null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String incrementAndGet(Numerator numerator) {
        numerator.setCurrent(numerator.getCurrent() + 1);
        update(numerator);
        return numerator.getPrefix() + numerator.getCurrent();
    }
}
