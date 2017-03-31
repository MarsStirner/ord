package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentTypeDao;

/**
 * Author: Upatov Egor <br>
 * Date: 11.02.2015, 4:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Repository("contragentTypeDao")
public class ContragentTypeDaoImpl extends ReferenceBookDaoImpl<ContragentType> implements ContragentTypeDao {

    @Override
    public Class<ContragentType> getEntityClass() {
        return ContragentType.class;
    }
}