package ru.hitsl.sql.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.ReportTemplate;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.ReportDao;


@Repository("reportDao")
@Transactional(propagation = Propagation.MANDATORY)
public class ReportDaoImpl extends CommonDaoImpl<ReportTemplate> implements ReportDao {

    @Override
    public Class<ReportTemplate> getEntityClass() {
        return ReportTemplate.class;
    }


}