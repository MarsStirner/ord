package ru.efive.sql.entity.user;

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.util.ApplicationHelper;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Пользователь системы
 */
@Entity
@Table(name = "dms_system_persons")
public class User extends IdentifiedEntity {

    public String getUNID() {
        return unid;
    }

    public void setUNID(String unid) {
        this.unid = unid;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public List<Role> getRoleList() {
        List<Role> result = new ArrayList<Role>();
        if (roles != null) {
            result.addAll(roles);
        }
        Collections.sort(result, new Comparator<Role>() {
            public int compare(Role o1, Role o2) {
                return o1.getId() - o2.getId();
            }
        });
        return result;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * полное имя
     */
    @Transient
    public String getDescription() {
        return lastName + " " + (firstName != null && !firstName.equals("") ? firstName + " " : "") +
                (middleName != null && !middleName.equals("") ? middleName : "");
    }

    /**
     * краткая форма полного имени
     */
    @Transient
    public String getDescriptionShort() {
        return lastName + " " + (firstName != null && !firstName.equals("") ? firstName.substring(0, 1) + ". " : "") +
                (middleName != null && !middleName.equals("") ? middleName.substring(0, 1) + "." : "");
    }

    @Transient
    public boolean isAdministrator() {
        Set<Role> roles = getRoles();
        for (Role role : roles) {
            if (role.getRoleType().name().equals("ADMINISTRATOR")) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isRecorder() {
        Set<Role> roles = getRoles();
        for (Role role : roles) {
            if (role.getRoleType().name().equals("RECORDER")) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isOfficeManager() {
        Set<Role> roles = getRoles();
        for (Role role : roles) {
            if (role.getRoleType().name().equals("OFFICE_MANAGER")) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isRequestManager() {
        Set<Role> roles = getRoles();
        for (Role role : roles) {
            if (role.getRoleType().name().equals("REQUEST_MANAGER")) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isEmployer() {
        Set<Role> roles = getRoles();
        for (Role role : roles) {
            if (role.getRoleType().name().equals("EMPLOYER")) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isOuter() {
        Set<Role> roles = getRoles();
        for (Role role : roles) {
            if (role.getRoleType().name().equals("OUTER")) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isHr() {
        Set<Role> roles = getRoles();
        for (Role role : roles) {
            if (role.getRoleType().name().equals("HR")) {
                return true;
            }
        }
        return false;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Transient
    public String getLastnameFM() {

        return ((ApplicationHelper.nonEmptyString(lastName) ? lastName + " " : "") +
                (ApplicationHelper.nonEmptyString(firstName) ? firstName.substring(0, 1) + "." : "") +
                (ApplicationHelper.nonEmptyString(middleName) ? middleName.substring(0, 1) + "." : ""));
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setInternalNumber(String internalNumber) {
        this.internalNumber = internalNumber;
    }

    public String getInternalNumber() {
        return internalNumber;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setJobPosition(String jobPosition) {
        this.jobPosition = jobPosition;
    }

    public String getJobPosition() {
        return jobPosition;
    }

    public void setJobDepartment(String jobDepartment) {
        this.jobDepartment = jobDepartment;
    }

    public String getJobDepartment() {
        return jobDepartment;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public Set<Group> getGroups() {
        return groups;

    }

    public void setMaxUserAccessLevel(UserAccessLevel maxUserAccessLevel) {
        this.maxUserAccessLevel = maxUserAccessLevel;
    }

    public UserAccessLevel getMaxUserAccessLevel() {
        return maxUserAccessLevel;
    }

    public void setCurrentUserAccessLevel(UserAccessLevel currentUserAccessLevel) {
        this.currentUserAccessLevel = currentUserAccessLevel;
    }

    public UserAccessLevel getCurrentUserAccessLevel() {
        return currentUserAccessLevel;
    }

    @Column(unique = true)
    private String GUID;

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String gUID) {
        GUID = gUID;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Boolean getFired() {
        return fired;
    }

    public void setFired(Boolean fired) {
        this.fired = fired;
    }

    public Date getFiredDate() {
        return firedDate;
    }

    public void setFiredDate(Date firedDate) {
        this.firedDate = firedDate;
    }

    @Column(unique = true)
    private String unid;

    /**
     * учетная запись
     */
    @Column(unique = true)
    private String login;

    /**
     * пароль
     */
    private String password;

    /**
     * email
     */
    private String email;

    /**
     * phone
     */
    private String phone;

    /**
     * work phone
     */
    private String workPhone;

    /**
     * internal number
     */
    private String internalNumber;

    /**
     * mobile phone
     */
    private String mobilePhone;

    /**
     * Максимальный уровень допуска
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private UserAccessLevel maxUserAccessLevel;

    /**
     * Текущий уровень допуска
     */
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private UserAccessLevel currentUserAccessLevel;

    /**
     * группы
     */
    //@ManyToMany(fetch = FetchType.EAGER, mappedBy="members", targetEntity=ru.efive.sql.entity.user.Group.class)
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER, mappedBy = "members")
    /*@JoinTable(name = "groups_dms_system_persons",
joinColumns = { @JoinColumn(name = "members_id") },
inverseJoinColumns = { @JoinColumn(name = "groups_id") })*/
    private Set<Group> groups = new HashSet<Group>();

    /**
     * фамилия
     */
    private String lastName;

    /**
     * имя
     */
    private String firstName;

    /**
     * отчество
     */
    private String middleName;

    /**
     * роли
     */
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "dms_system_person_roles",
            joinColumns = {@JoinColumn(name = "person_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<Role> roles;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastModified;

    /**
     * дата создания учетной записи
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    /**
     * дата последнего входа в систему
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastLogin;

    /**
     * должность
     */
    private String jobPosition;

    /**
     * подразделение
     */
    private String jobDepartment;

    private Boolean fired;

    private Date firedDate;

    /**
     * true - пользователь удалён, false или null - не удалён
     */
    private Boolean deleted;

    private static final long serialVersionUID = -7649892958713448678L;
}