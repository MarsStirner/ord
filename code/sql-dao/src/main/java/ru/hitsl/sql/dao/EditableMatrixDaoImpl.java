package ru.hitsl.sql.dao;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import ru.entity.model.util.DocumentTypeField;
import ru.entity.model.util.EditableFieldMatrix;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 08.07.2015, 18:47 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@SuppressWarnings("unchecked")
public class EditableMatrixDaoImpl extends HibernateDaoSupport {

    public List<EditableFieldMatrix> getFieldMatrixByDocumentTypeCode(final String documentTypeCode){
       return getHibernateTemplate().findByNamedQueryAndNamedParam("EditableFieldMatrix.getByDocumentTypeCode", "documentTypeCode", documentTypeCode);
    }

    public List<DocumentTypeField> getDocumentTypeFieldByDocumentTypeCode(final String documentTypeCode){
        return getHibernateTemplate().findByNamedQueryAndNamedParam("DocumentTypeField.getByDocumentTypeCode", "documentTypeCode", documentTypeCode);
    }

    public void updateValues(final List<EditableFieldMatrix> list) {
        getHibernateTemplate().saveOrUpdateAll(list);
    }
}
