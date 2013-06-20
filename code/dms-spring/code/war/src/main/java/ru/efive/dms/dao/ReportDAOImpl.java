package ru.efive.dms.dao;

import ru.efive.sql.dao.GenericDAOHibernate;
import ru.efive.dms.data.ReportTemplate;

public class ReportDAOImpl extends GenericDAOHibernate<ReportTemplate> {
	
	@Override
	protected Class<ReportTemplate> getPersistentClass() {
		return ReportTemplate.class;
	}
	
}