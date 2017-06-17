package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.GroupType;
import ru.entity.model.workflow.Status;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupTypeDao;
import ru.hitsl.sql.dao.interfaces.referencebook.StatusDao;

@FacesConverterWithSpringSupport("StatusConverter")
public class StatusConverter extends AbstractReferenceBookConverter<Status> {
    @Autowired
    public StatusConverter(@Qualifier("statusDao") final StatusDao statusDao) {
        super(statusDao);
    }
}