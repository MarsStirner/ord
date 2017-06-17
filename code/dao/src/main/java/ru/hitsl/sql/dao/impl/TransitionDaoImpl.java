package ru.hitsl.sql.dao.impl;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.workflow.Status;
import ru.entity.model.workflow.Transition;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.TransitionDao;

import java.util.List;

@Repository("transitionDao")
@Transactional(propagation = Propagation.MANDATORY)
public class TransitionDaoImpl extends CommonDaoImpl<Transition> implements TransitionDao{

    @Override
    public Class<Transition> getEntityClass() {
        return Transition.class;
    }

    @Override
    public List<Transition> getAvailableTransitions(DocumentType documentType, Status status) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, false);
        criteria.add(Restrictions.or(Restrictions.isNull("documentType"), Restrictions.eq("documentType", documentType)));
        criteria.add(Restrictions.or(Restrictions.isNull("fromStatus"), Restrictions.eq("fromStatus", status)));
        return getItems(criteria);
    }
}
