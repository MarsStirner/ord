package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.SenderType;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.SenderTypeDao;

@Repository("senderTypeDao")
@Transactional(propagation = Propagation.MANDATORY)
public class SenderTypeDaoImpl extends ReferenceBookDaoImpl<SenderType>  implements SenderTypeDao{
    @Override
    public Class<SenderType> getEntityClass() {
        return SenderType.class;
    }
}