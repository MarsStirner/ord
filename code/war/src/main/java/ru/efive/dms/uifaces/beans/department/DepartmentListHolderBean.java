package ru.efive.dms.uifaces.beans.department;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.referenceBook.Department;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;

import org.springframework.stereotype.Controller;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 12.09.2014, 18:01 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для получения списка подразделений (ApplicationScope) <br>
 */
@Controller("departmentList")
@SpringScopeView
public class DepartmentListHolderBean extends AbstractDocumentListHolderBean<Department> {

    private String filter;

    @Autowired
    @Qualifier("departmentDao")
    private DepartmentDao departmentDao;


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
        return departmentDao.countItems(filter, null, false);
    }

    @Override
    protected List<Department> loadDocuments() {
        return departmentDao.getItems(filter, null, getSorting().getColumnId(), getSorting().isAsc(), getPagination().getOffset(), getPagination().getPageSize(), false);
    }
}
