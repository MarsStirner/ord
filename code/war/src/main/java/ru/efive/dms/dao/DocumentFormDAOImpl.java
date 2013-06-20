package ru.efive.dms.dao;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.efive.dms.data.DocumentForm;

public class DocumentFormDAOImpl extends DictionaryDAOHibernate<DocumentForm> {

    @Override
    protected Class<DocumentForm> getPersistentClass() {
        return DocumentForm.class;
    }

}