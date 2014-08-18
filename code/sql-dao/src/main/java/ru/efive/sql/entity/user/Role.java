package ru.efive.sql.entity.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.enums.RoleType;

/**
 * Роль пользователя системы
 */
@Entity
@Table(name = "dms_system_roles")
public class Role extends IdentifiedEntity {

    private static final long serialVersionUID = -4121985925621903659L;

    /**
     * название
     */
    private String name;

    /**
     * тип права доступа
     */
    @Enumerated(value = EnumType.STRING)
    private RoleType roleType;

    /**
     * пользователи
     */
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "roles")
    @Type(type = "java.util.Set")
    @Fetch(value = FetchMode.SELECT)
    private Set<User> persons = new HashSet<User>();

    /**
     * email
     */
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getName();
    }

    public List<User> getPersonList() {

        List<User> result = new ArrayList<User>();
        if (persons != null) {
            result.addAll(persons);
        }
        Collections.sort(result, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getFullName().compareTo(o2.getFullName());
            }
        });
        return result;
    }

    public Set<User> getPersons() {
        return persons;
    }

    public void setPersons(Set<User> persons) {
        this.persons = persons;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Role)) {
            return false;
        }
        return getName().equals(((Role) obj).getName()) && getRoleType().equals(((Role) obj).getRoleType());
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getRoleType().ordinal();
    }
}