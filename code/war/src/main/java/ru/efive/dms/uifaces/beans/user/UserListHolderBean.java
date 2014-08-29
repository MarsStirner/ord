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
        toIndex = (getHashUsers().size() < fromIndex + toIndex) ? this.getHashUsers().size() : fromIndex + toIndex;
        List<User> result = new ArrayList<User>(getHashUsers().subList(fromIndex, toIndex));
        return result;
    }

    protected List<User> getHashUsers() {
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                hashUsers = new ArrayList<User>(new HashSet<User>(sessionManagement.getDAO(UserDAOHibernate.class,
                        ApplicationHelper.USER_DAO).findUsers(filter, false, false)));

                Collections.sort(hashUsers, new Comparator<User>() {
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
        return new Sorting("lastName,firstName,middleName", false);
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