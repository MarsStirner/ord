package ru.efive.dms.uifaces.beans.department;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 18.09.2014, 12:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для работы с Подразделением<br>
 */

@Controller("department")
@SpringScopeView
public class DepartmentHolderBean extends AbstractDocumentHolderBean<Department, DepartmentDao> {

    @Autowired
    public DepartmentHolderBean(@Qualifier("departmentDao") DepartmentDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    public boolean isCanCreate(AuthorizationData authData) {
        return authData.isAdministrator();
    }

    @Override
    public boolean isCanUpdate(AuthorizationData authData) {
        return authData.isAdministrator();
    }

    @Override
    public boolean isCanDelete(AuthorizationData authData) {
        return authData.isAdministrator();
    }

    @Override
    protected Department newModel(AuthorizationData authData) {
        final Department newItem = new Department();
        newItem.setDeleted(false);
        newItem.setValue("");
        return newItem;
    }
}
