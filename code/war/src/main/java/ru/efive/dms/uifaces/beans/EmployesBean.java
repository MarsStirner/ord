package ru.efive.dms.uifaces.beans;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.dms.util.LDAPImportService;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("employes")
@SessionScoped
public class EmployesBean extends AbstractDocumentListHolderBean<User> {
    private static final long serialVersionUID = 8282506863686518183L;
    private static final String beanName = "employes";

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    private String filter;
    private List<User> employes;

    private boolean needRefresh = true;


    protected List<User> getEmployes(int fromIndex, int toIndex) {
        employes = getEmployes();
        toIndex = (employes.size() < fromIndex + toIndex) ? employes.size() : fromIndex + toIndex;
        List<User> result = new ArrayList<User>(employes.subList(fromIndex, toIndex));
        return result;
    }

    public List<User> getEmployesPaged() {
        if(getPagination() == null) {
            super.reset();
        }
        return this.getEmployes(getPagination().getOffset(), getPagination().getPageSize());
    }

    public List<User> getEmployes() {
        if (needRefresh) {
            sessionManagement.registrateBeanName(beanName);
            try {
                employes = new ArrayList<User>(new HashSet<User>(sessionManagement.getDAO(UserDAOHibernate.class,
                        ApplicationHelper.USER_DAO).findUsers(filter, false, false)));

                Collections.sort(this.employes, new Comparator<User>() {
                    public int compare(User u1, User u2) {
                        return u1.getDescriptionShort().compareTo(u2.getDescriptionShort());
                    }
                });

                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return employes;
    }

    @Override
    public void sort(String columnName) {
        needRefresh = true;
        super.sort(columnName);
    }


    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("description", false);
    }

    @Override
    public List<User> getDocuments() {
        return super.getDocuments();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.needRefresh = true;
        this.filter = filter;
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(this.getEmployes().size()).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<User> loadDocuments() {
        List<User> result = new ArrayList<User>();
        try {
            result = this.getEmployes(getPagination().getOffset(), getPagination().getPageSize());
            Collections.sort(result, new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    int result = 0;
                    String colId = getSorting().getColumnId();

                    if(colId.equalsIgnoreCase("description")) {
                        result = ApplicationHelper.getNotNull(o1.getDescription()).compareTo(ApplicationHelper.getNotNull(o2.getDescription()));
                    } else if(colId.equalsIgnoreCase("jobDepartment")) {
                        result = ApplicationHelper.getNotNull(o1.getJobDepartment()).compareTo(ApplicationHelper.getNotNull(o2.getJobDepartment()));
                    } else  if(colId.equalsIgnoreCase("jobPosition")) {
                        result = ApplicationHelper.getNotNull(o1.getJobPosition()).compareTo(ApplicationHelper.getNotNull(o2.getJobPosition()));
                    } else if(colId.equalsIgnoreCase("email")) {
                        result = ApplicationHelper.getNotNull(o1.getEmail()).compareTo(ApplicationHelper.getNotNull(o2.getEmail()));
                    } else if(colId.equalsIgnoreCase("workPhone")) {
                        String s1 = ApplicationHelper.getNotNull(o1.getInternalNumber()).isEmpty() ? o1.getWorkPhone(): ApplicationHelper.getNotNull(o1.getWorkPhone()).concat(" (внутр. ").concat(ApplicationHelper.getNotNull(o1.getInternalNumber())).concat(")");
                        String s2 = ApplicationHelper.getNotNull(o2.getInternalNumber()).isEmpty() ? o2.getWorkPhone(): ApplicationHelper.getNotNull(o2.getWorkPhone()).concat(" (внутр. ").concat(ApplicationHelper.getNotNull(o2.getInternalNumber())).concat(")");
                        result = ApplicationHelper.getNotNull(s1).compareTo(ApplicationHelper.getNotNull(s2));
                    } else if(colId.equalsIgnoreCase("mobilePhone")) {
                        result = ApplicationHelper.getNotNull(o1.getMobilePhone()).compareTo(ApplicationHelper.getNotNull(o2.getMobilePhone()));
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

    @Override
    public void refresh() {
        this.needRefresh = true;
        super.refresh();
    }


    public void importLDAPUsers() {
        ClassPathXmlApplicationContext context = sessionManagement.getIndexManagement().getContext();
        LDAPImportService service = (LDAPImportService) context.getBean("ldapImportService");
        service.run();
    }
}