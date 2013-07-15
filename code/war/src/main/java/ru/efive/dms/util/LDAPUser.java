package ru.efive.dms.util;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Nastya Peshekhonova
 */
public class LDAPUser {
    private String id;
    private Date lastModified;
    private String login;
    private String lastName;
    private String firstName;
    private String secondName;
    private String mail;
    private String phone;
    private String mobile;
    private String jobPosition;
    private String jobDepartment;
    private Boolean fired;
    private String guid;
    private final String DOMAIN = "fccho-moscow.ru";

    public String getId() {
        return id;
    }

    private void setId(Attribute employeeID) throws NamingException {
        this.id = "";
        if (employeeID != null) {
            this.id = (String) employeeID.get();
        }

        this.id = (!this.id.isEmpty()) ? (Long.valueOf(this.id).toString()) : this.getGuid();
    }

    public Date getLastModified() throws NamingException, ParseException {
        return lastModified;
    }

    private void setLastModified(Attribute lastModified) throws NamingException, ParseException {
        if (lastModified != null) {
            this.lastModified = (new SimpleDateFormat("yyyyMMddHHmmss")).parse((String) lastModified.get());
        }
    }

    public String getLogin() {
        return login;
    }

    private void setLogin(Attribute login) throws NamingException {
        if (login != null) {
            this.login = (String) login.get();
        }
    }

    public String getLastName() {
        return lastName.trim();
    }

    private void setLastName(Attribute lastName) throws NamingException {
        if (lastName != null) {
            this.lastName = (String) lastName.get();
        }
    }

    public String getFirstName() {
        return firstName.trim();
    }

    private void setFirstName(Attribute firstName) throws NamingException {
        if (firstName != null) {
            this.firstName = (String) firstName.get();
        }
    }

    public String getSecondName() {
        return secondName.trim();
    }

    private void setSecondName(Attribute secondName) throws NamingException {
        if (secondName != null) {
            this.secondName = (String) secondName.get();
        }
    }

    public String getMail() {
        return mail;
    }

    private void setUserMail(Attribute mail) throws NamingException {
        if (mail != null) {
            this.mail = (String) mail.get();
        }
        else {
            this.mail = this.getGuid().replace(' ', '.') + '@' + DOMAIN;
        }
    }

    public Boolean getFired() {
        return fired;
    }

    public void setFired(Boolean fired) {
        this.fired = fired;
    }

    public String getPhone() {
        return phone;
    }

    private void setPhone(Attribute phone) throws NamingException {
        if (phone != null) {
            this.phone = (String) phone.get();
        }
    }

    public String getMobile() {
        return mobile;
    }

    private void setMobile(Attribute mobile) throws NamingException {
        if (mobile != null) {
            this.mobile = (String) mobile.get();
        }
    }

    public String getJobPosition() {
        return jobPosition;
    }

    private void setJobPosition(Attribute jobPosition) throws NamingException {
        if (jobPosition != null) {
            this.jobPosition = (String) jobPosition.get();
        }
    }

    public String getJobDepartment() {
        return jobDepartment;
    }

    private void setJobDepartment(Attribute jobDepartment) throws NamingException {
        if (jobDepartment != null) {
            this.jobDepartment = (String) jobDepartment.get();
        }
    }

    public String getFullName() {
        String fullName =
                (lastName != null && !lastName.isEmpty() ? (lastName.trim() + "_") : "") +
                        (firstName != null && !firstName.isEmpty() ? (firstName.trim() + "_") : "") +
                        (secondName != null && !secondName.isEmpty() ? (secondName.trim()) : "");
        if (fullName != null) {
            fullName = fullName.toLowerCase();
        }
        return fullName;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(Attribute objectGUID) throws NamingException {
        byte [] bytes = (byte[]) objectGUID.get();
        StringBuffer guid = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            StringBuffer dblByte = new StringBuffer(Integer.toHexString(bytes[i] & 0xff));
            if (dblByte.length() == 1) {
                guid.append("0");
            }
            guid.append(dblByte);
        }
        this.guid = guid.toString();
    }


    public LDAPUser(Attributes userAttributes, boolean fired) throws NamingException, ParseException {
        setGuid(userAttributes.get(LDAPUserAttribute.UNID_ATTR_VALUE.getName()));
        setId(userAttributes.get(LDAPUserAttribute.EMPLOYER_ID_ATTR_VALUE.getName()));
        setLogin(userAttributes.get(LDAPUserAttribute.LOGIN_ATTR_VALUE.getName()));
        setLastName(userAttributes.get(LDAPUserAttribute.LAST_NAME_ATTR_VALUE.getName()));
        setFirstName(userAttributes.get(LDAPUserAttribute.FIRST_NAME_ATTR_VALUE.getName()));
        setSecondName(userAttributes.get(LDAPUserAttribute.MIDDLE_NAME_ATTR_VALUE.getName()));
        setUserMail(userAttributes.get(LDAPUserAttribute.MAIL_ATTR_VALUE.getName()));
        setPhone(userAttributes.get(LDAPUserAttribute.PHONE_ATTR_VALUE.getName()));
        setMobile(userAttributes.get(LDAPUserAttribute.MOBILE_PHONE_ATTR_VALUE.getName()));
        setJobPosition(userAttributes.get(LDAPUserAttribute.JOB_POSITION_ATTR_VALUE.getName()));
        setJobDepartment(userAttributes.get(LDAPUserAttribute.JOB_DEPARTMENT_ATTR_VALUE.getName()));
        setLastModified(userAttributes.get(LDAPUserAttribute.LAST_MODIFIED_ATTR_VALUE.getName()));
        setFired(fired);
    }
}
