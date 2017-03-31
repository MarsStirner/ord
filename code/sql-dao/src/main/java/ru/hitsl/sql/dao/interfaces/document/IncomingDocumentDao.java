package ru.hitsl.sql.dao.interfaces.document;

import ru.entity.model.document.IncomingDocument;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.Date;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:34 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface IncomingDocumentDao extends DocumentDao<IncomingDocument> {
    List<IncomingDocument> findControlledDocumentsByUser(String filter, AuthorizationData authData, Date controlDate);

    List<IncomingDocument> findRegistratedDocumentsByCriteria(String s);
}
