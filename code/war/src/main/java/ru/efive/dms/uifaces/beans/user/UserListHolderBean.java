package ru.efive.dms.uifaces.beans.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.dms.util.LDAPImportService;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("userList")
@SessionScoped
public class UserListHolderBean extends AbstractDocumentListHolderBean<User> {
    private static final long serialVersionUID = 1023130636261147049L;
    private String filter;
    private List<User> hashUsers;
    private boolean needRefresh = true;

    @Inject
    @Named("sessionManagement")
    SessionManagementBean sessionManagement = new SessionManagementBean();
    private static final String beanName = "userList";

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        needRefresh = true;
        this.filter = filter;
    }

    @Override
    public void sort(String columnName) {
        needRefresh = true;
        super.sort(columnName);
    }

    protected List<User> getHashUsers(int fromIndex, int toIndex) {
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                hashUsers = new ArrayList<User>(new HashSet<User>(sessionManagement.getDAO(UserDAOHibernate.class,
                        ApplicationHelper.USER_DAO).findAllUsers(false, false)));

                Collections.sort(this.hashUsers, new Comparator<User>() {
                    public int compare(User u1, User u2) {
                        return u1.getDescription().compareTo(u2.getDescription());
                    }
                });
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        toIndex = (hashUsers.size() < fromIndex + toIndex) ? this.hashUsers.size() : fromIndex + toIndex;
        List<User> result = new ArrayList<User>(hashUsers.subList(fromIndex, toIndex));
        return result;
    }

    protected List<User> getHashUsers() {
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                hashUsers = new ArrayList<User>(new HashSet<User>(sessionManagement.getDAO(UserDAOHibernate.class,
                        ApplicationHelper.USER_DAO).findUsers(filter, false, false)));

                Collections.sort(this.hashUsers, new Comparator<User>() {
                    public int compare(User u1, User u2) {
                        return u1.getDescription().compareTo(u2.getDescription());
                    }
                });
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hashUsers;
    }

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }


    @Override
    protected Sorting initSorting() {
        return new Sorting("lastName,firstName,middleName", true);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(this.getHashUsers().size()).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<User> loadDocuments() {
        List<User> result = new ArrayList<User>();
        try {
            result = this.getHashUsers(getPagination().getOffset(), getPagination().getPageSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getUserDescription(int id) {
        String description = "";
        try {
            User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).get(id);
            if (user != null) {
                description = user.getDescriptionShort();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return description;
    }

    public void importLDAPUsers() {
        ClassPathXmlApplicationContext context = sessionManagement.getIndexManagement().getContext();
        LDAPImportService service = (LDAPImportService) context.getBean("ldapImportService");
        service.run();
    }
}