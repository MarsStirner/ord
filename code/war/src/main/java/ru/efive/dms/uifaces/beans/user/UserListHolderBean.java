package ru.efive.dms.uifaces.beans.user;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.dms.util.LDAPImportService;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("userList")
@SessionScoped
public class UserListHolderBean extends AbstractDocumentListHolderBean<User> {
    private static final long serialVersionUID = 8282506863686518183L;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    private String filter;

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("lastName", true);
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    protected int getTotalCount() {
        return new Long(sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).countUsers(filter, false, false)).intValue();
    }

    @Override
    protected List<User> loadDocuments() {
        return sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO)
                .findUsers(filter, false, false,
                        getPagination().getOffset(), getPagination().getPageSize(),
                        getSorting().getColumnId(), getSorting().isAsc()
                );
    }

    public void importLDAPUsers() {
        ClassPathXmlApplicationContext context = sessionManagement.getIndexManagement().getContext();
        LDAPImportService service = (LDAPImportService) context.getBean("ldapImportService");
        service.run();
    }
}