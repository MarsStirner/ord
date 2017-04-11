package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.GroupType;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupTypeDao;

@FacesConverterWithSpringSupport("GroupTypeConverter")
public class GroupTypeConverter extends AbstractReferenceBookConverter<GroupType> {
    @Autowired
    public GroupTypeConverter(@Qualifier("groupTypeDao") final GroupTypeDao groupTypeDao) {
        super(groupTypeDao);
    }
}