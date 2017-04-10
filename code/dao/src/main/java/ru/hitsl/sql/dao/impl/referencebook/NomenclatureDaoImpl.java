package ru.hitsl.sql.dao.impl.referencebook;

import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.Nomenclature;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.NomenclatureDao;

import java.util.List;

@Repository("nomenclatureDao")
@Transactional(propagation = Propagation.MANDATORY)
public class NomenclatureDaoImpl extends ReferenceBookDaoImpl<Nomenclature> implements NomenclatureDao {

    @Override
    public Nomenclature getUserDefaultNomenclature(User user) {
        final DetachedCriteria detachedCriteria = DetachedCriteria.forClass(User.class);
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        detachedCriteria.add(Restrictions.eq("id", user.getId()));
        detachedCriteria.setFetchMode("defaultNomenclature", FetchMode.JOIN);
        final List list = detachedCriteria.getExecutableCriteria(em.unwrap(Session.class)).list();
        return ((User) list.get(0)).getDefaultNomenclature();
    }

    public List<Nomenclature> getItems() {
        DetachedCriteria detachedCriteria = getListCriteria();
        detachedCriteria.addOrder(Order.asc("code"));
        return getItems(detachedCriteria);
    }

    @Override
    public Class<Nomenclature> getEntityClass() {
        return Nomenclature.class;
    }
}