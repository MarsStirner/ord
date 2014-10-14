package ru.efive.dms.dao;

import java.util.Collections;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.document.HistoryEntry;

public class HistoryDAOImpl extends GenericDAOHibernate<HistoryEntry> {

    @Override
    protected Class<HistoryEntry> getPersistentClass() {
        return HistoryEntry.class;
    }

    /**
     * Поиск записей в истории
     *
     * @param parentId - идентификатор parent документа
     * @return - список записей
     */
    @SuppressWarnings("unchecked")
    public List<HistoryEntry> findDocumentsByParentId(String parentId, int offset, int count, String orderBy, boolean orderAsc) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);
        if (parentId != null && !parentId.equals("")) {
            detachedCriteria.add(Restrictions.eq("parentId", parentId));
            String[] ords = orderBy == null ? null : orderBy.split(",");
            if (ords != null) {
                if (ords.length > 1) {
                    addOrder(detachedCriteria, ords, orderAsc);
                } else {
                    addOrder(detachedCriteria, orderBy, orderAsc);
                }
            }
            return getHibernateTemplate().findByCriteria(detachedCriteria, offset, count);
        } else {
            return Collections.EMPTY_LIST;
        }
    }


}