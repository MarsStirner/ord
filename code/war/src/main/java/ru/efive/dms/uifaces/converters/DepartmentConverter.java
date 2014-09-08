package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.DepartmentDAO;
import ru.efive.sql.entity.user.Department;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

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
        if(value != null && value.trim().length() > 0) {
            SessionManagementBean sessionManagement = fc.getApplication().evaluateExpressionGet(fc, "#{sessionManagement}", SessionManagementBean.class);
            DepartmentDAO service = sessionManagement.getDAO(DepartmentDAO.class, ApplicationHelper.DEPARTMENT_DAO);
            final List<Department> departmentList = service.findByValue(value);
            if(departmentList.isEmpty()){
                return null;
            }
            return departmentList.get(0);
        }
        else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if(o != null) {
            return o.toString();
        }
        return null;
    }
}
