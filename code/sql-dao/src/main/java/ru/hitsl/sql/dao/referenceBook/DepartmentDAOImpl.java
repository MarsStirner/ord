package ru.hitsl.sql.dao.referenceBook;

import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.DictionaryDAOHibernate;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 19:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class DepartmentDAOImpl extends DictionaryDAOHibernate<Department> {
    @Override
    public Class<Department> getPersistentClass(){
        return Department.class;
    }
}
