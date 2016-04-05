package ru.efive.dms.util.ldap;

import ru.entity.model.referenceBook.ContactInfoType;
import ru.entity.model.referenceBook.Department;
import ru.entity.model.referenceBook.Position;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.user.UserDAOHibernate;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 01.04.2016, 18:06 <br>
 * Company: hitsl (Hi-Tech Solutions) <br>
 * Description:  Объект для хранения данных нужных в процессе импорта пользователй<br>
 */
public class ImportCacheObject {
    //Пароль по-умолчанию для новых пользователей
    public static final String DEFAULT_PASSWORD = "12345";
    //DAO для работы с локальными пользователями
    private UserDAOHibernate userDAO;
    // Уровнь допуска по-умолчанию для создаваемых пользователей
    private UserAccessLevel defaultUserAccessLevel;
    //Перечень типов контактной информации
    private ContactInfoType emailContactType;
    private ContactInfoType phoneContactType;
    private ContactInfoType mobileContactType;
    //Перечень должностей
    private List<Position> positions;
    //Перечень подразделений
    private List<Department> departments;
    private List<User> localUsers;

    public UserDAOHibernate getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(final UserDAOHibernate userDAO) {
        this.userDAO = userDAO;
    }

    public UserAccessLevel getDefaultUserAccessLevel() {
        return defaultUserAccessLevel;
    }

    public void setDefaultUserAccessLevel(final UserAccessLevel defaultUserAccessLevel) {
        this.defaultUserAccessLevel = defaultUserAccessLevel;
    }

    public ContactInfoType getEmailContactType() {
        return emailContactType;
    }

    public void setEmailContactType(final ContactInfoType emailContactType) {
        this.emailContactType = emailContactType;
    }

    public ContactInfoType getPhoneContactType() {
        return phoneContactType;
    }

    public void setPhoneContactType(final ContactInfoType phoneContactType) {
        this.phoneContactType = phoneContactType;
    }

    public ContactInfoType getMobileContactType() {
        return mobileContactType;
    }

    public void setMobileContactType(final ContactInfoType mobileContactType) {
        this.mobileContactType = mobileContactType;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(final List<Position> positions) {
        this.positions = positions;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(final List<Department> departments) {
        this.departments = departments;
    }

    public void setLocalUsers(final List<User> localUsers) {
        this.localUsers = localUsers;
    }

    public List<User> getLocalUsers() {
        return localUsers;
    }
}
