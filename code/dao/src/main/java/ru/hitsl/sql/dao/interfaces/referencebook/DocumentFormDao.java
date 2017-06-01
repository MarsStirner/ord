package ru.hitsl.sql.dao.interfaces.referencebook;

import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:44 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface DocumentFormDao extends ReferenceBookDao<DocumentForm> {
    List<DocumentForm> findByDocumentTypeCode(String documentTypeCode);

    List<DocumentForm> findByDocumentTypeCodeAndValue(String documentTypeCode, String value);

    List<DocumentForm> findByDocumentType(final DocumentType documentType);
}
