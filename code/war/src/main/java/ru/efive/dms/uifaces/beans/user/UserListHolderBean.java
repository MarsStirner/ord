package ru.efive.dms.uifaces.beans.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationDAONames;
import ru.efive.dms.util.ldap.LDAPImportService;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.user.User;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@ManagedBean(name = "userList")
@ViewScoped
public class UserListHolderBean extends AbstractDocumentListHolderBean<User> {

    private static final Logger logger = LoggerFactory.getLogger("USERLIST");

    private static final long serialVersionUID = 8282506863686518183L;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;
    private String filter;
    /**
     * Флаг для определения: показывать только уволенных или наоборот
     */
    private boolean showFired = false;

    public boolean getShowFired(){
        return showFired;
    }

    public void changeShowFiredFlag(){
        showFired = !showFired;
        refresh();
    }

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
        if(!showFired) {
            return new Long(sessionManagement.getDAO(UserDAOHibernate.class, ApplicationDAONames.USER_DAO).countUsers(filter, false, false)).intValue();
        } else {
            return new Long(sessionManagement.getDAO(UserDAOHibernate.class, ApplicationDAONames.USER_DAO).countFiredUsers(filter, false)).intValue();
        }
    }

    @Override
    protected List<User> loadDocuments() {
        if(!showFired) {
            return sessionManagement.getDAO(UserDAOHibernate.class, ApplicationDAONames.USER_DAO)
                    .findUsers(filter, false, false,
                            getPagination().getOffset(), getPagination().getPageSize(),
                            getSorting().getColumnId(), getSorting().isAsc()
                    );
        } else {
            return sessionManagement.getDAO(UserDAOHibernate.class, ApplicationDAONames.USER_DAO)
                    .findFiredUsers(filter, false,
                            getPagination().getOffset(), getPagination().getPageSize(),
                            getSorting().getColumnId(), getSorting().isAsc()
                    );
        }
    }

    public void importLDAPUsers() {
        ApplicationContext context = sessionManagement.getIndexManagement().getContext();
        LDAPImportService service = (LDAPImportService) context.getBean("ldapImportService");
        service.run();
    }

    //TODO выпилить к черту из @ConversationScoped бина
    public String getUserFullNameById(int id) {
        try {
            User user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationDAONames.USER_DAO).get(id);
            if (user != null) {
                return user.getDescription();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}