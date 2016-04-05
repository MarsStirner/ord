package ru.efive.dms.util.ldap;

import javax.naming.NamingEnumeration;
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

    private static final SimpleDateFormat lastModifiedFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final String BLOCKED_ACCOUNT_MARKER = "514";

    //Distinguished Name (DN)
    private String DN;
    //Фамилия
    private String lastName;
    //Имя
    private String firstName;
    //Отчество
    private String patrName;
    //Подразделение
    private String jobDepartment;
    //Должность
    private String jobPosition;
    //Email
    private String mail;
    //Мобильный телефон
    private String mobile;
    //Рабочий телефон
    //*Добавочный номер = рабочий телефон
    private String phone;
    // Уволен
    private boolean fired;
    //GUID
    private String guid;
    // Дата последнего изменения
    private Date lastModified;
    //Логин
    private String login;

    public LDAPUser(Attributes attributes) throws NamingException, ParseException {
        final NamingEnumeration<? extends Attribute> allAtributes = attributes.getAll();
        while (allAtributes != null && allAtributes.hasMore()) {
            final Attribute current = allAtributes.next();
            if (LDAPUserAttribute.LAST_NAME_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setLastName(current);
            } else if (LDAPUserAttribute.FIRST_NAME_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setFirstName(current);
            } else if (LDAPUserAttribute.JOB_DEPARTMENT_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setJobDepartment(current);
            } else if (LDAPUserAttribute.JOB_POSITION_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setJobPosition(current);
            } else if (LDAPUserAttribute.MAIL_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setUserMail(current);
            } else if (LDAPUserAttribute.MOBILE_PHONE_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setMobile(current);
            } else if (LDAPUserAttribute.PHONE_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setPhone(current);
            } else if (LDAPUserAttribute.UNID_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setGuid(current);
            } else if (LDAPUserAttribute.LAST_MODIFIED_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setLastModified(current);
            } else if (LDAPUserAttribute.FIO_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())) {
                setFIO(current);
            } else if(LDAPUserAttribute.DN_ATTR.getName().equalsIgnoreCase(current.getID())) {
                setDN(current);
            } else if(LDAPUserAttribute.LOGIN_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())){
                setLogin(current);
            } else if(LDAPUserAttribute.UAC_ATTR_VALUE.getName().equalsIgnoreCase(current.getID())){
                setBlocked(current);
            }
        }
    }

    private void setBlocked(final Attribute blocked) throws NamingException {
        if(blocked !=null){
            this.fired = BLOCKED_ACCOUNT_MARKER.equals(blocked.get());
        }
    }

    private void setLogin(Attribute login) throws NamingException {
       if(login!=null){
           this.login = (String) login.get();
       }
        if(mail == null || mail.isEmpty()){
            this.mail = this.login +"@fccho-moscow.ru";
        }
    }

    public String getLogin() {
        return login;
    }

    private void setDN(Attribute dn) throws NamingException {
        this.DN = (String)dn.get();
    }
    public String getDN(){
        return DN;
    }

    /**
     * Фамилия
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    private void setLastName(Attribute lastName) throws NamingException {
        if (lastName != null) {
            this.lastName = ((String) lastName.get()).trim();
        }
    }

    /**
     * Имя
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    private void setFirstName(Attribute firstName) throws NamingException {
        if (firstName != null) {
            this.firstName = ((String) firstName.get()).trim();
        }
    }

    /**
     * Подразделение
     * @return
     */
    public String getJobDepartment() {
        return jobDepartment;
    }

    private void setJobDepartment(Attribute jobDepartment) throws NamingException {
        if (jobDepartment != null) {
            this.jobDepartment = ((String) jobDepartment.get()).trim();
        }
    }

    /**
     * Должность
     * @return
     */
    public String getJobPosition() {
        return jobPosition;
    }

    private void setJobPosition(Attribute jobPosition) throws NamingException {
        if (jobPosition != null) {
            this.jobPosition = ((String) jobPosition.get()).trim();
        }
    }

    /**
     * Email
     * @return
     */
    public String getMail() {
        return mail;
    }

    private void setUserMail(Attribute mail) throws NamingException {
        if (mail != null) {
            this.mail = (String) mail.get();
        }
    }

    /**
     * Мобильный телефон
     * @return
     */
    public String getMobile() {
        return mobile;
    }

    private void setMobile(Attribute mobile) throws NamingException {
        if (mobile != null) {
            final String value = ((String) mobile.get()).trim();
            if(!value.contains(".")){
                this.mobile = value;
            }
        }
    }

    /**
     * Номер телефона
     * @return
     */
    public String getPhone() {
        return phone;
    }

    private void setPhone(Attribute phone) throws NamingException {
        if (phone != null) {
            this.phone = ((String) phone.get()).trim();
        }
    }


    /**
     * GUID
     * @return
     */
    public String getGuid() {
        return guid;
    }

    public void setGuid(Attribute objectGUID) throws NamingException {
        //Не может быть null
        byte [] bytes = (byte[]) objectGUID.get();
        StringBuffer guid = new StringBuffer();
        for (byte aByte : bytes) {
            StringBuffer dblByte = new StringBuffer(Integer.toHexString(aByte & 0xff));
            if (dblByte.length() == 1) {
                guid.append("0");
            }
            guid.append(dblByte);
        }
        this.guid = guid.toString();
    }

    /**
     * Дата последнего изменения
     * @return
     */
    public Date getLastModified() {
        return lastModified;
    }

    private void setLastModified(Attribute lastModified) throws NamingException, ParseException {
        if (lastModified != null) {
            this.lastModified = lastModifiedFormat.parse((String) lastModified.get());
        }   else {
            this.lastModified = new Date();
        }
    }

    /**
     * Уволен
     * @param fired
     */
    public void setFired(boolean fired) {
        this.fired = fired;
    }

    public boolean isFired() {
        return fired;
    }

    /**
     * ФИО
     * @param fio
     */
    private void setFIO(Attribute fio) throws NamingException {
          if(fio != null){
              final String value = ((String)fio.get()).trim();
              final String[] fioParts = value.split(" ");
              switch (fioParts.length){
                  case 1:{
                      if(lastName == null || lastName.isEmpty() ) {
                          lastName = fioParts[0];
                      }
                      break;
                  }
                  case 2: {
                      if(lastName == null || lastName.isEmpty() ) {
                          lastName = fioParts[0];
                      }
                      if(firstName == null || firstName.isEmpty()){
                          firstName = fioParts[1];
                      }
                      break;
                  }
                  case 3: {
                      if(lastName == null || lastName.isEmpty() ) {
                          lastName = fioParts[0];
                      }
                      if(firstName == null || firstName.isEmpty()){
                          firstName = fioParts[1];
                      }
                      if(patrName == null || patrName.isEmpty()){
                         patrName = fioParts[2];
                      }
                      break;
                  }
                  default:{
                      break;
                  }
              }
          }
    }

    public String getPatrName() {
        return patrName;
    }
}
