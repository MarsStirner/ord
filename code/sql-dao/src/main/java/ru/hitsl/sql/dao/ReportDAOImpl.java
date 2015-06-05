package ru.hitsl.sql.dao;

import ru.entity.model.document.ReportTemplate;

public class ReportDAOImpl extends GenericDAOHibernate<ReportTemplate> {

    @Override
    protected Class<ReportTemplate> getPersistentClass() {
        return ReportTemplate.class;
    }

}