package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentTypeDao;

/**
 * Author: Upatov Egor <br>
 * Date: 03.06.2015, 21:57 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Repository("documentTypeDao")
@Transactional(propagation = Propagation.MANDATORY)
public class DocumentTypeDaoImpl extends ReferenceBookDaoImpl<DocumentType> implements DocumentTypeDao{
    @Override
    public Class<DocumentType> getEntityClass() {
        return DocumentType.class;
    }
}
