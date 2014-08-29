package ru.efive.sql.entity.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.util.StoredCodes;

/**
 * Пользователь системы
 */
@Entity
@Table(name = "dms_system_persons")
public class User extends IdentifiedEntity {

    /**********************************************************************
     * DATABASE FIELD MAPPING START
     */

    /**
     * дата создания учетной записи
     */
    @Column(name = "created")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    /**
     * Признак удаления
     * true - пользователь удалён
     */
    @Column(name = "deleted")
    private boolean deleted;

    /**
     * фамилия
     */
    @Column(name = "lastName")
    private String lastName;

    /**
     * имя
     */
    @Column(name = "firstName")
    private String firstName;

    /**
     * отчество
     */
    @Column(name = "middleName")
    private String middleName;

    /**
     * Адрес почты
     */
    //TODO перенести в contacts
    @Column(name="email")
    private String email;

    /**
     *   Табельный номер сотрудника
     */
    @Column(name = "unid")
    private String UNID;

    /**
     * учетная запись
     */
    @Column(name = "login", unique = true)
    private String login;

    /**
     * пароль (md5 Hash)
     */
    @Column(name = "password")
    private String password;

    /**
     * Соостветствующий идентификатор из AD
     */
    @Column(name = "GUID", unique = true)
    private String GUID;

    /**
     * Признак уволенного сотрудника
     * TRUE - уволен
     */
    @Column(name = "fired")
    private boolean fired;

    /**
     * Дата увольнения сотрудника
     */
    @Column(name = "firedDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date firedDate;

    /**
     * Максимальный уровень допуска
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "maxUserAccessLevel_id")
    private UserAccessLevel maxUserAccessLevel;

    /**
     * Текущий уровень допуска
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "currentUserAccessLevel_id")
    private UserAccessLevel currentUserAccessLevel;

    /**
     * Контактные данные пользователя (почта, телефон, итд)
     * При сохранении пользователя - добавлять или обновлять записи
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "person", cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    private Set<PersonContact> contacts = new HashSet<PersonContact>(4);

    /**
     * группы
     */
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "members")
    private Set<Group> groups = new HashSet<Group>();


    /**
     * роли
     */
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_system_person_roles",
            joinColumns = {@JoinColumn(name = "person_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private Set<Role> roles;

    /**
     * Время последней модификации
     */
    @Column(name = "lastModified")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastModified;


    /**
     * должность
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobPosition_id", nullable = true)
    private Position jobPosition;

    /**
     * подразделение
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobDepartment_id", nullable = true)
    private Department jobDepartment;

    /**
     * *******************************************************************
     * DATABASE FIELD MAPPING END
     */

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
     * Получение полного ФИО в виде строки
     * @return  Строка с полным ФИО
     * "Иванов Иван Иванович"
     * если нету части ФИО - то выводится без нее
     * "Иван Иванович"\"Иванов Иван"\"Иванов Иванович"\"Иван"\"Иванов"\""
     */
    @Transient
    public String getFullName() {
        StringBuilder sb = new StringBuilder("");
        if (lastName != null) {
            sb.append(lastName);
        }
        if (firstName != null) {
            if (sb.length() != 0) {
                sb.append(' ');
            }
            sb.append(firstName);
        }
        if (middleName != null) {
            if (sb.length() != 0) {
                sb.append(' ');
            }
            sb.append(middleName);
        }
        return sb.toString();
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
        for (Role role : roles) {
            if (StoredCodes.RoleType.ADMINISTRATOR.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isRecorder() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.RECORDER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isOfficeManager() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.OFFICE_MANAGER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isRequestManager() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.REQUEST_MANAGER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isEmployer() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.EMPLOYER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isOuter() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.OUTER.equals(role.getRoleType().name())) {
                return true;
            }
        }
        return false;
    }

    @Transient
    public boolean isHr() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.HR.equals(role.getRoleType().name())) {
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Position getJobPosition() {
        return jobPosition;
    }

    public void setJobPosition(Position jobPosition) {
        this.jobPosition = jobPosition;
    }

    public Department getJobDepartment() {
        return jobDepartment;
    }

    public void setJobDepartment(Department jobDepartment) {
        this.jobDepartment = jobDepartment;
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

    public String getGUID() {
        return GUID;
    }

    public void setGUID(String GUID) {
        this.GUID = GUID;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public boolean isFired() {
        return fired;
    }

    public void setFired(boolean fired) {
        this.fired = fired;
    }

    public Date getFiredDate() {
        return firedDate;
    }

    public void setFiredDate(Date firedDate) {
        this.firedDate = firedDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUNID() {
        return UNID;
    }

    public void setUNID(String UNID) {
        this.UNID = UNID;
    }

    //Collections *****************************

    public Set<PersonContact> getContacts() {
        return contacts;
    }

    public void setContacts(Set<PersonContact> contacts) {
        this.contacts = contacts;
    }

    public boolean addToContacts(final PersonContact contact){
        if(contacts == null){
            this.contacts = new HashSet<PersonContact>(1);
        }
        return this.contacts.add(contact);
    }

    private static final long serialVersionUID = -7649892958713448678L;


}