package ru.hitsl.sql.dao.referenceBook;

import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import ru.entity.model.referenceBook.Nomenclature;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

import java.util.List;

public class NomenclatureDAOImpl extends DictionaryDAOHibernate<Nomenclature> {

    @Override
    protected Class<Nomenclature> getPersistentClass() {
        return Nomenclature.class;
    }

    public Nomenclature getUserDefaultNomenclature(User user) {
        final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("id", user.getId()));
        detachedCriteria.setFetchMode("defaultNomenclature", FetchMode.JOIN);
        final List list = getHibernateTemplate().findByCriteria(detachedCriteria);
        return ((User)list.get(0)).getDefaultNomenclature();
    }

    public List<Nomenclature> findDocuments(){
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.addOrder(Order.asc("code"));
        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }
}