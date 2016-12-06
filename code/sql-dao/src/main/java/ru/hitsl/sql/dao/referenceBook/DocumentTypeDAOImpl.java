package ru.hitsl.sql.dao.referenceBook;

import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

/**
 * Author: Upatov Egor <br>
 * Date: 03.06.2015, 21:57 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class DocumentTypeDAOImpl extends DictionaryDAOHibernate<DocumentType> {
    @Override
    protected Class<DocumentType> getPersistentClass() {
        return DocumentType.class;
    }
}
