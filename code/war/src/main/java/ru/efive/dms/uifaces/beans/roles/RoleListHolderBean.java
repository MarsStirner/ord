package ru.efive.dms.uifaces.beans.roles;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationDAONames;
import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.user.Role;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import static ru.efive.dms.util.ApplicationDAONames.ROLE_DAO;

@Named("roleList")
@SessionScoped
public class RoleListHolderBean extends AbstractDocumentListHolderBean<Role> {
    private static final long serialVersionUID = 1023130636261147049L;
    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();

    private String filter;
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
    protected Sorting initSorting() {
        return new Sorting("name", true);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).countRoles()).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<Role> loadDocuments() {
        List<Role> result = new ArrayList<Role>();
        try {
            result = sessionManagement.getDAO(RoleDAOHibernate.class,ROLE_DAO).findRoles(getPagination().getOffset(),
                    getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Role> getAvailableRoles() {
        List<Role> result = new ArrayList<Role>();
        try {
            result = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findRoles(-1, -1, "name", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}