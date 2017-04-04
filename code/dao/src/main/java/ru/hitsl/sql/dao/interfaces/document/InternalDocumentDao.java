package ru.hitsl.sql.dao.interfaces.document;

import ru.entity.model.document.InternalDocument;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:34 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface InternalDocumentDao extends DocumentDao<InternalDocument> {
    List<InternalDocument> findDocumentsByCriteria(Map<String, Object> in_filters, boolean b, boolean b1);
}
