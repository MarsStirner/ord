package ru.hitsl.sql.dao.impl;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.OfficeKeepingFile;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;

import java.util.List;

@Repository("officeKeepingFileDao")
@Transactional(propagation = Propagation.MANDATORY)
public class OfficeKeepingFileDaoImpl extends CommonDaoImpl<OfficeKeepingFile>
        implements OfficeKeepingFileDao {

    @Override
    public Class<OfficeKeepingFile> getEntityClass() {
        return OfficeKeepingFile.class;
    }

    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        result.createAlias("history", "history", JoinType.LEFT_OUTER_JOIN);
        result.createAlias("volumes", "volumes", JoinType.LEFT_OUTER_JOIN);
        return result;
    }

    public List<OfficeKeepingFile> getItemsByNumber(String fileIndex) {
        DetachedCriteria detachedCriteria = getListCriteria();
        detachedCriteria.add(Restrictions.eq("deleted", false));
        detachedCriteria.add(Restrictions.eq("fileIndex", fileIndex));
               return getItems(detachedCriteria);
    }

    @Override
    public long countItemsByNumber(String fileIndex) {
        DetachedCriteria detachedCriteria = getListCriteria();
        detachedCriteria.add(Restrictions.eq("deleted", false));
        detachedCriteria.add(Restrictions.eq("fileIndex", fileIndex));
             return countItems(detachedCriteria);
    }
}