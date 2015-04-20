package ru.efive.dms.dao.ejb;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.joda.time.LocalDate;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.user.Substitution;
import ru.entity.model.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 20:10 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class SubstitutionDaoImpl extends GenericDAOHibernate<Substitution> {

    @Override
    public Class<Substitution> getPersistentClass() {
        return Substitution.class;
    }

    public List<Substitution> getAll() {
        return getHibernateTemplate().findByNamedQuery("Substitutions.getAll");
    }

    public List<Substitution> getDocuments(
            final String filter,
            final boolean showDeleted,
            int first,
            int pageSize,
            String sortField,
            boolean sortOrder
    ) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        if (StringUtils.isNotEmpty(sortField)) {
            criteria.addOrder(sortOrder ? Order.asc(sortField) : Order.desc(sortField));
        }
        applyFilterCriteria(criteria, filter);
        criteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(criteria, first, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<Substitution>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        DetachedCriteria resultCriteria = getDistinctCriteria(showDeleted).add(Restrictions.in("id", ids));
        if (StringUtils.isNotEmpty(sortField)) {
            resultCriteria.addOrder(sortOrder ? Order.asc(sortField) : Order.desc(sortField));
        }
        return getHibernateTemplate().findByCriteria(resultCriteria);
    }

    public int getDocumentsCount(final String filter, final boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        applyFilterCriteria(criteria, filter);
        return (int) getCountOf(criteria);
    }

    public List<Substitution> findDocumentsOnPerson(final User person, final boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.add(Restrictions.eq("person.id", person.getId()));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List<Substitution> findCurrentDocumentsOnPerson(final User person, final boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.add(Restrictions.eq("person.id", person.getId()));
        addCurrentDateRestrictions(criteria);
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List<Substitution> findDocumentsOnSubstitution(final User substitution, final boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.add(Restrictions.eq("substitution.id", substitution.getId()));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List<Substitution> findCurrentDocumentsOnSubstitution(final User substitution, final boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.add(Restrictions.eq("substitution.id", substitution.getId()));
        addCurrentDateRestrictions(criteria);
        return getHibernateTemplate().findByCriteria(criteria);
    }


    /**
     * Возвращает используемый везде запрос к замещениям (DISTINCT) с проверкой флага удаления
     *
     * @param showDeleted true - удаленные попадут в список \ false - в списке будут только не удаленные
     * @return типовой запрос
     */
    private DetachedCriteria getDistinctCriteria(final boolean showDeleted) {
        final DetachedCriteria result = DetachedCriteria.forClass(Substitution.class);
        result.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        if (!showDeleted) {
            result.add(Restrictions.eq("deleted", false));
        }
        result.createAlias("person", "person", CriteriaSpecification.INNER_JOIN);
        result.createAlias("substitution", "substitution", CriteriaSpecification.INNER_JOIN);
        result.createAlias("person.groups", "person.groups", CriteriaSpecification.LEFT_JOIN);
        result.createAlias("person.roles", "person.roles", CriteriaSpecification.LEFT_JOIN);
        return result;
    }

    /**
     * Добавляет к исходному запросу отграничение на вхождение текущей даты в интервал startDate и endDate
     *
     * @param criteria исходный запрос
     */
    private void addCurrentDateRestrictions(final DetachedCriteria criteria) {
        final LocalDate currentDate = new LocalDate();
        criteria.add(Restrictions.le("startDate", currentDate.toDate()));
        criteria.add(Restrictions.ge("endDate", currentDate.toDate()));
    }

    /**
     * Обрабатывает поисковый шаблон через ИЛИ (НУЖНА LIST_CRITERIA)
     * @param criteria критерий отбора
     * @param filter поисковый шаблон
     */
    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter){
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("person.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("person.firstName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("person.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("substitution.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("substitution.firstName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("substitution.middleName", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
    }
}
