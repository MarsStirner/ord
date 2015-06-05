package ru.efive.dms.uifaces.beans.user;

import org.springframework.context.ApplicationContext;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ldap.LDAPImportService;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.user.UserDAOHibernate;
import ru.hitsl.sql.dao.util.ApplicationDAONames;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("userList")
@ViewScoped
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
            return new Long(sessionManagement.getDAO(UserDAOHibernate.class, ApplicationDAONames.USER_DAO).countUsers(filter, false, false)).intValue();

    }

    @Override
    protected List<User> loadDocuments() {
            return sessionManagement.getDAO(UserDAOHibernate.class, ApplicationDAONames.USER_DAO)
                    .findUsers(filter, false, false,
                            getPagination().getOffset(), getPagination().getPageSize(),
                            getSorting().getColumnId(), getSorting().isAsc()
                    );
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