package ru.entity.model.mapped;


import ru.entity.model.referenceBook.Role;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@MappedSuperclass
public abstract class AccessControlledDocumentEntity extends DocumentEntity {

    /**
     * Уровень допуска
     * XXX: @AssociationOverride
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userAccessLevel_id", nullable = false)
    private UserAccessLevel userAccessLevel;

    /**
     * Пользователи-читатели
     * XXX: @AssociationOverride
     */
    @ManyToMany(fetch = FetchType.LAZY)
    protected Set<User> personReaders;

    /**
     * Пользователи-редакторы
     * XXX: @AssociationOverride
     */
    @ManyToMany(fetch = FetchType.LAZY)
    protected Set<User> personEditors;

    /**
     * Роли-читатели
     * XXX: @AssociationOverride
     */
    @ManyToMany(fetch = FetchType.LAZY)
    protected Set<Role> roleReaders;

    /**
     * Роли-редакторы
     * XXX: @AssociationOverride
     */
    @ManyToMany(fetch = FetchType.LAZY)
    protected Set<Role> roleEditors;


    public Set<User> getPersonReaders() {
        return personReaders;
    }

    public void setPersonReaders(Set<User> personReaders) {
        this.personReaders = personReaders;
    }

    public List<User> getPersonReadersList() {
        if (personReaders != null && !personReaders.isEmpty()) {
            return new ArrayList<>(personReaders);
        }
        return new ArrayList<>(0);
    }

    public Set<Role> getRoleReaders() {
        return roleReaders;
    }

    public void setRoleReaders(Set<Role> roleReaders) {
        this.roleReaders = roleReaders;
    }

    public List<Role> getRoleReadersList() {
        if (roleReaders != null && !roleReaders.isEmpty()) {
            return new ArrayList<>(roleReaders);
        }
        return new ArrayList<>(0);
    }

    public Set<Role> getRoleEditors() {
        return roleEditors;
    }

    public void setRoleEditors(Set<Role> roleEditors) {
        this.roleEditors = roleEditors;
    }

    public List<Role> getRoleEditorsList() {
        if (roleEditors == null || roleEditors.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(roleEditors);
        }
    }

    public Set<User> getPersonEditors() {
        return personEditors;
    }

    public void setPersonEditors(Set<User> personEditors) {
        this.personEditors = personEditors;
    }

    public List<User> getPersonEditorsList() {
        if (personEditors == null || personEditors.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(personEditors);
        }
    }

    public UserAccessLevel getUserAccessLevel() {
        return userAccessLevel;
    }

    public void setUserAccessLevel(UserAccessLevel userAccessLevel) {
        this.userAccessLevel = userAccessLevel;
    }
}
