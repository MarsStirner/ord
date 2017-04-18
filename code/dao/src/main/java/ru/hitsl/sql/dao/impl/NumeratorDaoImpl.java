package ru.hitsl.sql.dao.impl;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.Numerator;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;

/**
 * Created by EUpatov on 18.04.2017.
 */
@Repository("numeratorDao")
@Transactional(propagation = Propagation.MANDATORY)
public class NumeratorDaoImpl extends CommonDaoImpl<Numerator> implements NumeratorDao {
    @Override
    public Class<Numerator> getEntityClass() {
        return Numerator.class;
    }

    @Override
    public DetachedCriteria getSimpleCriteria() {
        final DetachedCriteria result = super.getSimpleCriteria();
        result.createAlias("author", "author", JoinType.INNER_JOIN);
        result.createAlias("documentType", "documentType", JoinType.LEFT_OUTER_JOIN);
        return result;
    }

    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result =getSimpleCriteria();
        result.createAlias("nomenclature", "nomenclature", JoinType.LEFT_OUTER_JOIN);
        return result;
    }
}
