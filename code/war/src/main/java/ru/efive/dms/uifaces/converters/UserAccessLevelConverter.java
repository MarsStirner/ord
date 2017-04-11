package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverter;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.hitsl.sql.dao.interfaces.referencebook.UserAccessLevelDao;

@FacesConverter("UserAccessLevelConverter")
public class UserAccessLevelConverter extends AbstractReferenceBookConverter<UserAccessLevel> {
    @Autowired
    public UserAccessLevelConverter(@Qualifier("userAccessLevelDao") final UserAccessLevelDao userAccessLevelDao) {
        super(userAccessLevelDao);
    }
}