package ru.efive.dms.dao;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.util.ApplicationHelper;
import ru.entity.model.document.Nomenclature;

public class NomenclatureDAOImpl extends DictionaryDAOHibernate<Nomenclature> {

    @Override
    protected Class<Nomenclature> getPersistentClass() {
        return Nomenclature.class;
    }

    public List<Nomenclature> findByDescription(String description) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (ApplicationHelper.nonEmptyString(description)) {
            detachedCriteria.add(Restrictions.eq("description", description));
        }

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

}