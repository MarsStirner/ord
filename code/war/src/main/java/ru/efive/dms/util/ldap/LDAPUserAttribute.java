package ru.efive.dms.util.ldap;

/**
 * @author Nastya Peshekhonova
 */
public enum LDAPUserAttribute {
    // Атрибут фамилии
    LAST_NAME_ATTR_VALUE("sn"),
    // Атрибут имени
    FIRST_NAME_ATTR_VALUE("givenName"),
    // Атрибут отчества  (В АD нет поля для отчества, его дописывают рядом с именем)
    // MIDDLE_NAME_ATTR_VALUE("middleName"),
    // Атрибут штатного подразделения
    JOB_DEPARTMENT_ATTR_VALUE("physicalDeliveryOfficeName"),
    // Атрибут штатной должности
    JOB_POSITION_ATTR_VALUE("description"),
    // Атрибут почтового адреса (если не указан, равен логин + @ + почтовый
    // домен
    MAIL_ATTR_VALUE("mail"),
    // Атрибут мобильного телефона
    MOBILE_PHONE_ATTR_VALUE("mobile"),
    // Атрибут телефона
    PHONE_ATTR_VALUE("telephoneNumber"),
    // Атрибут табельного номера
    //EMPLOYER_ID_ATTR_VALUE("employeeID"),
    // Атрибут универсального идентификатора
    UNID_ATTR_VALUE("objectGUID"),
    // ДАта последнего изменения
    LAST_MODIFIED_ATTR_VALUE("whenChanged"),
    // Атрибут ФИО (ФИО полностью)
    FIO_ATTR_VALUE("DisplayName"),
    // Атрибут логина
    LOGIN_ATTR_VALUE("sAMAccountName"),
    // Атрибут разрешения входа
    UAC_ATTR_VALUE("userAccountControl"),
    //Distinguished Name
    DN_ATTR("distinguishedName");
    private final String name;

    LDAPUserAttribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
