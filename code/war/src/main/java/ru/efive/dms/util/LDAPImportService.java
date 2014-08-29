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
import ru.efive.sql.dao.user.*;
import ru.efive.sql.entity.user.*;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.sql.util.StoredCodes;

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
    //Перечень типов контактной информации
    private static RbContactInfoType EMAIL_CONTACT_TYPE = null;
    private static RbContactInfoType PHONE_CONTACT_TYPE = null;
    private static RbContactInfoType MOBILE_CONTACT_TYPE = null;
    //Перечень должностей
    private List<Position> ALL_POSITIONS;
    //Перечень подразделений
    private List<Department> ALL_DEPARTMENTS;

    private String ldapAddressValue;
    private String loginValue;
    private String passwordValue;
    private String baseValue;
    private String filterValue;
    private final int PAGE_SIZE = 100;
    private List<String> skipBaseList;


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
        if (skipBaseList == null) {
            skipBaseList = new ArrayList<String>();
        }
        for (String current : skipBaseValue.toLowerCase().split(",")) {
            skipBaseList.add(current.concat(","));
        }
    }

    public void run() {
        LOGGER.info("START");
        if (preLoadReferences()) {
            try {
                importActiveDirectoryUsers();
            } catch (Exception e) {
                LOGGER.error("LDAP Error:", e);
            }
        }
        clearPreload();
        LOGGER.info("END");
    }

    private void clearPreload() {
        //Remove links and wait for GC to free memory
        userDAO = null;
        USER_ACCESS_LEVEL_FOR_PUBLIC_USE = null;
        EMAIL_CONTACT_TYPE = null;
        PHONE_CONTACT_TYPE = null;
        MOBILE_CONTACT_TYPE = null;
        ALL_POSITIONS = null;
        ALL_DEPARTMENTS = null;
    }

    private boolean preLoadReferences() {
        final FacesContext facesContext = FacesContext.getCurrentInstance();
        final SessionManagementBean sessionManagement = facesContext.getApplication().evaluateExpressionGet(facesContext,
                "#{sessionManagement}", SessionManagementBean.class);
        if (preloadUserDAO(sessionManagement) == null) {
            LOGGER.error("Upload not started: cause not founded USER_DAO");
            return false;
        }
        if (preLoadUserAccessLevel(sessionManagement) == null) {
            LOGGER.error("Upload not started: cause not founded UserAccessLevel = 1");
            return false;
        }

        final List<RbContactInfoType> contactTypes = sessionManagement.getDictionaryDAO(RbContactTypeDAO.class, ApplicationHelper.RB_CONTACT_TYPE_DAO).findDocuments();
        for (RbContactInfoType contactType : contactTypes) {
            if (StoredCodes.ContactInfoType.EMAIL.equals(contactType.getCode())) {
                EMAIL_CONTACT_TYPE = contactType;
            } else if (StoredCodes.ContactInfoType.PHONE.equals(contactType.getCode())) {
                PHONE_CONTACT_TYPE = contactType;
            } else if (StoredCodes.ContactInfoType.MOBILE_PHONE.equals(contactType.getCode())) {
                MOBILE_CONTACT_TYPE = contactType;
            }
        }

        if (EMAIL_CONTACT_TYPE == null) {
            LOGGER.error("NOT FOUNDED EMAIL_CONTACT_TYPE, UPLOAD NOT Started");
            return false;
        }
        if (PHONE_CONTACT_TYPE == null) {
            LOGGER.error("NOT FOUNDED EMAIL_CONTACT_TYPE, UPLOAD NOT Started");
            return false;
        }
        if (PHONE_CONTACT_TYPE == null) {
            LOGGER.error("NOT FOUNDED EMAIL_CONTACT_TYPE, UPLOAD NOT Started");
            return false;
        }


        ALL_POSITIONS = sessionManagement.getDictionaryDAO(PositionDAO.class, ApplicationHelper.POSITION_DAO).findDocuments();

        ALL_DEPARTMENTS = sessionManagement.getDictionaryDAO(DepartmentDAO.class, ApplicationHelper.DEPARTMENT_DAO).findDocuments();


        return true;
    }

    private UserAccessLevel preLoadUserAccessLevel(SessionManagementBean sessionManagement) {
        final List<UserAccessLevel> userAccessLevels = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findDocuments();
        for (UserAccessLevel current : userAccessLevels) {
            if (current.getLevel() == 1) {
                return USER_ACCESS_LEVEL_FOR_PUBLIC_USE = current;

            }
        }
        return null;
    }

    private UserDAOHibernate preloadUserDAO(SessionManagementBean sessionManagement) {
        userDAO = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO);
        LOGGER.debug("userDao: " + userDAO);
        return userDAO;
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
                        final String curLDAPPatr = currentLdapUser.getPatrName();

                        final List<User> overlappedByFioUserList = new ArrayList<User>(4);
                        for (User curLocalUser : allLocalUsers) {
                            //Совпадает ФИО
                            if (
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
                                    ) {
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
        user.setFired(ldapUser.isFired());
        user.setFirstName(ldapUser.getFirstName());
        user.setLastName(ldapUser.getLastName());
        user.setMiddleName(ldapUser.getPatrName());
        if (ldapUser.getJobPosition() != null && !ldapUser.getJobPosition().isEmpty()) {
            final String requestedPositionName = ldapUser.getJobPosition().trim();
            boolean founded = false;
            for (Position position : ALL_POSITIONS) {
                if (requestedPositionName.equalsIgnoreCase(position.getValue())) {
                    user.setJobPosition(position);
                    founded = true;
                    break;
                }
            }
            if (!founded) {
                LOGGER.error("NOT FOUND JobPosition [" + requestedPositionName + "]");
            }
        }
        if (ldapUser.getJobDepartment() != null && !ldapUser.getJobDepartment().isEmpty()) {
            final String requestedDepartmentName = ldapUser.getJobDepartment().trim();
            boolean founded = false;
            for (Department department : ALL_DEPARTMENTS) {
                if (requestedDepartmentName.equalsIgnoreCase(department.getValue())) {
                    user.setJobDepartment(department);
                    founded = true;
                    break;
                }
            }
            if (!founded) {
                LOGGER.error("NOT FOUND JobDepartment [" + requestedDepartmentName + "]");
            }
        }
        user.setLastModified(ldapUser.getLastModified());
        if (ldapUser.getMail() != null && !ldapUser.getMail().isEmpty()) {
            user.addToContacts(new PersonContact(user, EMAIL_CONTACT_TYPE, ldapUser.getMail()));
        }
        if (ldapUser.getPhone() != null && !ldapUser.getPhone().isEmpty()) {
            user.addToContacts(new PersonContact(user, PHONE_CONTACT_TYPE, ldapUser.getPhone()));
        }
        if (ldapUser.getMobile() != null && !ldapUser.getMobile().isEmpty()) {
            user.addToContacts(new PersonContact(user, MOBILE_CONTACT_TYPE, ldapUser.getMobile()));
        }
        try {
            userDAO.save(user);
            LOGGER.debug("Created");
        } catch (Exception e) {
            LOGGER.error("Cannot INSERT new ROW : " + e.getCause().getMessage());
        }
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
            localUser.setFired(ldapUser.isFired());
            localUser.setLogin(ldapUser.getLogin());
            localUser.setFirstName(ldapUser.getFirstName());
            localUser.setLastName(ldapUser.getLastName());
            localUser.setMiddleName(ldapUser.getPatrName());
            if (ldapUser.getJobPosition() != null && !ldapUser.getJobPosition().isEmpty() && !ldapUser.getJobPosition().equals(localUser.getJobPosition().getValue())) {
                final String requestedPositionName = ldapUser.getJobPosition().trim();
                boolean founded = false;
                for (Position position : ALL_POSITIONS) {
                    if (requestedPositionName.equalsIgnoreCase(position.getValue())) {
                        localUser.setJobPosition(position);
                        founded = true;
                        break;
                    }
                }
                if (!founded) {
                    LOGGER.error("NOT FOUND JobPosition [" + requestedPositionName + "]");
                }
            }
            if (ldapUser.getJobDepartment() != null && !ldapUser.getJobDepartment().isEmpty() && !ldapUser.getJobDepartment().equals(localUser.getJobDepartment().getValue())) {
                final String requestedDepartmentName = ldapUser.getJobDepartment().trim();
                boolean founded = false;
                for (Department department : ALL_DEPARTMENTS) {
                    if (requestedDepartmentName.equalsIgnoreCase(department.getValue())) {
                        localUser.setJobDepartment(department);
                        founded = true;
                        break;
                    }
                }
                if (!founded) {
                    LOGGER.error("NOT FOUND JobDepartment [" + requestedDepartmentName + "]");
                }
            }
            localUser.setLastModified(ldapUser.getLastModified());
            //Поиск и обновление контактных данных
            boolean foundedEmail = ldapUser.getMail() == null || ldapUser.getMail().isEmpty();
            boolean foundedPhone = ldapUser.getPhone() == null || ldapUser.getPhone().isEmpty();
            boolean foundedMobile = ldapUser.getMobile() == null || ldapUser.getMobile().isEmpty();
            for (PersonContact contact : localUser.getContacts()) {
                if (!foundedEmail && EMAIL_CONTACT_TYPE.equals(contact.getType())) {
                    contact.setValue(ldapUser.getMail());
                    foundedEmail = true;
                } else if (!foundedPhone && PHONE_CONTACT_TYPE.equals(contact.getType())) {
                    contact.setValue(ldapUser.getPhone());
                    foundedPhone = true;
                } else if (!foundedMobile && MOBILE_CONTACT_TYPE.equals(contact.getType())) {
                    contact.setValue(ldapUser.getMobile());
                    foundedMobile = true;
                }
            }
            if (!foundedEmail) {
                localUser.addToContacts(new PersonContact(localUser, EMAIL_CONTACT_TYPE, ldapUser.getMail()));
            }
            if (!foundedPhone) {
                localUser.addToContacts(new PersonContact(localUser, PHONE_CONTACT_TYPE, ldapUser.getPhone()));
            }
            if (!foundedMobile) {
                localUser.addToContacts(new PersonContact(localUser, MOBILE_CONTACT_TYPE, ldapUser.getMobile()));
            }
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