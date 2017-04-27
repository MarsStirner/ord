package ru.hitsl.sql.dao.impl;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.report.Report;
import ru.hitsl.sql.dao.impl.mapped.CommonDaoImpl;
import ru.hitsl.sql.dao.interfaces.ReportDao;

import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;


@Repository("reportDao")
@Transactional(propagation = Propagation.MANDATORY)
public class ReportDaoImpl extends CommonDaoImpl<Report> implements ReportDao {

    @Override
    public DetachedCriteria getFullCriteria() {
        final DetachedCriteria result = getListCriteria();
        result.createAlias("parameters", "parameters", LEFT_OUTER_JOIN);
        return result;
    }

    @Override
    public Class<Report> getEntityClass() {
        return Report.class;
    }


}