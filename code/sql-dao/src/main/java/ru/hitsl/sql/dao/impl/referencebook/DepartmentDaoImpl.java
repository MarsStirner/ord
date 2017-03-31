package ru.hitsl.sql.dao.impl.referencebook;

import org.springframework.stereotype.Repository;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.impl.mapped.ReferenceBookDaoImpl;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 19:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Repository("departmentDao")
public class DepartmentDaoImpl extends ReferenceBookDaoImpl<Department> implements DepartmentDao{
    @Override
    public Class<Department> getEntityClass() {
        return Department.class;
    }
}
