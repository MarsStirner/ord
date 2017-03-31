package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.entity.model.referenceBook.Role;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 17:16 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Component("roleLDM")
@SpringScopeView
public class LazyDataModelForRole extends AbstractFilterableLazyDataModel<Role> {
    @Autowired
    public LazyDataModelForRole(@Qualifier("roleDao")RoleDao roleDao) {
        super(roleDao);
    }
}
