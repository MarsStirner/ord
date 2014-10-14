package ru.efive.dms.dao;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.document.ReportTemplate;

public class ReportDAOImpl extends GenericDAOHibernate<ReportTemplate> {

    @Override
    protected Class<ReportTemplate> getPersistentClass() {
        return ReportTemplate.class;
    }

}