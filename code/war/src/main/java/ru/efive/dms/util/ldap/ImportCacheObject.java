package ru.efive.dms.util.ldap;

import ru.entity.model.referenceBook.RbContactPointSystem;
import ru.entity.model.referenceBook.Department;
import ru.entity.model.referenceBook.Position;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.User;

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
    // Уровнь допуска по-умолчанию для создаваемых пользователей
    private UserAccessLevel defaultUserAccessLevel;
    //Перечень типов контактной информации
    private RbContactPointSystem emailContactType;
    private RbContactPointSystem phoneContactType;
    private RbContactPointSystem mobileContactType;
    //Перечень должностей
    private List<Position> positions;
    //Перечень подразделений
    private List<Department> departments;
    private List<User> localUsers;

    public UserAccessLevel getDefaultUserAccessLevel() {
        return defaultUserAccessLevel;
    }

    public void setDefaultUserAccessLevel(final UserAccessLevel defaultUserAccessLevel) {
        this.defaultUserAccessLevel = defaultUserAccessLevel;
    }

    public RbContactPointSystem getEmailContactType() {
        return emailContactType;
    }

    public void setEmailContactType(final RbContactPointSystem emailContactType) {
        this.emailContactType = emailContactType;
    }

    public RbContactPointSystem getPhoneContactType() {
        return phoneContactType;
    }

    public void setPhoneContactType(final RbContactPointSystem phoneContactType) {
        this.phoneContactType = phoneContactType;
    }

    public RbContactPointSystem getMobileContactType() {
        return mobileContactType;
    }

    public void setMobileContactType(final RbContactPointSystem mobileContactType) {
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

    public List<User> getLocalUsers() {
        return localUsers;
    }

    public void setLocalUsers(final List<User> localUsers) {
        this.localUsers = localUsers;
    }
}
