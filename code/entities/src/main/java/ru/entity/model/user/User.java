package ru.entity.model.user;

import org.apache.commons.lang.StringUtils;
import ru.entity.model.document.Nomenclature;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.util.ApplicationHelper;
import ru.util.Descriptionable;
import ru.util.StoredCodes;

import javax.persistence.*;
import java.util.*;

/**
 * Пользователь системы
 */
@Entity
@Table(name = "dms_system_persons")
public class User extends IdentifiedEntity implements Descriptionable, Comparable<User>{

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
    @Column(name = "email")
    private String email;

    /**
     * Табельный номер сотрудника
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maxUserAccessLevel_id")
    private UserAccessLevel maxUserAccessLevel;

    /**
     * Текущий уровень допуска
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currentUserAccessLevel_id")
    private UserAccessLevel currentUserAccessLevel;

    /**
     * Контактные данные пользователя (почта, телефон, итд)
     * При сохранении пользователя - добавлять\удалять и обновлять записи
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "person", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private Set<PersonContact> contacts;

    /**
     * группы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="groups_dms_system_persons",
            joinColumns = {@JoinColumn(name="members_id")},
            inverseJoinColumns = {@JoinColumn(name="groups_id")}
    )
    private Set<Group> groups;


    /**
     * роли
     */
    @ManyToMany(fetch = FetchType.LAZY)
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

    @Column(name="jobPosition")
    private String jobPositionString;

    /**
     * подразделение
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobDepartment_id", nullable = true)
    private Department jobDepartment;

    @Column(name="jobDepartment")
    private String jobDepartmentString;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="defaultNomeclature_id", nullable = true)
    private Nomenclature defaultNomenclature;

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

    /**
     * Выставляет пароль, преобразуя его в хеш
     * @param password  пароль, хэш которого надо сохранить в модель
     */
    public void setPassword(String password) {
        this.password = ApplicationHelper.getMD5(password);
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
        Collections.sort(result);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Descriptionable
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получение полного ФИО в виде строки
     * @return Строка с полным ФИО
     * "Иванов Иван Иванович"
     * если нету части ФИО - то выводится без нее
     * "Иван Иванович"\"Иванов Иван"\"Иванов Иванович"\"Иван"\"Иванов"\""
     */
    @Override
    public String getDescription() {
        final StringBuilder sb = new StringBuilder();
        if(StringUtils.isNotEmpty(lastName)){
            sb.append(lastName);
        }
        if(StringUtils.isNotEmpty(firstName)){
            if(sb.length()!=0){
                sb.append(' ');
            }
            sb.append(firstName);
        }
        if(StringUtils.isNotEmpty(middleName)){
            if(sb.length()!=0){
                sb.append(' ');
            }
            sb.append(middleName);
        }
        return sb.toString();
    }

    /**
     * краткая форма полного имени
     */
    @Override
    public String getDescriptionShort() {
        final StringBuilder sb = new StringBuilder();
        if(StringUtils.isNotEmpty(lastName)){
            sb.append(lastName);
        }
        if(StringUtils.isNotEmpty(firstName)){
            if(sb.length()!=0){
                sb.append(' ');
            }
            sb.append(firstName.charAt(0)).append('.');
        }
        if(StringUtils.isNotEmpty(middleName)){
            if(sb.length()!=0){
                sb.append(' ');
            }
            sb.append(middleName.charAt(0)).append('.');
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Comparable<User>
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public int compareTo(User o) {
        return getDescription().compareTo(o.getDescription());
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

    @Transient
    public boolean isFilling() {
        for (Role role : roles) {
            if (StoredCodes.RoleType.FILLING.equals(role.getRoleType().name())) {
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

    public Nomenclature getDefaultNomenclature() {
        return defaultNomenclature;
    }

    public void setDefaultNomenclature(Nomenclature defaultNomenclature) {
        this.defaultNomenclature = defaultNomenclature;
    }

    public String getJobPositionString() {
        return jobPositionString;
    }

    public void setJobPositionString(final String jobPositionString) {
        this.jobPositionString = jobPositionString;
    }

    public String getJobDepartmentString() {
        return jobDepartmentString;
    }

    public void setJobDepartmentString(final String jobDepartmentString) {
        this.jobDepartmentString = jobDepartmentString;
    }

    //Collections *****************************

    public List<PersonContact> getContacts() {return new ArrayList<PersonContact>(contacts);}

    public String getContact(final String type){
        final StringBuilder sb = new StringBuilder();
        for(PersonContact contact : contacts){
            if(contact.getType().getCode().equalsIgnoreCase(type)){
                if(sb.length() != 0){
                    sb.append(", ");
                }
                sb.append(contact.getValue());
            }
        }
        return sb.toString();
    }

    public void setContacts(List<PersonContact> contacts) {
        this.contacts = new HashSet<PersonContact>(contacts);
    }

    public boolean addToContacts(final PersonContact contact) {
        if (contacts == null) {
            contacts = new HashSet<PersonContact>(1);
        }
        return contacts.add(contact);
    }

    public boolean addToContacts(final Collection<PersonContact> newContacts) {
        if (contacts == null) {
            contacts = new HashSet<PersonContact>(newContacts.size());
        }
        return contacts.addAll(newContacts);
    }

    private static final long serialVersionUID = -7649892958713448678L;

    /**
     * Объекты равны, когда оба объекта - Пользователи и у них одинаковый идентификатор
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof User)) {
            return false;
        }
        return getId() == ((User) obj).getId();
    }

    /**
     * Принять сотрудника на работу начиная от даты !не меняет содержимое БД, только модель!
     * @param date дата приема на работу
     */
    public void hire(final Date date) {
        fired = false;
        lastModified = date;
    }

    /**
     * Уволить сотрудника с даты !не меняет содержимое БД, только модель!
     * @param date дата увольнения
     */
    public void fire(final Date date) {
       fired = true;
       firedDate = date;
       lastModified = date;
    }
}