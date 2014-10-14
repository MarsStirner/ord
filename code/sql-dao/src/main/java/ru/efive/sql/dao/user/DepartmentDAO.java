package ru.efive.sql.dao.user;

import ru.efive.sql.dao.DictionaryDAOHibernate;
import ru.entity.model.user.Department;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 19:26 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("departmentDAO")
@ApplicationScoped
public class DepartmentDAO extends DictionaryDAOHibernate<Department> {
    @Override
    public Class<Department> getPersistentClass(){
        return Department.class;
    }
}
