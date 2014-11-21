package ru.efive.dms.dao.ejb;

import org.apache.commons.lang.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.*;
import org.joda.time.LocalDate;
import org.primefaces.model.SortOrder;
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

    public List<Substitution> getDocuments(final boolean showDeleted, int first, int pageSize, String sortField, SortOrder sortOrder) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        if (StringUtils.isNotEmpty(sortField)) {
            criteria.addOrder(sortOrder == SortOrder.ASCENDING ? Order.asc(sortField) : Order.desc(sortField));
        }
        criteria.setProjection(Projections.distinct(Projections.id()));
        //получаем список ключей от сущностей, которые нам нужны (с корректным [LIMIT offset, count])
        List ids = getHibernateTemplate().findByCriteria(criteria, first, pageSize);
        if (ids.isEmpty()) {
            return new ArrayList<Substitution>(0);
        }
        //Ищем только по этим ключам с упорядочиванием
        DetachedCriteria resultCriteria = DetachedCriteria.forClass(Substitution.class).add(Restrictions.in("id", ids));
        if (StringUtils.isNotEmpty(sortField)) {
            criteria.addOrder(sortOrder == SortOrder.ASCENDING ? Order.asc(sortField) : Order.desc(sortField));
        }
        resultCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        return getHibernateTemplate().findByCriteria(resultCriteria);
    }

    public int getDocumentsCount(boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.setProjection(Projections.rowCount());
        final Object result = getHibernateTemplate().findByCriteria(criteria).get(0);
        return ((Long) result).intValue();
    }

    public List<Substitution> findDocumentsOnPerson(User person, boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.add(Restrictions.eq("person.id", person.getId()));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List<Substitution> findCurrentDocumentsOnPerson(User person, boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.add(Restrictions.eq("person.id", person.getId()));
        addCurrentDateRestrictions(criteria);
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List<Substitution> findDocumentsOnSubstitution(User substitution, boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.add(Restrictions.eq("substitution.id", substitution.getId()));
        criteria.setFetchMode("person.groups", FetchMode.JOIN);
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List<Substitution> findCurrentDocumentsOnSubstitution(User substitution, boolean showDeleted) {
        final DetachedCriteria criteria = getDistinctCriteria(showDeleted);
        criteria.add(Restrictions.eq("substitution.id", substitution.getId()));
        addCurrentDateRestrictions(criteria);
        criteria.setFetchMode("person.groups", FetchMode.JOIN);
        return getHibernateTemplate().findByCriteria(criteria);
    }


    /**
     * Возвращает используемый везде запрос к замещениям (DISTINCT) с проверкой флага удаления
     *
     * @param showDeleted true - удаленные попадут в список \ false - в списке будут только не удаленные
     * @return типовой запрос
     */
    private DetachedCriteria getDistinctCriteria(boolean showDeleted) {
        final DetachedCriteria result = DetachedCriteria.forClass(Substitution.class);
        result.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        if (!showDeleted) {
            result.add(Restrictions.eq("deleted", false));
        }
        return result;
    }

    /**
     * Добавляет к исходному запросу отграничение на вхождение текущей даты в интервал startDate и endDate
     *
     * @param criteria исходный запрос
     */
    private void addCurrentDateRestrictions(DetachedCriteria criteria) {
        final LocalDate currentDate = new LocalDate();
        criteria.add(Restrictions.le("startDate", currentDate.toDate()));
        criteria.add(Restrictions.ge("endDate", currentDate.toDate()));
    }
}
