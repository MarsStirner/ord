package ru.efive.dms.uifaces.beans.user;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.util.ldap.LDAPImportService;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;

import org.springframework.stereotype.Controller;
import java.util.List;

@Controller("userList")
@SpringScopeView
public class UserListHolderBean extends AbstractDocumentListHolderBean<User> {

    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;
    @Autowired
    @Qualifier("ldapImportService")
    private LDAPImportService ldapImportService;


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
        return new Long(userDao.countItems(filter, false, false)).intValue();

    }

    @Override
    protected List<User> loadDocuments() {
        return userDao.getItems(filter, false, false, getPagination().getOffset(), getPagination().getPageSize(),
                getSorting().getColumnId(), getSorting().isAsc()
        );
    }

    public void importLDAPUsers() throws Exception {
        ldapImportService.run();
    }

    //TODO выпилить к черту из @ConversationScoped бина
    public String getUserFullNameById(int id) {
        try {
            User user = userDao.get(id);
            if (user != null) {
                return user.getDescription();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}