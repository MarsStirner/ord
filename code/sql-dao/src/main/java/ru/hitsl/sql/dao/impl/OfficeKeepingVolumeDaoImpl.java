package ru.hitsl.sql.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.*;
import org.springframework.stereotype.Repository;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingVolumeDao;

import java.util.List;
import java.util.Map;

@Repository("officeKeepingVolumeDao")
public class OfficeKeepingVolumeDaoImpl extends CommonDaoImpl<OfficeKeepingVolume> implements OfficeKeepingVolumeDao {

    @Override
    public Class<OfficeKeepingVolume> getEntityClass() {
        return OfficeKeepingVolume.class;
    }

    @Override
    public List<OfficeKeepingVolume> findAllVolumesByParentId(String parentId) {
        DetachedCriteria detachedCriteria = getListCriteria();
        if (StringUtils.isNotEmpty(parentId)) {
            detachedCriteria.add(Restrictions.eq("parentFile.id", Integer.valueOf("0" + parentId)));
        }
        applyOrder(detachedCriteria, Order.asc("volumeIndex"));
        return getItems(detachedCriteria);
    }

    @Override
    public void applyFilter(DetachedCriteria criteria, String filter) {
        if (StringUtils.isNotEmpty(filter)) {
            Disjunction disjunction = Restrictions.disjunction();
            disjunction.add(Restrictions.ilike("volumeIndex", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("comments", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("keepingPeriodReasons", filter, MatchMode.ANYWHERE));
            disjunction.add(Restrictions.ilike("shortDescription", filter, MatchMode.ANYWHERE));
            criteria.add(disjunction);
        }
    }

    @Override
    public void applyFilter(DetachedCriteria criteria, Map<String, Object> in_map) {
        if ((in_map != null) && (in_map.size() > 0)) {
            Conjunction conjunction = Restrictions.conjunction();
            String in_key;

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
                //detachedCriteria.createAlias(in_key, in_key, JoinType.LEFT_OUTER_JOIN);
                conjunction.add(Restrictions.ilike(in_key + ".lastName", author.getLastName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".middleName", author.getMiddleName(), MatchMode.ANYWHERE));
                conjunction.add(Restrictions.ilike(in_key + ".firstName", author.getFirstName(), MatchMode.ANYWHERE));
            }

            criteria.add(conjunction);
        }
    }
}