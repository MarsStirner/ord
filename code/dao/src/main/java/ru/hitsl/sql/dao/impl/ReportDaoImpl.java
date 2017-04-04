package ru.hitsl.sql.dao.impl;

import org.springframework.stereotype.Repository;
import ru.entity.model.document.ReportTemplate;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.ReportDao;


@Repository("reportDao")
public class ReportDaoImpl extends CommonDaoImpl<ReportTemplate> implements ReportDao {

    @Override
    public Class<ReportTemplate> getEntityClass() {
        return ReportTemplate.class;
    }


}