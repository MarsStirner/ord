package ru.entity.model.referenceBook;

import org.apache.commons.lang3.StringUtils;
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
@Table(name = "wf_action")
public class Action extends ReferenceBookEntity implements Comparable<Action> {

    /**
     * тип права доступа
     */
    @Transient
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

    public RoleType getRoleType() {
        if(roleType == null && StringUtils.isNotEmpty(code)){
            roleType = RoleType.valueOf(code);
        }
        return roleType;
    }

    public Set<User> getPersons() {
        return persons;
    }

    public void setPersons(Set<User> persons) {
        this.persons = persons;
    }

    @Override
    public int compareTo(@NotNull Action o) {
        return o.getId() - getId();
    }
}