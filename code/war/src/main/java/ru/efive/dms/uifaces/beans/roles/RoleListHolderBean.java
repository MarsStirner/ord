package ru.efive.dms.uifaces.beans.roles;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.LazyDataModelForRole;
import ru.entity.model.referenceBook.Role;

import org.springframework.stereotype.Controller;

@Controller("roleList")
@SpringScopeView
public class RoleListHolderBean extends AbstractDocumentLazyDataModelBean<Role> {

    @Autowired
    @Qualifier("roleLDM")
    private LazyDataModelForRole ldm;


}