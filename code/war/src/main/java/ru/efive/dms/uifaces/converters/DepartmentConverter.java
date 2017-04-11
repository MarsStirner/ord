package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import ru.efive.dms.uifaces.beans.annotations.FacesConverter;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 08.09.2014, 15:59 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverter("departmentConverter")
public class DepartmentConverter extends AbstractReferenceBookConverter<Department> {

    @Autowired
    public DepartmentConverter(@Qualifier("departmentDao") final DepartmentDao departmentDao) {
        super(departmentDao);
    }
}
