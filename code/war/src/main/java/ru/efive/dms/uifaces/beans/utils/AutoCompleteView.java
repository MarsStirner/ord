package ru.efive.dms.uifaces.beans.utils;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.referenceBook.Department;
import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.referenceBook.DepartmentDAOImpl;
import ru.hitsl.sql.dao.referenceBook.PositionDAOImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.DEPARTMENT_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.POSITION_DAO;

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

    private DepartmentDAOImpl departmentDAOImpl;
    private List<Department> allDepartments;

    private PositionDAOImpl positionDAO;
    private List<Position> allPositions;


    @PostConstruct
    public void init() {
        departmentDAOImpl = sessionManagement.getDAO(DepartmentDAOImpl.class, DEPARTMENT_DAO);
        positionDAO = sessionManagement.getDAO(PositionDAOImpl.class, POSITION_DAO);
    }

    public List<Department> completeDepartment(final String query) {
        if (allDepartments == null) {
            allDepartments = departmentDAOImpl.findDocuments();
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
