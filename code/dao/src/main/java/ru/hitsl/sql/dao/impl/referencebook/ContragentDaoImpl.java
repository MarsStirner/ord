package ru.hitsl.sql.dao.impl.referencebook;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentDao;

import java.util.List;

import static org.hibernate.criterion.MatchMode.ANYWHERE;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

@Repository("contragentDao")
public class ContragentDaoImpl extends ReferenceBookDaoImpl<Contragent> implements ContragentDao{

    @Override
    public Class<Contragent> getEntityClass() {
        return Contragent.class;
    }

    /**
     * Получить критерий для отбора Документов и их показа в расширенных списках
     * Обычно:
     *
     * @return критерий для документов с DISTINCT и нужными alias (with fetch strategy)
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimpleCriteria();
        result.createAlias("type", "type", LEFT_OUTER_JOIN);
        return result;
    }

    @Override
    public void applyFilter(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("value", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("shortName", filter, ANYWHERE));
            // По замечаниям от Опарина не надо фильтровать по названию типа котрагента
            //disjunction.add(Restrictions.ilike("type.value", filter, ANYWHERE));
            criteria.add(disjunction);
        }
    }

    @Override
    public List<Contragent> getByType(final ContragentType type) {
        final DetachedCriteria detachedCriteria = getListCriteria();
        detachedCriteria.add(Restrictions.eq("type", type));
        return getItems(detachedCriteria);
    }
}