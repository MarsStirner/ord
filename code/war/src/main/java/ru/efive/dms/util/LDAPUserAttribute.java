package ru.efive.dms.util;

/**
 * @author Nastya Peshekhonova
 */
public enum LDAPUserAttribute {
    // Атрибут универсального идентификатора
    UNID_ATTR_VALUE("objectGUID"),
    LAST_MODIFIED_ATTR_VALUE("whenChanged"),
    // Атрибут логина
    LOGIN_ATTR_VALUE("sAMAccountName"),
    // Атрибут фамилии
    LAST_NAME_ATTR_VALUE("sn"),
    // Атрибут имени
    FIRST_NAME_ATTR_VALUE("givenName"),
    // Атрибут отчества
    MIDDLE_NAME_ATTR_VALUE("middleName"),
    // Атрибут почтового адреса (если не указан, равен логин + @ + почтовый
    // домен
    MAIL_ATTR_VALUE("mail"),
    // Атрибут телефона
    PHONE_ATTR_VALUE("telephoneNumber"),
    // Атрибут мобильного телефона
    MOBILE_PHONE_ATTR_VALUE("mobile"),
    // Атрибут штатной должности
    JOB_POSITION_ATTR_VALUE("description"),
    // Атрибут штатного подразделения
    JOB_DEPARTMENT_ATTR_VALUE("physicalDeliveryOfficeName"),
    // Атрибут табельного номера
    EMPLOYER_ID_ATTR_VALUE("employeeID");

    private final String name;

    LDAPUserAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
