package ru.hitsl.sql.dao.impl;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.util.DocumentTypeField;
import ru.entity.model.util.EditableFieldMatrix;
import ru.hitsl.sql.dao.interfaces.EditableMatrixDao;
import ru.util.ApplicationHelper;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 08.07.2015, 18:47 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Repository("editableMatrixDao")
@Transactional(propagation = Propagation.MANDATORY)
public class EditableMatrixDaoImpl implements EditableMatrixDao {

    @PersistenceContext(unitName = ApplicationHelper.ORD_PERSISTENCE_UNIT_NAME)
    private EntityManager em;


    @Override
    public List<EditableFieldMatrix> getFieldMatrixByDocumentTypeCode(final String documentTypeCode) {
        return em.createNamedQuery("EditableFieldMatrix.getByDocumentTypeCode", EditableFieldMatrix.class).setParameter("documentTypeCode", documentTypeCode).getResultList();
    }

    @Override
    public List<DocumentTypeField> getDocumentTypeFieldByDocumentTypeCode(final String documentTypeCode) {
        return em.createNamedQuery("DocumentTypeField.getByDocumentTypeCode", DocumentTypeField.class).setParameter("documentTypeCode", documentTypeCode).getResultList();
    }

    @Override
    public void updateValues(final List<EditableFieldMatrix> list) {
        list.forEach(em::merge);
    }
}
