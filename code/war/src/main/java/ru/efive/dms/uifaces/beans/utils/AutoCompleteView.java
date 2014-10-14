package ru.efive.dms.uifaces.beans.utils;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.sql.dao.user.DepartmentDAO;
import ru.efive.sql.dao.user.PositionDAO;
import ru.entity.model.user.Department;
import ru.entity.model.user.Position;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.DEPARTMENT_DAO;
import static ru.efive.dms.util.ApplicationDAONames.POSITION_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 08.09.2014, 15:44 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("autoCompleteView")
@ApplicationScoped
public class AutoCompleteView {
    private final static int MIN_SEARCH_STRING_LENGTH = 2;
    private final static int MAX_SEARCH_RESULTS = 10;

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement;

    private DepartmentDAO departmentDAO;
    private List<Department> allDepartments;
    private PositionDAO positionDAO;
    private List<Position> allPositions;


    @PostConstruct
    public void init() {
        departmentDAO = sessionManagement.getDAO(DepartmentDAO.class, DEPARTMENT_DAO);
        positionDAO = sessionManagement.getDAO(PositionDAO.class, POSITION_DAO);
    }

    public List<Department> completeDepartment(final String query) {
        if (allDepartments == null) {
            allDepartments = departmentDAO.findDocuments();
        }
        final List<Department> result = new ArrayList<Department>(MAX_SEARCH_RESULTS);
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
            allPositions = positionDAO.findDocuments();
        }
        final List<Position> result = new ArrayList<Position>(MAX_SEARCH_RESULTS);
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
}
