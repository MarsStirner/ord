package ru.hitsl.sql.dao.impl.mapped;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.mapped.DeletableEntity;
import ru.hitsl.sql.dao.interfaces.mapped.criteria.Deletable;

/**
 * Author: Upatov Egor <br>
 * Date: 21.03.2017, 18:24 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public abstract class DeletableDaoImpl<T extends DeletableEntity> extends DaoImpl<T> implements Deletable<T> {
    @Override
    @Transactional(transactionManager = "ordTransactionManager", propagation = Propagation.MANDATORY)
    public boolean delete(T entity) {
        log.trace("Delete {}", entity);
        entity.setDeleted(true);
        update(entity);
        log.debug("Deleted {}", entity);
        return entity.isDeleted();
    }
}
