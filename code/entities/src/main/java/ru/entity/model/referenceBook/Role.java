package ru.entity.model.referenceBook;

import ru.entity.model.enums.RoleType;
import ru.entity.model.mapped.ReferenceBookEntity;
import ru.entity.model.user.User;

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
@Table(name = "rbRole")
public class Role extends ReferenceBookEntity implements Comparable<Role> {

    /**
     * тип права доступа
     */
    @Column(name = "roleType")
    @Enumerated(value = EnumType.STRING)
    private RoleType roleType;

    /**
     * пользователи
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_system_person_roles",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    private Set<User> persons;

    /**
     * email
     */
    private String email;

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
    public int compareTo(@NotNull Role o) {
        return o.getId() - getId();
    }
}