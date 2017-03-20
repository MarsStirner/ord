package ru.entity.model.user;

import ru.entity.model.enums.RoleType;
import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Роль пользователя системы
 */
@Entity
@Table(name = "dms_system_roles")
public class Role extends IdentifiedEntity implements Comparable<Role>{

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
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_system_person_roles",
            joinColumns = @JoinColumn(name="role_id"),
            inverseJoinColumns = @JoinColumn(name="person_id")
    )
    private Set<User> persons;

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
        if (persons != null && !persons.isEmpty()) {
            List<User> result = new ArrayList<>(persons);
            Collections.sort(result);
            return result;
        }
        return new ArrayList<>(0);
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
        return getId();
    }


    @Override
    public int compareTo(@NotNull Role o) {
        return o.getId() - getId();
    }
}