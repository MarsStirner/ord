package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.Role;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;

@FacesConverterWithSpringSupport("RoleConverter")
public class RoleConverter extends AbstractReferenceBookConverter<Role> {
    @Autowired
    public RoleConverter(@Qualifier("roleDao") final RoleDao roleDao) {
        super(roleDao);
    }
}