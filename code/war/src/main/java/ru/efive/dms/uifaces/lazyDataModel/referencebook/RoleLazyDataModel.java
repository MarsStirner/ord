package ru.efive.dms.uifaces.lazyDataModel.referencebook;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.referenceBook.Role;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 17:16 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@ViewScopedLazyDataModel("roleLDM")
public class RoleLazyDataModel extends AbstractFilterableLazyDataModel<Role> {
    @Autowired
    public RoleLazyDataModel(@Qualifier("roleDao")RoleDao roleDao) {
        super(roleDao);
    }
}
