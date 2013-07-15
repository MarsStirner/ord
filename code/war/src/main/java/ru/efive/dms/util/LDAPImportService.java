package ru.efive.dms.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.beans.SessionManagementBean;

public class LDAPImportService {
    private String ldapAddressName;
    private String ldapAddressValue;
    private String loginName;
    private String loginValue;
    private String passwordName;
    private String passwordValue;
    private String baseValue;
    private String firedValue;
    private String filterValue;
    private UserDAOHibernate userDAO;
    private final int pageSize = 100;

    public void setLdapAddressValue(String ldapAddressValue) {
        this.ldapAddressValue = ldapAddressValue;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setLoginValue(String loginValue) {
        this.loginValue = loginValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public void setPasswordName(String passwordName) {
        this.passwordName = passwordName;
    }

    public void setPasswordValue(String passwordValue) {
        this.passwordValue = passwordValue;
    }

    public void setBaseValue(String baseValue) {
        this.baseValue = baseValue;
    }

    public void setLdapAddressName(String ldapAddressName) {
        this.ldapAddressName = ldapAddressName;
    }

    public void run() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SessionManagementBean sessionManagement = (SessionManagementBean) facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{sessionManagement}", SessionManagementBean.class);
        userDAO = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO);
        try {
            importActiveDirectoryUsers();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void importActiveDirectoryUsers() throws Exception {
        LdapContext context = null;
        try {
            // Create ORD users cache
            Map<String, User> localUsersCache = getORDUsers();
            saveNotFiredUsers(getLdapContext(), localUsersCache);
            saveFiredUsers(getLdapContext(), localUsersCache);
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    private void saveNotFiredUsers(LdapContext context, Map<String, User> localUsersCache) throws NamingException, IOException, ParseException {
        byte[] cookie = null;
        do {
            //Search LDAP group where all users
            NamingEnumeration ldapUsers = context.search(baseValue, filterValue, getSearchControls());
            //Save not fired users
            cookie = saveLdapUsers(ldapUsers, context, localUsersCache, cookie, false);
        } while ((cookie != null) && (cookie.length != 0));
    }

    private void saveFiredUsers(LdapContext context, Map<String, User> localUsersCache) throws NamingException, IOException, ParseException {
        byte[] cookie = null;
        do {
            //Search LDAP group where all users
            NamingEnumeration firedUsers = context.search(firedValue, filterValue, getSearchControls());
            //Save fired users
            cookie = saveLdapUsers(firedUsers, context, localUsersCache, cookie, true);
        } while ((cookie != null) && (cookie.length != 0));
    }

    private void saveLdapUserToORD(LDAPUser ldapUser, Map<String, User> localUsersCache) throws NamingException, ParseException {
        User user;
        // User exist in db
        if (((localUsersCache.containsKey(ldapUser.getId())) && ((user = localUsersCache.get(ldapUser.getId())) != null))
                || localUsersCache.containsKey(ldapUser.getFullName()) && ((user = localUsersCache.get(ldapUser.getFullName())) != null)
                || (localUsersCache.containsKey(ldapUser.getLogin())) && ((user = localUsersCache.get(ldapUser.getLogin())) != null)) {

            if (((user.getLastModified() != null) && (user.getLastModified().before(ldapUser.getLastModified())))
                    || (user.getLastModified() == null)) {
                userDAO.save(changeUserSettings(user, ldapUser));
            }
            // Create user
        } else {
            user = new User();
            user.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
            userDAO.save(changeUserSettings(user, ldapUser));
        }
    }

    private Map<String, User> getORDUsers() {
        List<User> localUsers = userDAO.findAllUsers(false, false);
        Map<String, User> localUsersCache = new HashMap<String, User>();
        for (User user : localUsers) {
            // by default user will deleted
            user.setDeleted(true);
            if (user.getGUID() != null) {
                localUsersCache.put(user.getGUID(), user);
            }
            if (user.getUNID() != null) {
                localUsersCache.put(user.getUNID(), user);
            }
            if (user.getLogin() != null) {
                localUsersCache.put(user.getLogin(), user);
            }

            String userFullName =
                    (user.getLastName() != null && !user.getLastName().equals("") ? (user.getLastName().trim() + "_") : "") +
                            (user.getFirstName() != null && !user.getFirstName().equals("") ? (user.getFirstName().trim() + "_") : "") +
                            (user.getMiddleName() != null && !user.getMiddleName().equals("") ? user.getMiddleName().trim() : "");
            if (userFullName != null && !userFullName.equals("")) {
                localUsersCache.put(userFullName.toLowerCase(), user);
            }
        }
        return localUsersCache;
    }

    private LdapContext getLdapContext() throws NamingException, IOException {
        Properties properties = new Properties();
        properties.put(ldapAddressName, ldapAddressValue);
        properties.put(Context.SECURITY_AUTHENTICATION, "Simple");
        properties.put(loginName, loginValue);
        properties.put(passwordName, passwordValue);
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put("java.naming.ldap.attributes.binary", "objectGUID");
        LdapContext context = new InitialLdapContext(properties, null);
        Control[] paginator = new Control[]{new PagedResultsControl(pageSize, Control.CRITICAL)};
        context.setRequestControls(paginator);
        return context;
    }

    private SearchControls getSearchControls() {
        SearchControls controls = new SearchControls();
        controls.setReturningAttributes(new String[]{"distinguishedName",
                LDAPUserAttribute.UNID_ATTR_VALUE.getName(), LDAPUserAttribute.LAST_MODIFIED_ATTR_VALUE.getName(), LDAPUserAttribute.LOGIN_ATTR_VALUE.getName(),
                LDAPUserAttribute.LAST_NAME_ATTR_VALUE.getName(), LDAPUserAttribute.FIRST_NAME_ATTR_VALUE.getName(),
                LDAPUserAttribute.MIDDLE_NAME_ATTR_VALUE.getName(), LDAPUserAttribute.MAIL_ATTR_VALUE.getName(), LDAPUserAttribute.PHONE_ATTR_VALUE.getName(),
                LDAPUserAttribute.MOBILE_PHONE_ATTR_VALUE.getName(), LDAPUserAttribute.JOB_POSITION_ATTR_VALUE.getName(),
                LDAPUserAttribute.JOB_DEPARTMENT_ATTR_VALUE.getName(), LDAPUserAttribute.EMPLOYER_ID_ATTR_VALUE.getName()});
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        return controls;
    }

    private byte[] saveLdapUsers(NamingEnumeration ldapUsers, LdapContext context,
                                 Map<String, User> localUsersCache, byte[] cookie, boolean  fired)
            throws NamingException, IOException, ParseException {
        //Save changes from LDAP in DB
        while (ldapUsers != null && ldapUsers.hasMore()) {
            SearchResult userADEntry = (SearchResult) ldapUsers.next();
            Attributes userAttributes = userADEntry.getAttributes();

            saveLdapUserToORD(new LDAPUser(userAttributes, fired), localUsersCache);

            // examine the response controls
            cookie = parseControls(context.getResponseControls());

            // pass the cookie back to the server for the next page
            context.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, Control.CRITICAL)});
        }
        return cookie;
    }


    private User changeUserSettings(User user, LDAPUser ldapUser) throws NamingException, ParseException {
        user.setUNID(ldapUser.getId());
        user.setGUID(ldapUser.getGuid());
        user.setLastModified(ldapUser.getLastModified());
        user.setEmail(ldapUser.getMail());
        user.setDeleted(false);
        user.setFired(false);
        user.setLogin(ldapUser.getLogin());
        user.setFirstName(ldapUser.getFirstName());
        user.setLastName(ldapUser.getLastName());
        user.setMiddleName(ldapUser.getSecondName());
        user.setWorkPhone(ldapUser.getPhone());
        user.setMobilePhone(ldapUser.getMobile());
        user.setJobPosition(ldapUser.getJobPosition());
        user.setJobDepartment(ldapUser.getJobDepartment());
        user.setLastModified(ldapUser.getLastModified());
        return user;
    }

    static byte[] parseControls(Control[] controls) throws NamingException {
        byte[] cookie = null;
        if (controls != null) {
            for (int i = 0; i < controls.length; i++) {
                if (controls[i] instanceof PagedResultsResponseControl) {
                    PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                    cookie = prrc.getCookie();
                }
            }
        }
        return (cookie == null) ? new byte[0] : cookie;
    }

    public void setFiredValue(String firedValue) {
        this.firedValue = firedValue;
    }
}