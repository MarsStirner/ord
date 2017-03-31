package ru.hitsl.sql.dao.interfaces.document;

import ru.entity.model.document.RequestDocument;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:34 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface RequestDocumentDao extends DocumentDao<RequestDocument> {
    List<RequestDocument> findRegistratedDocuments();
}
