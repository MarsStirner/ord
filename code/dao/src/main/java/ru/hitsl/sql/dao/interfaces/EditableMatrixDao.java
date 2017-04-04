package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.util.DocumentTypeField;
import ru.entity.model.util.EditableFieldMatrix;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 20:19 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface EditableMatrixDao {
    List<EditableFieldMatrix> getFieldMatrixByDocumentTypeCode(String documentTypeCode);

    List<DocumentTypeField> getDocumentTypeFieldByDocumentTypeCode(String documentTypeCode);

    void updateValues(List<EditableFieldMatrix> list);
}
