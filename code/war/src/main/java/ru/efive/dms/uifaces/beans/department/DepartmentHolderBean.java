package ru.efive.dms.uifaces.beans.department;

import com.github.javaplugs.jsf.SpringScopeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 18.09.2014, 12:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для работы с Подразделением<br>
 */

@Controller("department")
@SpringScopeView
public class DepartmentHolderBean extends AbstractDocumentHolderBean<Department> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger("DEPARTMENT");


    @Autowired
    @Qualifier("departmentDao")
    private DepartmentDao departmentDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;
    @Autowired
    @Qualifier("sessionManagement")
    private transient SessionManagementBean sessionManagementBean;

    /**
     * Меняет значение флажка Deleted и сохраняет это в БД
     *
     * @return успешность сохранения изменений в БД
     */
    public boolean changeDeleted() {
        Department item = getDocument();
        item.setDeleted(!item.isDeleted());
        return saveDocument();
    }

    public boolean isCanCreate() {
        return authData.isAdministrator();
    }

    public boolean isCanDelete() {
        return authData.isAdministrator();
    }

    public boolean isCanEdit() {
        return authData.isAdministrator();
    }

    @Override
    protected boolean deleteDocument() {
        getDocument().setDeleted(true);
        return getDocument().isDeleted();
    }

    @Override
    protected void initNewDocument() {
        final Department newItem = new Department();
        newItem.setDeleted(false);
        newItem.setValue("");
        setDocument(newItem);
    }

    @Override
    protected void initDocument(Integer documentId) {
        setDocument(departmentDao.get(documentId));
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            setDocument(departmentDao.save(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE NEW:", e);
            return false;
        }
    }

    @Override
    protected boolean saveDocument() {
        try {
            setDocument(departmentDao.update(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE:", e);
            return false;
        }
    }
}
