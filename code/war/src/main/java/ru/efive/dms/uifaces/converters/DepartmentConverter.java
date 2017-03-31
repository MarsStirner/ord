package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;

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

    @Autowired
    @Qualifier("departmentDao")
    private DepartmentDao departmentDao;

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        if (value != null && value.trim().length() > 0) {
            final List<Department> departmentList = departmentDao.getByValue(value);
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
