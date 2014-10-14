package ru.efive.dms.dao;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.entity.model.document.DocumentForm;

public class DocumentFormDAOImpl extends DictionaryDAOHibernate<DocumentForm> {

    @Override
    protected Class<DocumentForm> getPersistentClass() {
        return DocumentForm.class;
    }

}