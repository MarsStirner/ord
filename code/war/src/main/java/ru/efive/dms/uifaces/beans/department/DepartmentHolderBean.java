package ru.efive.dms.uifaces.beans.department;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean;
import ru.efive.uifaces.bean.FromStringConverter;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.referenceBook.DepartmentDAOImpl;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.DEPARTMENT_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 18.09.2014, 12:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для работы с Подразделением<br>
 */

@Named("department")
@ConversationScoped
public class DepartmentHolderBean extends AbstractDocumentHolderBean<Department, Integer> implements Serializable {
    private static final Logger LOGGER = LoggerFactory.getLogger("DEPARTMENT");

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
        return sessionManagementBean.isAdministrator();
    }

    public boolean isCanDelete() {
        return sessionManagementBean.isAdministrator();
    }

    public boolean isCanEdit() {
        return sessionManagementBean.isAdministrator();
    }

    @Override
    protected boolean deleteDocument() {
        getDocument().setDeleted(true);
        return getDocument().isDeleted();
    }

    @Override
    protected Integer getDocumentId() {
        return getDocument().getId();
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
        setDocument(sessionManagementBean.getDAO(DepartmentDAOImpl.class, DEPARTMENT_DAO).get(documentId));
    }

    @Override
    protected boolean saveNewDocument() {
        try {
            setDocument(sessionManagementBean.getDAO(DepartmentDAOImpl.class, DEPARTMENT_DAO).save(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE NEW:", e);
            return false;
        }
    }

    @Override
    protected boolean saveDocument() {
        try {
            setDocument(sessionManagementBean.getDAO(DepartmentDAOImpl.class, DEPARTMENT_DAO).update(getDocument()));
            return true;
        } catch (Exception e) {
            LOGGER.error("CANT SAVE:", e);
            return false;
        }
    }

    @Override
    protected FromStringConverter<Integer> getIdConverter() {
        return FromStringConverter.INTEGER_CONVERTER;
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagementBean;
}
