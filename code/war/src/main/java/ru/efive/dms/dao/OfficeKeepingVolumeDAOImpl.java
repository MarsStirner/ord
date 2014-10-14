package ru.efive.dms.dao;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.user.User;
import ru.entity.model.document.OfficeKeepingVolume;

public class OfficeKeepingVolumeDAOImpl extends GenericDAOHibernate<OfficeKeepingVolume> {

    @Override
    protected Class<OfficeKeepingVolume> getPersistentClass() {
        return OfficeKeepingVolume.class;
    }

    @SuppressWarnings("unchecked")
    public OfficeKeepingVolume findDocumentById(String id) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        detachedCriteria.add(Restrictions.eq("id", Integer.valueOf(id)));
        List<OfficeKeepingVolume> in_results = getHibernateTemplate().findByCriteria(detachedCriteria);
        if (in_results != null && in_results.size() > 0) {
            return in_results.get(0);
        } else {
            return null;
        }
    }

    public List<OfficeKeepingVolume> findAllDocuments(Map<String, Object> in_map, String filter, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.VOLUME_PROJECT.getId())));
        }

        return getHibernateTemplate().findByCriteria(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map), filter));
    }


    public long countAllDocuments(Map<String, Object> in_map, String filter, boolean showDeleted, boolean showDrafts) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        if (!showDrafts) {
            detachedCriteria.add(Restrictions.not(Restrictions.eq("statusId", DocumentStatus.VOLUME_PROJECT.getId())));
        }

        return getCountOf(getSearchCriteria(getConjunctionSearchCriteria(detachedCriteria, in_map), filter));
    }

    @SuppressWarnings("unchecked")
    public List<OfficeKeepingVolume> findDocuments(Map<String, Object> filters, String filter, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", showDeleted));
        }
        return getHibernateTemplate().findByCriteria(getConjunctionSearchCriteria(getSearchCriteria(detachedCriteria, filter), filters), -1, 0);
    }

    @SuppressWarnings("unchecked")
    public long countDocuments(Map<String, Object> filters, String filter, boolean showDeleted) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (!showDeleted) {
            detachedCriteria.add(Restrictions.eq("deleted", false));
        }

        return getCountOf(getConjunctionSearchCriteria(getSearchCriteria(detachedCriteria, filter), filters));

    }

    public List<OfficeKeepingVolume> findAllVolumesByParentId(String parentId) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(getPersistentClass());
        detachedCriteria.setResultTransformer(DetachedCriteria.DISTINCT_ROOT_ENTITY);

        if (parentId != null && !parentId.equals("")) {
            detachedCriteria.add(Restrictions.eq("parentFile.id", Integer.valueOf("0" + parentId)));
        }

        addOrder(detachedCriteria, "volumeIndex", true);

        return getHibernateTemplate().findByCriteria(detachedCriteria);
    }

    @Override
    protected DetachedCriteria getSearchCriteria(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("volumeIndex", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("comments", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("keepingPeriodReasons", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));

            criteria.add(disjunction);
        }
        return criteria;
    }

    protected DetachedCriteria getConjunctionSearchCriteria(DetachedCriteria criteria, Map<String, Object> in_map) {

        if ((in_map != null) && (in_map.size() > 0)) {
            Conjunction conjunction = Restrictions.conjunction();
            String in_key = "";

            in_key = "volumeIndex";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.isNotNull(in_key));
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "shortDescription";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.ilike(in_key, in_map.get(in_key).toString(), MatchMode.ANYWHERE));
            }

            in_key = "statusId";
            if (in_map.get(in_key) != null && in_map.get(in_key).toString().length() > 0) {
                conjunction.add(Restrictions.eq(in_key, Integer.parseInt(in_map.get(in_key).toString())));
            }

            in_key = "collector";
            if (in_map.get(in_key) != null) {
                User author = (User) in_map.get(in_key);
                //detachedCriteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
                conjunction.add(Restrictions.ilike(in_key + ".lastName", author.getLastName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".middleName", author.getMiddleName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".firstName", author.getFirstName(), MatchMode.ANYWHERE));
            }

            /*in_key="deliveryType";
               //if(in_map.get(in_key)!=null && in_map.get(in_key).toString().length()>0){
               if(in_map.get(in_key)!=null){
                   //detachedCriteria.createAlias(in_key, in_key, CriteriaSpecification.LEFT_JOIN);
                   conjunction.add(Restrictions.eq(in_key+".value", ((DeliveryType)in_map.get(in_key)).getValue()));
               }*/

            /*in_key="executionDate";
               if(in_map.get(in_key)!=null){
                   conjunction.add(Restrictions.sqlRestriction("DATE_FORMAT("+in_key+", '%d.%m.%Y') like lower(?)", format.format(in_map.get(in_key)) + "%", new StringType()));
               }*/


            criteria.add(conjunction);
        }
        return criteria;
    }
}