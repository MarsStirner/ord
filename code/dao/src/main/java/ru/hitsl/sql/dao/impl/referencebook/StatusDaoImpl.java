package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.workflow.Status;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.StatusDao;

@Repository("statusDao")
@Transactional(propagation = Propagation.MANDATORY)
public class StatusDaoImpl extends ReferenceBookDaoImpl<Status> implements StatusDao {

    @Override
    public Class<Status> getEntityClass() {
        return Status.class;
    }
}