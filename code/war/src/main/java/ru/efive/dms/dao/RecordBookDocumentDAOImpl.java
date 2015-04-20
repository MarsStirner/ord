package ru.efive.dms.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import ru.efive.dms.util.security.AuthorizationData;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.document.RecordBookDocument;

import java.util.List;

public class RecordBookDocumentDAOImpl extends GenericDAOHibernate<RecordBookDocument> {

    @Override
    protected Class<RecordBookDocument> getPersistentClass() {
        return RecordBookDocument.class;
    }

    public DetachedCriteria getSimplestCriteria(){
        return DetachedCriteria.forClass(RecordBookDocument.class, "this").setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    public DetachedCriteria getListCriteria(){
        return getSimplestCriteria().createAlias("author", "author", CriteriaSpecification.INNER_JOIN);
    }

    public DetachedCriteria getEagerCriteria(){
        return getListCriteria();
    }

    public void applyFilterCriteria(final DetachedCriteria criteria, final String filter){
        if (StringUtils.isNotEmpty(filter)) {
            final Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("author.lastName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.middleName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("author.firstName", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("description", filter, MatchMode.ANYWHERE));
            disjunction.add(createDateLikeTextRestriction("plannedDate", filter));
            disjunction.add(createDateLikeTextRestriction("creationDate", filter));
            criteria.add(disjunction);
        }
    }

    public void applyAccessCriteria(final DetachedCriteria criteria, final AuthorizationData auth) {
        criteria.add(Restrictions.eq("author.id", auth.getAuthorized().getId()));
    }


    public List<RecordBookDocument> findDocuments(
            final AuthorizationData authData,
            final String filter,
            final String orderBy,
            final boolean orderAsc,
            final int first,
            final int pageSize
    ) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteria(criteria, filter);
        applyAccessCriteria(criteria, authData);
        if(StringUtils.isNotEmpty(orderBy)){
            criteria.addOrder(orderAsc ? Order.asc(orderBy) : Order.desc(orderBy));
        }
        return getHibernateTemplate().findByCriteria(criteria, first, pageSize);
    }

    public int countDocuments(final AuthorizationData authData, final String filter) {
        final DetachedCriteria criteria = getListCriteria();
        applyFilterCriteria(criteria, filter);
        applyAccessCriteria(criteria, authData);
        return (int) getCountOf(criteria);
    }

    /**
     * Создать часть критерия, которая будет проверять заданное поле(типа Дата-Время) на соотвтевие поисковому шаблону
     * @param fieldName  имя поля с типом  (Дата-Время)
     * @param filter  поисковый шаблон
     * @return Часть критерия, проверяющая сответвтвие поля поисковому шаблону
     */
    public Criterion createDateLikeTextRestriction(final String fieldName, final String filter) {
        return Restrictions.sqlRestriction(
                "DATE_FORMAT(".concat(fieldName).concat(", '%d.%m.%Y') like lower(?)"), filter + "%", new StringType()
        );
    }
}