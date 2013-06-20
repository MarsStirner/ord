package ru.efive.crm.dao;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.efive.crm.data.ContragentNomenclature;

public class NomenclatureDAOImpl extends DictionaryDAOHibernate<ContragentNomenclature> {
	
	@Override
	protected Class<ContragentNomenclature> getPersistentClass() {
		return ContragentNomenclature.class;
	}
	
}