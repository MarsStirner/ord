package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;

import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;

/**
 * Author: Upatov Egor <br>
 * Date: 08.09.2014, 15:59 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverterWithSpringSupport("departmentConverter")
public class DepartmentConverter extends AbstractReferenceBookConverter<Department> {

    @Autowired
    public DepartmentConverter(@Qualifier("departmentDao") final DepartmentDao departmentDao) {
        super(departmentDao);
    }
}
