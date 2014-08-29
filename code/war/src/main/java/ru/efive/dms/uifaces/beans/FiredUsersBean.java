package ru.efive.dms.uifaces.beans;

import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Named("firedUsers")
@SessionScoped
public class FiredUsersBean extends AbstractDocumentListHolderBean<User> {

    private static final long serialVersionUID = -2455312448234911746L;

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).countFiredUsers(filter, false)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<User> loadDocuments() {
        List<User> result = new ArrayList<User>();
        try {
            result = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findFiredUsers(filter, false,
                    getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());

            Collections.sort(result, new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    int result = 0;
                    String colId = getSorting().getColumnId();

                    if(colId.equalsIgnoreCase("description")) {
                        result = ApplicationHelper.getNotNull(o1.getFullName()).compareTo(ApplicationHelper.getNotNull(o2.getFullName()));
                    }

                    if(getSorting().isAsc()) {
                        result *= -1;
                    }

                    return result;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<User> getDocuments() {
        return super.getDocuments();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }


    private String filter;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;


}