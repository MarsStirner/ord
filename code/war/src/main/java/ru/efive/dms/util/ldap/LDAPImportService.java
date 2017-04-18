package ru.efive.dms.util.ldap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.referenceBook.ContactInfoType;
import ru.entity.model.referenceBook.Department;
import ru.entity.model.referenceBook.Position;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.PersonContact;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;
import ru.hitsl.sql.dao.interfaces.referencebook.ContactInfoTypeDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DepartmentDao;
import ru.hitsl.sql.dao.interfaces.referencebook.PositionDao;
import ru.hitsl.sql.dao.interfaces.referencebook.UserAccessLevelDao;
import ru.util.ApplicationHelper;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;


@Service("ldapImportService")
@Transactional("ordTransactionManager")
public class LDAPImportService {
    //Именованный логгер (LDAP)
    private static final Logger LOGGER = LoggerFactory.getLogger("LDAP");

    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;
    @Autowired
    @Qualifier("userAccessLevelDao")
    private UserAccessLevelDao userAccessLevelDao;
    @Autowired
    @Qualifier("contactInfoTypeDao")
    private ContactInfoTypeDao contactInfoTypeDao;
    @Autowired
    @Qualifier("positionDao")
    private PositionDao positionDao;
    @Autowired
    @Qualifier("departmentDao")
    private DepartmentDao departmentDao;

    private String ldapAddressValue;
    private String loginValue;
    private String passwordValue;
    private String baseValue;
    private String firedBaseValue;
    private String filterValue;


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

    public void setFiredBaseValue(String firedBaseValue) {
        this.firedBaseValue = firedBaseValue;
    }

    public void run() throws Exception {
        LOGGER.info("START");
        final PreferencesObject preferences = createPreferences();
        final ImportCacheObject cache = createImportCache();
        //Получить список корректных пользователей из LDAP
        final List<LDAPUser> userList = loadUsers(preferences, baseValue, filterValue);
        //Получить список уволенных пользователей
        preferences.setLdapContext(getLdapContext());
        final List<LDAPUser> firedUserList = loadUsers(preferences, firedBaseValue, filterValue);
        for (LDAPUser current : firedUserList) {
            current.setFired(true);
        }
        // Смешать всех пользователей в один список
        userList.addAll(firedUserList);
        synchronizeAll(userList, cache);
        LOGGER.info("END");
    }


    private void synchronizeAll(final List<LDAPUser> userList, final ImportCacheObject cache) {
        int i = 0;
        for (LDAPUser currentLdapUser : userList) {
            i++;
            LOGGER.debug("#{} Start processing [{}]", i, currentLdapUser.getDN());
            if (currentLdapUser.getDN().contains("Глушкова Светлана Юрьевна")) {
                i++;
                i--;
            }
            //Поиск совпадений  по GUID
            boolean foundedByGuid = false;
            for (User currentLocalUser : cache.getLocalUsers()) {
                if (currentLdapUser.getGuid().equalsIgnoreCase(currentLocalUser.getGUID())) {
                    foundedByGuid = true;
                    LOGGER.debug("#{} Founded by GUID. localID={}", i, currentLocalUser.getId());
                    synchronizeUser(currentLdapUser, currentLocalUser, cache);
                    break;
                }
            }
            //Для тех, кому не нашли соответсвия по GUID
            if (!foundedByGuid) {
                //Выполняем поиск по ФИО
                final String curLDAPLast = currentLdapUser.getLastName();
                final String curLDAPFirst = currentLdapUser.getFirstName();
                final String curLDAPPatr = currentLdapUser.getPatrName();

                final List<User> overlappedByFioUserList = new ArrayList<>(4);
                for (User curLocalUser : cache.getLocalUsers()) {
                    //Совпадает ФИО
                    if (
                        //[Обе Фамилии null]
                        // ИЛИ
                        // [Обе не null И равны без пробелов]
                            ((curLDAPLast == null && curLocalUser.getLastName() == null) || (curLDAPLast != null && curLocalUser
                                    .getLastName() != null && curLDAPLast.equalsIgnoreCase(curLocalUser.getLastName().trim()))) &&
                                    ((curLDAPFirst == null && curLocalUser.getFirstName() == null) || (curLDAPFirst != null && curLocalUser
                                            .getFirstName() != null && curLDAPFirst.equalsIgnoreCase(curLocalUser.getFirstName().trim()))

                                    ) &&
                                    ((curLDAPPatr == null && curLocalUser.getMiddleName() == null) || (curLDAPPatr != null && curLocalUser
                                            .getMiddleName() != null && curLDAPPatr.equalsIgnoreCase(curLocalUser.getMiddleName().trim()))

                                    )) {
                        LOGGER.debug("#{} Founded by FIO. localID={}", i, curLocalUser.getId());
                        overlappedByFioUserList.add(curLocalUser);
                    }
                }
                switch (overlappedByFioUserList.size()) {
                    //Не нашли пользователя ни по ФИО ни по GUID
                    case 0: {
                        //Не было найдено ни одного совпадения
                        if (!currentLdapUser.isFired()) {
                            createNewUser(currentLdapUser, cache);
                        } else {
                            LOGGER.debug("#{} Skip  because FIRED", i);
                        }
                        break;
                    }
                    //Нашли единственного пользователя по ФИО
                    case 1: {
                        final User uniqueFioUser = overlappedByFioUserList.iterator().next();
                        if (uniqueFioUser.getGUID() != null) {
                            //Но у него заполнен GUID и не совпадает с нашим (иначе мы бы сюда не дошли)
                            LOGGER.error("#{} Founded by FIO user has different GUID:{}", i, uniqueFioUser.getGUID());
                            break;
                        } else {
                            //Похоже наш кадр, только GUID не заполнен
                            synchronizeUser(currentLdapUser, uniqueFioUser, cache);
                            break;
                        }
                    }
                    //Больше одного совпадения по ФИО
                    default: {
                        LOGGER.error("#{} Too many users founded by FIO, skip this LDAP user", i);
                        break;
                    }
                }//END OF SWITCH
            }
        }
    }

    private List<LDAPUser> loadUsers(final PreferencesObject preferences, final String baseValue, final String filterValue)
            throws IOException, NamingException {
        final List<LDAPUser> result = new ArrayList<>(preferences.getPageSize());
        // Activate paged results
        byte[] cookie = null;
        final LdapContext context = preferences.getLdapContext();
        int i = 0;
        do {
            context.setRequestControls(new Control[]{new PagedResultsControl(preferences.getPageSize(), cookie, Control.NONCRITICAL)});
            /* perform the search */
            LOGGER.info("Select ldap users[{}\\{}]", baseValue, filterValue);
            final NamingEnumeration results = context.search(baseValue, filterValue, preferences.getSearchControls());
            /* for each entry print out name + all attrs and values */
            while (results != null && results.hasMore()) {
                final LDAPUser temp = convertToLdapUser((SearchResult) results.next(), i++);
                if (temp != null) {
                    result.add(temp);
                } else {
                    LOGGER.warn("SKIP User: Cannot convert to LDAP structure");
                }
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
        } while (cookie != null);
        context.close();
        LOGGER.debug("PROCESSED {} LDAP USERS", i);
        return result;
    }

    private LDAPUser convertToLdapUser(final SearchResult entry, final int currentNumber) {
        LOGGER.info("#{} DN=\'{}\'", currentNumber, entry.getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(printAttributesToString(entry));
        }
        try {
            return new LDAPUser(entry.getAttributes());
        } catch (ParseException e) {
            LOGGER.error("{} ParseException: ", currentNumber, e);
        } catch (NamingException e) {
            LOGGER.error("{} NamingException: ", currentNumber, e);
        }
        return null;
    }

    private void createNewUser(final LDAPUser ldapUser, final ImportCacheObject cache) {
        final User user = new User();
        user.setCreated(LocalDateTime.now());
        user.setDeleted(false);
        //При создании указывать пароль по умолчанию "12345")
        user.setPassword(ImportCacheObject.DEFAULT_PASSWORD);
        user.setLogin(ldapUser.getLogin());
        //Максимальный и все остальные уровни допуска выставить как "Для общего пользования"
        user.setMaxUserAccessLevel(cache.getDefaultUserAccessLevel());
        user.setCurrentUserAccessLevel(cache.getDefaultUserAccessLevel());
        user.setGUID(ldapUser.getGuid());
        user.setLastModified(ldapUser.getLastModified());
        user.setFired(ldapUser.isFired());
        user.setFirstName(ldapUser.getFirstName());
        user.setLastName(ldapUser.getLastName());
        user.setMiddleName(ldapUser.getPatrName());
        user.setEmail(ldapUser.getMail());
        if (ldapUser.getJobPosition() != null && !ldapUser.getJobPosition().isEmpty()) {
            final String requestedPositionName = ldapUser.getJobPosition().trim();
            boolean founded = false;
            for (Position position : cache.getPositions()) {
                if (requestedPositionName.equalsIgnoreCase(position.getValue())) {
                    user.setJobPosition(position);
                    founded = true;
                    break;
                }
            }
            if (!founded) {
                LOGGER.error("NOT FOUND JobPosition [" + requestedPositionName + "]");
            }
            user.setJobPositionString(requestedPositionName);
        }
        if (ldapUser.getJobDepartment() != null && !ldapUser.getJobDepartment().isEmpty()) {
            final String requestedDepartmentName = ldapUser.getJobDepartment().trim();
            boolean founded = false;
            for (Department department : cache.getDepartments()) {
                if (requestedDepartmentName.equalsIgnoreCase(department.getValue())) {
                    user.setJobDepartment(department);
                    founded = true;
                    break;
                }
            }
            if (!founded) {
                LOGGER.error("NOT FOUND JobDepartment [" + requestedDepartmentName + "]");
            }
            user.setJobDepartmentString(requestedDepartmentName);
        }
        user.setLastModified(ldapUser.getLastModified());
        if (ldapUser.getMail() != null && !ldapUser.getMail().isEmpty()) {
            user.addToContacts(new PersonContact(user, cache.getEmailContactType(), ldapUser.getMail()));
        }
        if (ldapUser.getPhone() != null && !ldapUser.getPhone().isEmpty()) {
            user.addToContacts(new PersonContact(user, cache.getPhoneContactType(), ldapUser.getPhone()));
        }
        if (ldapUser.getMobile() != null && !ldapUser.getMobile().isEmpty()) {
            user.addToContacts(new PersonContact(user, cache.getMobileContactType(), ldapUser.getMobile()));
        }
        try {
            userDao.save(user);
            LOGGER.debug("Created");
        } catch (Exception e) {
            LOGGER.error("Cannot INSERT new ROW : ", e);
        }
    }

    /**
     * @param ldapUser  пользователь из ldap
     * @param localUser локальный пользователь
     */
    private void synchronizeUser(LDAPUser ldapUser, User localUser, ImportCacheObject cache) {
        LOGGER.debug("Synchronize with localID=" + localUser.getId());
        try {
            if (localUser.getLastModified() != null && Objects.equals(localUser.getLastModified(), ldapUser.getLastModified()) && localUser
                    .getGUID() != null) {
                LOGGER.debug("Already Up to Date");
            } else {
                localUser.setGUID(ldapUser.getGuid());
                localUser.setLastModified(ldapUser.getLastModified());
                localUser.setFired(ldapUser.isFired());
                localUser.setLogin(ldapUser.getLogin());
                localUser.setFirstName(ldapUser.getFirstName());
                localUser.setLastName(ldapUser.getLastName());
                localUser.setMiddleName(ldapUser.getPatrName());
                if (StringUtils.isNotEmpty(ldapUser.getMail())) {
                    localUser.setEmail(ldapUser.getMail());
                }
                if (ldapUser.getJobPosition() != null && !ldapUser.getJobPosition().isEmpty()) {
                    final String requestedPositionName = ldapUser.getJobPosition().trim();
                    boolean founded = false;
                    for (Position position : cache.getPositions()) {
                        if (requestedPositionName.equalsIgnoreCase(position.getValue())) {
                            localUser.setJobPosition(position);
                            founded = true;
                            break;
                        }
                    }
                    if (!founded) {
                        LOGGER.error("NOT FOUND JobPosition [" + requestedPositionName + "]");
                    }
                    localUser.setJobPositionString(ldapUser.getJobPosition());
                }
                if (ldapUser.getJobDepartment() != null && !ldapUser.getJobDepartment().isEmpty()) {
                    final String requestedDepartmentName = ldapUser.getJobDepartment().trim();
                    boolean founded = false;
                    for (Department department : cache.getDepartments()) {
                        if (requestedDepartmentName.equalsIgnoreCase(department.getValue())) {
                            localUser.setJobDepartment(department);
                            founded = true;
                            break;
                        }
                    }
                    if (!founded) {
                        LOGGER.error("NOT FOUND JobDepartment [" + requestedDepartmentName + "]");
                    }
                    localUser.setJobDepartmentString(ldapUser.getJobDepartment());
                }
                localUser.setLastModified(ldapUser.getLastModified());
                //Поиск и обновление контактных данных
                boolean foundedEmail = ldapUser.getMail() == null || ldapUser.getMail().isEmpty();
                boolean foundedPhone = ldapUser.getPhone() == null || ldapUser.getPhone().isEmpty();
                boolean foundedMobile = ldapUser.getMobile() == null || ldapUser.getMobile().isEmpty();
                for (PersonContact contact : localUser.getContacts()) {
                    if (!foundedEmail && cache.getEmailContactType().equals(contact.getType())) {
                        contact.setValue(ldapUser.getMail());
                        foundedEmail = true;
                    } else if (!foundedPhone && cache.getPhoneContactType().equals(contact.getType())) {
                        contact.setValue(ldapUser.getPhone());
                        foundedPhone = true;
                    } else if (!foundedMobile && cache.getMobileContactType().equals(contact.getType())) {
                        contact.setValue(ldapUser.getMobile());
                        foundedMobile = true;
                    }
                }
                if (!foundedEmail) {
                    localUser.addToContacts(new PersonContact(localUser, cache.getEmailContactType(), ldapUser.getMail()));
                    localUser.setEmail(ldapUser.getMail());
                }
                if (!foundedPhone) {
                    localUser.addToContacts(new PersonContact(localUser, cache.getPhoneContactType(), ldapUser.getPhone()));
                }
                if (!foundedMobile) {
                    localUser.addToContacts(new PersonContact(localUser, cache.getMobileContactType(), ldapUser.getMobile()));
                }
                userDao.save(localUser);
                LOGGER.debug("Updated");
            }
        } catch (Exception e) {
            LOGGER.error("Synchronize with localID=" + localUser.getId() + " FAILED.", e);
        }
    }

    private ImportCacheObject createImportCache() throws Exception {
        LOGGER.debug("Start creating cache");
        final ImportCacheObject result = new ImportCacheObject();
        final Optional<UserAccessLevel> defaultUserAccessLevel = userAccessLevelDao.getItems().stream().filter(x -> x.getLevel() == 1).findFirst();
        if (defaultUserAccessLevel.isPresent()) {
            result.setDefaultUserAccessLevel(defaultUserAccessLevel.get());
        } else {
            LOGGER.error("CACHE_ERROR: Upload not started: cause not founded default UserAccessLevel = 1");
            throw new Exception("CACHE_ERROR: Upload not started: cause not founded default UserAccessLevel = 1");
        }

        final List<ContactInfoType> contactTypes = contactInfoTypeDao.getItems();
        for (ContactInfoType contactType : contactTypes) {
            if (ContactInfoType.RB_CODE_EMAIL.equals(contactType.getCode())) {
                result.setEmailContactType(contactType);
            } else if (ContactInfoType.RB_CODE_PHONE.equals(contactType.getCode())) {
                result.setPhoneContactType(contactType);
            } else if (ContactInfoType.RB_CODE_MOBILE_PHONE.equals(contactType.getCode())) {
                result.setMobileContactType(contactType);
            }
        }
        if (result.getEmailContactType() == null) {
            LOGGER.error("CACHE_ERROR: NOT FOUNDED EMAIL_CONTACT_TYPE, UPLOAD NOT Started");
            throw new Exception("CACHE_ERROR: NOT FOUNDED EMAIL_CONTACT_TYPE, UPLOAD NOT Started");
        } else if (result.getPhoneContactType() == null) {
            LOGGER.error("CACHE_ERROR: NOT FOUNDED PHONE_CONTACT_TYPE, UPLOAD NOT Started");
            throw new Exception("CACHE_ERROR: NOT FOUNDED PHONE_CONTACT_TYPE, UPLOAD NOT Started");
        } else if (result.getMobileContactType() == null) {
            LOGGER.error("CACHE_ERROR: NOT FOUNDED MOBILE_CONTACT_TYPE, UPLOAD NOT Started");
            throw new Exception("CACHE_ERROR: NOT FOUNDED MOBILE_CONTACT_TYPE, UPLOAD NOT Started");
        }
        result.setPositions(positionDao.getItems());
        result.setDepartments(departmentDao.getItems());
        if (result.getDepartments() == null || result.getDepartments().isEmpty()) {
            LOGGER.warn("CACHE_WARNING: NO JOB_DEPARTMENTS founded");
        }
        if (result.getPositions() == null || result.getPositions().isEmpty()) {
            LOGGER.warn("CACHE_WARNING: NO JOB_POSITIONS founded");
        }
        // Create ORD users cache
        LOGGER.debug("Start caching local users");
        result.setLocalUsers(userDao.getItems());
        LOGGER.debug("Cached {} users", result.getLocalUsers() != null ? result.getLocalUsers().size() : -1);
        LOGGER.debug("Cache constructed successfully");
        return result;
    }


    private PreferencesObject createPreferences() throws NamingException {
        final PreferencesObject result = new PreferencesObject();
        final SearchControls searchControls = new SearchControls();
        searchControls.setReturningAttributes(
                new String[]{
                        //DN
                        LDAPUserAttribute.DN_ATTR.getName(),
                        //GUID
                        LDAPUserAttribute.UNID_ATTR_VALUE.getName(), LDAPUserAttribute.LAST_MODIFIED_ATTR_VALUE
                        .getName(), LDAPUserAttribute.LOGIN_ATTR_VALUE.getName(), LDAPUserAttribute.LAST_NAME_ATTR_VALUE
                        .getName(), LDAPUserAttribute.FIRST_NAME_ATTR_VALUE.getName(),
                        // LDAPUserAttribute.MIDDLE_NAME_ATTR_VALUE.getName(),
                        LDAPUserAttribute.MAIL_ATTR_VALUE.getName(), LDAPUserAttribute.PHONE_ATTR_VALUE
                        .getName(), LDAPUserAttribute.MOBILE_PHONE_ATTR_VALUE.getName(), LDAPUserAttribute.JOB_POSITION_ATTR_VALUE
                        .getName(), LDAPUserAttribute.JOB_DEPARTMENT_ATTR_VALUE.getName(),
                        //LDAPUserAttribute.EMPLOYER_ID_ATTR_VALUE.getName(),
                        LDAPUserAttribute.FIO_ATTR_VALUE.getName(), LDAPUserAttribute.UAC_ATTR_VALUE.getName()}
        );
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        result.setSearchControls(searchControls);
        result.setPageSize(100);
        result.setLdapContext(getLdapContext());
        return result;
    }

    private LdapContext getLdapContext() throws NamingException {
        final Properties properties = new Properties();
        properties.put(Context.PROVIDER_URL, ldapAddressValue);
        properties.put(Context.SECURITY_AUTHENTICATION, "Simple");
        properties.put(Context.SECURITY_PRINCIPAL, loginValue);
        properties.put(Context.SECURITY_CREDENTIALS, passwordValue);
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        properties.put("java.naming.ldap.attributes.binary", LDAPUserAttribute.UNID_ATTR_VALUE.getName());
        // the following is helpful in debugging errors  properties.put("com.sun.jndi.ldap.trace.ber", System.err);
        return new InitialLdapContext(properties, null);
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
                    sb.append(attribute.getID()).append('=');
                    if (attribute.get() instanceof byte[]) {
                        sb.append(LDAPUser.convertToDashedString((byte[]) attribute.get()));
                    } else {
                        sb.append(attribute.get());
                    }
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