package ru.hitsl.sql.dao.impl;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.util.DocumentTypeField;
import ru.entity.model.util.EditableFieldMatrix;
import ru.hitsl.sql.dao.interfaces.EditableMatrixDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 08.07.2015, 18:47 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Repository("editableMatrixDao")
@Transactional("ordTransactionManager")
public class EditableMatrixDaoImpl implements EditableMatrixDao {

    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public List<EditableFieldMatrix> getFieldMatrixByDocumentTypeCode(final String documentTypeCode) {
        return sessionFactory.getCurrentSession().getNamedQuery("EditableFieldMatrix.getByDocumentTypeCode").setParameter("documentTypeCode", documentTypeCode).list();
    }

    @Override
    public List<DocumentTypeField> getDocumentTypeFieldByDocumentTypeCode(final String documentTypeCode) {
        return sessionFactory.getCurrentSession().getNamedQuery("DocumentTypeField.getByDocumentTypeCode").setParameter("documentTypeCode", documentTypeCode).list();
    }

    @Override
    public void updateValues(final List<EditableFieldMatrix> list) {
        list.forEach(sessionFactory.getCurrentSession()::update);
    }
}
