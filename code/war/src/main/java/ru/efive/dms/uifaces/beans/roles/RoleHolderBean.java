package ru.efive.dms.uifaces.beans.roles;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.enums.RoleType;
import ru.entity.model.referenceBook.Role;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ViewScopedController("role")
public class RoleHolderBean extends AbstractDocumentHolderBean<Role, RoleDao> {

    @Autowired
    public RoleHolderBean(@Qualifier("roleDao") RoleDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    protected Role newModel(AuthorizationData authData) {
        return new Role();
    }


    public List<RoleType> getTypes() {
        List<RoleType> result = new ArrayList<>();
        Collections.addAll(result, RoleType.values());
        return result;
    }
}