package ru.hitsl.sql.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.SubstitutionDao;

import java.time.LocalDate;
import java.util.List;

import static org.hibernate.criterion.MatchMode.ANYWHERE;
import static org.hibernate.sql.JoinType.INNER_JOIN;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 20:10 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Repository("substitutionDao")
@Transactional(propagation = Propagation.MANDATORY)
public class SubstitutionDaoImpl extends CommonDaoImpl<Substitution> implements SubstitutionDao{

    @Override
    public Class<Substitution> getEntityClass() {
        return Substitution.class;
    }


    @Override
    public List<Substitution> getItemsOnPerson(final User person, final boolean showDeleted) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("person.id", person.getId()));
        return getItems(criteria);
    }

    @Override
    public List<Substitution> getCurrentItemsOnPerson(final User person, final boolean showDeleted) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("person.id", person.getId()));
        addCurrentDateRestrictions(criteria);
        return getItems(criteria);
    }

    @Override
    public List<Substitution> getItemsOnSubstitution(final User substitution, final boolean showDeleted) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("substitution.id", substitution.getId()));
        return getItems(criteria);
    }

    @Override
    public List<Substitution> getCurrentItemsOnSubstitution(final User substitution, final boolean showDeleted) {
        final DetachedCriteria criteria = getListCriteria();
        applyDeletedRestriction(criteria, showDeleted);
        criteria.add(Restrictions.eq("substitution.id", substitution.getId()));
        addCurrentDateRestrictions(criteria);
        return getItems(criteria);
    }


    /**
     * Возвращает используемый везде запрос к замещениям (DISTINCT) с проверкой флага удаления
     *
     * @return типовой запрос
     */
    @Override
    public DetachedCriteria getListCriteria() {
        final DetachedCriteria result = getSimpleCriteria();
        result.createAlias("person", "person", INNER_JOIN);
        result.createAlias("substitution", "substitution", INNER_JOIN);
        result.createAlias("person.groups", "person.groups", LEFT_OUTER_JOIN);
        result.createAlias("person.roles", "person.roles", LEFT_OUTER_JOIN);
        return result;
    }

    /**
     * Добавляет к исходному запросу отграничение на вхождение текущей даты в интервал startDate и endDate
     *
     * @param criteria исходный запрос
     */
    private void addCurrentDateRestrictions(final DetachedCriteria criteria) {
        final LocalDate currentDate = LocalDate.now();
        criteria.add(Restrictions.le("startDate", currentDate));
        criteria.add(Restrictions.ge("endDate", currentDate));
    }

    /**
     * Обрабатывает поисковый шаблон через ИЛИ (НУЖНА LIST_CRITERIA)
     *
     * @param criteria критерий отбора
     * @param filter   поисковый шаблон
     */
    @Override
    public void applyFilter(final DetachedCriteria criteria, final String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("person.lastName", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("person.firstName", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("person.middleName", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("substitution.lastName", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("substitution.firstName", filter, ANYWHERE));
            disjunction.add(Restrictions.ilike("substitution.middleName", filter, ANYWHERE));
            disjunction.add(createDateLikeTextRestriction("startDate", filter));
            disjunction.add(createDateLikeTextRestriction("endDate", filter));
            criteria.add(disjunction);
        }
    }
}
