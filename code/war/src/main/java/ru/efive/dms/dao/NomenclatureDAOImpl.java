package ru.efive.dms.dao;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.entity.model.document.Nomenclature;

import java.util.List;

public class NomenclatureDAOImpl extends DictionaryDAOHibernate<Nomenclature> {

    @Override
    protected Class<Nomenclature> getPersistentClass() {
        return Nomenclature.class;
    }

    public List<Nomenclature> findByDescription(String description) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (StringUtils.isNotEmpty(description)) {
            detachedCriteria.add(Restrictions.eq("description", description));
        }

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

}