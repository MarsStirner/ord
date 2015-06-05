package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.referenceBook.DepartmentDAOImpl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.DEPARTMENT_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 08.09.2014, 15:59 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverter("departmentConverter")
public class DepartmentConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        if (value != null && value.trim().length() > 0) {
            SessionManagementBean sessionManagement = fc.getApplication().evaluateExpressionGet(fc, "#{sessionManagement}", SessionManagementBean.class);
            DepartmentDAOImpl service = sessionManagement.getDAO(DepartmentDAOImpl.class, DEPARTMENT_DAO);
            final List<Department> departmentList = service.getByValue(value);
            if (departmentList.isEmpty()) {
                return null;
            }
            return departmentList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }
}
