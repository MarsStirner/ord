package ru.efive.dms.util;

import java.text.ParseException;
import java.util.*;
import javax.faces.context.FacesContext;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import ru.efive.sql.dao.user.UserAccessLevelDAO;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.sql.entity.user.UserAccessLevel;

public class LDAPImportService {
    //Именованный логгер (LDAP)
    private static final Logger LOGGER = Logger.getLogger("LDAP");
    private static final SearchControls SEARCH_CONTROLS = new SearchControls();
    //Пароль по-умолчанию для новых пользователей
    private static final String DEFAULT_PASSWORD = "12345";
    //DAO для работы с локальными пользователями
    private static UserDAOHibernate userDAO;
    //Присваемый по-умолчанию уровень допуска
    private static UserAccessLevel USER_ACCESS_LEVEL_FOR_PUBLIC_USE;


    static {
        LOGGER.setLevel(Level.DEBUG);
        SEARCH_CONTROLS.setReturningAttributes(
                new String[]{
                        //DN
                        LDAPUserAttribute.DN_ATTR.getName(),
                        //GUID
                        LDAPUserAttribute.UNID_ATTR_VALUE.getName(),
                        LDAPUserAttribute.LAST_MODIFIED_ATTR_VALUE.getName(),
                        LDAPUserAttribute.LOGIN_ATTR_VALUE.getName(),
                        LDAPUserAttribute.LAST_NAME_ATTR_VALUE.getName(),
                        LDAPUserAttribute.FIRST_NAME_ATTR_VALUE.getName(),
                        // LDAPUserAttribute.MIDDLE_NAME_ATTR_VALUE.getName(),
                        LDAPUserAttribute.MAIL_ATTR_VALUE.getName(),
                        LDAPUserAttribute.PHONE_ATTR_VALUE.getName(),
                        LDAPUserAttribute.MOBILE_PHONE_ATTR_VALUE.getName(),
                        LDAPUserAttribute.JOB_POSITION_ATTR_VALUE.getName(),
                        LDAPUserAttribute.JOB_DEPARTMENT_ATTR_VALUE.getName(),
                        //LDAPUserAttribute.EMPLOYER_ID_ATTR_VALUE.getName(),
                        LDAPUserAttribute.FIO_ATTR_VALUE.getName()
                }
        );
        SEARCH_CONTROLS.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    private String ldapAddressValue;
    private String loginValue;
    private String passwordValue;
    private String baseValue;
    private String filterValue;
    private final int PAGE_SIZE = 100;
    private List<String> skipBaseList;

    public void setLdapAddressValue(String ldapAddressValue) {
        this.ldapAddressValue = ldapAddressValue;
    }

    public void setLoginValue(String loginValue) {
        this.loginValue = loginValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public void setPasswordValue(String passwordValue) {
        this.passwordValue = passwordValue;
    }

    public void setBaseValue(String baseValue) {
        this.baseValue = baseValue;
    }

    public void setSkipBaseValue(String skipBaseValue) {
        skipBaseList = Arrays.asList(skipBaseValue.toLowerCase().split(","));
        for(String current : skipBaseList){
            current = current.concat(",");
        }
    }

    public void run() {
        LOGGER.info("START");
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final SessionManagementBean sessionManagement = facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{sessionManagement}", SessionManagementBean.class);
        userDAO = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO);
        LOGGER.debug("userDao: " + userDAO.toString());
        final List<UserAccessLevel> userAccessLevels = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findDocuments();
        for (UserAccessLevel current : userAccessLevels) {
            if (current.getLevel() == 1) {
                USER_ACCESS_LEVEL_FOR_PUBLIC_USE = current;
                break;
            }
        }
        try {
            importActiveDirectoryUsers();
        } catch (Exception e) {
            LOGGER.error("LDAP Error:", e);
        }
        LOGGER.info("END");
    }

    /**
     * Получение ldap контекста
     *
     * @return контекст
     */
    private LdapContext getLdapContext() {
        Properties properties = new Properties();
        properties.put(Context.PROVIDER_URL, ldapAddressValue);
        properties.put(Context.SECURITY_AUTHENTICATION, "Simple");
        properties.put(Context.SECURITY_PRINCIPAL, loginValue);
        properties.put(Context.SECURITY_CREDENTIALS, passwordValue);
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put("java.naming.ldap.attributes.binary", "objectGUID");
        // the following is helpful in debugging errors
        //properties.put("com.sun.jndi.ldap.trace.ber", System.err);
        try {
            return new InitialLdapContext(properties, null);
        } catch (CommunicationException ce) {
            LOGGER.error("Couldn't connect to LDAP server:", ce);
            return null;
        } catch (NamingException ne) {
            LOGGER.error("NamingException:", ne);
            return null;
        }
    }


    /**
     * Импорт пользователей из LDAP
     *
     * @throws Exception
     */
    private void importActiveDirectoryUsers() throws Exception {
        final LdapContext context = getLdapContext();
        if (context == null) {
            return;
        }
        // Create ORD users cache
        LOGGER.debug("Start caching local users");
        final List<User> allLocalUsers = userDAO.findAllUsers();
        LOGGER.debug("Cached " + allLocalUsers.size() + " users");
        // Activate paged results
        byte[] cookie = null;
        context.setRequestControls(new Control[]{new PagedResultsControl(PAGE_SIZE, Control.NONCRITICAL)});
        int i = 0;
        do {
            /* perform the search */
            LOGGER.info("Select ldap users[" + baseValue + '\\' + filterValue + "]");
            final NamingEnumeration results = context.search(baseValue, filterValue, SEARCH_CONTROLS);
            /* for each entry print out name + all attrs and values */
            while (results != null && results.hasMore()) {
                LDAPUser currentLdapUser = convertToLdapUser((SearchResult) results.next(), i++);
                if (currentLdapUser != null) {
                    //Поиск совпадений  по GUID
                    boolean foundedByGuid = false;
                    for (User currentLocalUser : allLocalUsers) {
                        if (currentLdapUser.getGuid().equalsIgnoreCase(currentLocalUser.getGUID())) {
                            foundedByGuid = true;
                            LOGGER.debug("Founded by GUID. localID=" + currentLocalUser.getId());
                            synchronizeUser(currentLdapUser, currentLocalUser);
                            break;
                        }
                    }
                    //Для тех, кому не нашли соответсвия по GUID
                    if (!foundedByGuid) {
                        //Выполняем поиск по ФИО
                        final String curLDAPLast = currentLdapUser.getLastName();
                        final String curLDAPFirst = currentLdapUser.getFirstName();
                        final String curLDAPPatr= currentLdapUser.getPatrName();

                        final List<User> overlappedByFioUserList = new ArrayList<User>(4);
                        for (User curLocalUser : allLocalUsers) {
                            //Совпадает ФИО
                            if(
                                //[Обе Фамилии null]
                                // ИЛИ
                                // [Обе не null И равны без пробелов]
                                (
                                        (curLDAPLast == null && curLocalUser.getLastName() == null)
                                        ||
                                        (curLDAPLast != null && curLocalUser.getLastName() != null && curLDAPLast.equalsIgnoreCase(curLocalUser.getLastName().trim()))
                                )
                                &&
                                (
                                        (curLDAPFirst == null && curLocalUser.getFirstName() == null)
                                        ||
                                        (curLDAPFirst != null && curLocalUser.getFirstName() != null && curLDAPFirst.equalsIgnoreCase(curLocalUser.getFirstName().trim()))

                                )
                                &&
                                (
                                        (curLDAPPatr == null && curLocalUser.getMiddleName() == null)
                                        ||
                                        (curLDAPPatr != null && curLocalUser.getMiddleName() != null && curLDAPPatr.equalsIgnoreCase(curLocalUser.getMiddleName().trim()))

                                )
                            )
                            {
                                LOGGER.debug("Founded by FIO. localID=" + curLocalUser.getId());
                                overlappedByFioUserList.add(curLocalUser);
                            }
                        }
                        switch (overlappedByFioUserList.size()) {
                            //Не нашли пользователя ни по ФИО ни по GUID
                            case 0: {
                                //Не было найдено ни одного совпадения
                                createNewUser(currentLdapUser);
                                break;
                            }
                            //Нашли единственного пользователя по ФИО
                            case 1: {
                                final User uniqueFioUser = overlappedByFioUserList.iterator().next();
                                if (uniqueFioUser.getGUID() != null) {
                                    //Но у него заполнен GUID и не совпадает с нашим (иначе мы бы сюда не дошли)
                                    LOGGER.error("Founded by FIO user has different GUID:" + uniqueFioUser.getGUID());
                                    break;
                                } else {
                                    //Похоже наш кадр, только GUID не заполнен
                                    synchronizeUser(currentLdapUser, uniqueFioUser);
                                    break;
                                }
                            }
                            //Больше одного совпадения по ФИО
                            default: {
                                LOGGER.error("Too many users founded by FIO, skip this LDAP user");
                                break;
                            }
                        }//END OF SWITCH
                    }
                }//END OF currentLDAP NOT NULL
            }
            // Examine the paged results control response
            Control[] controls = context.getResponseControls();
            if (controls != null) {
                for (Control control : controls) {
                    if (control instanceof PagedResultsResponseControl) {
                        final PagedResultsResponseControl prrc = (PagedResultsResponseControl) control;
                        LOGGER.info("***************** END-OF-PAGE *****************");
                        cookie = prrc.getCookie();
                    }
                }
            } else {
                LOGGER.info("No controls were sent from the server");
            }
            // Re-activate paged results
            context.setRequestControls(new Control[]{new PagedResultsControl(PAGE_SIZE, cookie, Control.CRITICAL)});
        } while (cookie != null);
        context.close();
        LOGGER.debug("PROCESSED " + i + " LDAP USERS");
    }

    private LDAPUser convertToLdapUser(final SearchResult entry, final int currentNumber) {
        LOGGER.info("#" + currentNumber + " DN=\'" + entry.getName() + '\'');
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(printAttributesToString(entry));
        }
        //Отсекаем лишние
        if (skipBaseList != null && !skipBaseList.isEmpty()) {
            final String entryName = entry.getName().toLowerCase();
            for (String currentSkipBase : skipBaseList) {
                if (entryName.contains(currentSkipBase)) {
                    LOGGER.info(currentNumber + " SKIPPED. Because contains part:" + currentSkipBase);
                    return null;
                }
            }
        }

        try {
            //Если есть "ou=fired" значит пользователь уволен
            return new LDAPUser(entry.getAttributes(), entry.getName().toLowerCase().contains("ou=fired,"));
        } catch (ParseException e) {
            LOGGER.error(currentNumber + " ParseException: " + e.getMessage());
        } catch (NamingException e) {
            LOGGER.error(currentNumber + " NamingException: " + e.getMessage());
        }
        return null;
    }

    private void createNewUser(LDAPUser ldapUser) {
        final User user = new User();
        user.setCreated(Calendar.getInstance(ApplicationHelper.getLocale()).getTime());
        user.setDeleted(false);
        //При создании указывать пароль по умолчанию "12345")
        user.setPassword(DEFAULT_PASSWORD);
        user.setLogin(ldapUser.getLogin());
        //Максимальный и все остальные уровни допуска выставить как "Для общего пользования"
        user.setMaxUserAccessLevel(USER_ACCESS_LEVEL_FOR_PUBLIC_USE);
        user.setCurrentUserAccessLevel(USER_ACCESS_LEVEL_FOR_PUBLIC_USE);
        user.setGUID(ldapUser.getGuid());
        user.setLastModified(ldapUser.getLastModified());
        user.setEmail(ldapUser.getMail());
        user.setFired(ldapUser.isFired());
        user.setFirstName(ldapUser.getFirstName());
        user.setLastName(ldapUser.getLastName());
        user.setMiddleName(ldapUser.getPatrName());
        user.setWorkPhone(ldapUser.getPhone());
        user.setMobilePhone(ldapUser.getMobile());
        user.setJobPosition(ldapUser.getJobPosition());
        user.setJobDepartment(ldapUser.getJobDepartment());
        user.setLastModified(ldapUser.getLastModified());
        try {
            userDAO.save(user);
        } catch (Exception e) {
            LOGGER.error("Cannot INSERT new ROW : " + e.getCause().getMessage());
        }
        LOGGER.debug("Created");
    }

    /**
     * @param ldapUser  пользователь из ldap
     * @param localUser локальный пользователь
     */
    private void synchronizeUser(LDAPUser ldapUser, User localUser) {
        LOGGER.debug("Synchronize with localID=" + localUser.getId());
        if (localUser.getLastModified() != null && localUser.getLastModified().getTime() == ldapUser.getLastModified().getTime() && localUser.getGUID() != null) {
            LOGGER.debug("Already Up to Date");
        } else {
            localUser.setGUID(ldapUser.getGuid());
            localUser.setLastModified(ldapUser.getLastModified());
            localUser.setEmail(ldapUser.getMail());
            localUser.setFired(ldapUser.isFired());
            localUser.setLogin(ldapUser.getLogin());
            localUser.setFirstName(ldapUser.getFirstName());
            localUser.setLastName(ldapUser.getLastName());
            localUser.setMiddleName(ldapUser.getPatrName());
            localUser.setWorkPhone(ldapUser.getPhone());
            localUser.setMobilePhone(ldapUser.getMobile());
            localUser.setJobPosition(ldapUser.getJobPosition());
            localUser.setJobDepartment(ldapUser.getJobDepartment());
            localUser.setLastModified(ldapUser.getLastModified());
            userDAO.save(localUser);
            LOGGER.debug("Updated");
        }
    }

    /**
     * Вывод всех аттрибутов найденной сущности из LDAP в строку
     *
     * @param entry найденная сущность
     * @return все аттрибуты (NAME=VALUE) в виде строки с разделителем (\n)
     */
    private String printAttributesToString(final SearchResult entry) {
        if (entry.getAttributes() != null) {
            try {
                final NamingEnumeration<? extends Attribute> allAtributes = entry.getAttributes().getAll();
                final StringBuilder sb = new StringBuilder();
                while (allAtributes != null && allAtributes.hasMore()) {
                    final Attribute attribute = allAtributes.next();
                    sb.append(attribute.getID()).append('=').append(attribute.get());
                    if (allAtributes.hasMore()) {
                        sb.append('\n');
                    }
                }
                return sb.toString();
            } catch (NamingException e) {
                return "";
            }
        }
        return "";
    }
}