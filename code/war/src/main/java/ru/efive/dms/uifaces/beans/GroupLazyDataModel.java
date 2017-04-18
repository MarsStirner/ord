package ru.efive.dms.uifaces.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
import ru.entity.model.referenceBook.Group;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupDao;

/**
 * Author: Upatov Egor <br>
 * Date: 17.04.2015, 17:37 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("groupLDM")
public class GroupLazyDataModel extends AbstractFilterableLazyDataModel<Group> {
    @Autowired
    public GroupLazyDataModel(@Qualifier("groupDao")GroupDao groupDao) {
        super(groupDao);
    }
}