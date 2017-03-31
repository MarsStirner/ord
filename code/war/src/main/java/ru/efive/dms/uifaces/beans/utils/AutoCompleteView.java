package ru.efive.dms.uifaces.beans.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.entity.model.referenceBook.Department;
import ru.entity.model.referenceBook.Position;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;
import ru.hitsl.sql.dao.interfaces.referencebook.PositionDao;

import javax.enterprise.context.ApplicationScoped;
import org.springframework.stereotype.Controller;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 08.09.2014, 15:44 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Controller("autoCompleteView")
@ApplicationScoped
public class AutoCompleteView {
    private final static int MIN_SEARCH_STRING_LENGTH = 2;
    private final static int MAX_SEARCH_RESULTS = 10;

    @Autowired
    @Qualifier("departmentDao")
    private DepartmentDao departmentDao;

    @Autowired
    @Qualifier("positionDao")
    private PositionDao positionDao;

    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;


    private List<Department> allDepartments;
    private List<Position> allPositions;


    public List<Department> completeDepartment(final String query) {
        if (allDepartments == null) {
            allDepartments = departmentDao.getItems();
        }
        final List<Department> result = new ArrayList<>(MAX_SEARCH_RESULTS);
        int i = 0;
        if (queryStringValid(query)) {
            final String queryLowerCase = query.toLowerCase();
            for (Department item : allDepartments) {
                if (i >= MAX_SEARCH_RESULTS) {
                    break;
                }
                if (item.getValue().toLowerCase().startsWith(queryLowerCase)) {
                    result.add(item);
                    i++;
                }
            }
        } else {

            for (Department item : allDepartments) {
                if (i >= MAX_SEARCH_RESULTS) {
                    break;
                }
                result.add(item);
                i++;
            }
        }
        return result;
    }

    private boolean queryStringValid(final String query) {
        return query.trim().length() >= MIN_SEARCH_STRING_LENGTH;
    }

    public List<Position> completePosition(final String query) {
        if (allPositions == null) {
            allPositions = positionDao.getItems();
        }
        final List<Position> result = new ArrayList<>(MAX_SEARCH_RESULTS);
        int i = 0;
        if (queryStringValid(query)) {
            final String queryLowerCase = query.toLowerCase();
            for (Position item : allPositions) {
                if (i >= MAX_SEARCH_RESULTS) {
                    break;
                }
                if (item.getValue().toLowerCase().startsWith(queryLowerCase)) {
                    result.add(item);
                    i++;
                }
            }
        } else {
            for (Position item : allPositions) {
                if (i >= MAX_SEARCH_RESULTS) {
                    break;
                }
                result.add(item);
                i++;
            }
        }
        return result;
    }


    public List<User> completeUser(final String query) {
        return userDao.getItems(query, false, false, 0, 100, "lastName", true);
    }
}
