package ru.efive.sql.dao.user;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.efive.sql.entity.user.Department;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 19:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class DepartmentDAO extends DictionaryDAOHibernate<Department> {
    @Override
    public Class<Department> getPersistentClass(){
        return Department.class;
    }
}
