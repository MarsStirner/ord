package ru.efive.dms.uifaces.beans.department;

import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.referenceBook.DepartmentDAOImpl;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.DEPARTMENT_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 12.09.2014, 18:01 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для получения списка подразделений (ApplicationScope) <br>
 */
@Named("departmentList")
@ViewScoped
public class DepartmentListHolderBean extends AbstractDocumentListHolderBean<Department> {

    private String filter;
    private static DepartmentDAOImpl dao;
    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    @PostConstruct
    public void init() {
        dao = indexManagement.getContext().getBean(DEPARTMENT_DAO, DepartmentDAOImpl.class);
    }


    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 25);
    }

    @Override
    public Sorting initSorting() {
        return new Sorting("value", true);
    }


    @Override
    protected int getTotalCount() {
        return (int) dao.countDocument(filter, false);
    }

    @Override
    protected List<Department> loadDocuments() {
        return dao.findDocuments(filter, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
    }
}
