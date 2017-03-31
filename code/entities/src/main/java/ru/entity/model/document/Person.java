package ru.entity.model.document;

import ru.entity.model.referenceBook.Role;
import ru.entity.model.user.User;
import ru.external.ProcessUser;

import java.util.HashSet;
import java.util.Set;

public class Person extends User implements ProcessUser {
    private static final long serialVersionUID = -6200882661231446726L;
    private Set<Role> totalRoles;


    public Person(User user) {
        setCreated(user.getCreated());
        setDeleted(user.isDeleted());
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setId(user.getId());
        setLastName(user.getLastName());
        setLogin(user.getLogin());
        setMiddleName(user.getMiddleName());
        setPassword(user.getPassword());
        totalRoles = new HashSet<>(user.getRoles());
    }

    public Person(User user, Set<User> substitutedUsers) {
        setCreated(user.getCreated());
        setDeleted(user.isDeleted());
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setId(user.getId());
        setLastName(user.getLastName());
        setLogin(user.getLogin());
        setMiddleName(user.getMiddleName());
        setPassword(user.getPassword());
        totalRoles = new HashSet<>(user.getRoles());
        for (User currentUser : substitutedUsers) {
            totalRoles.addAll(currentUser.getRoles());
        }
    }

    @Override
    public Set<Role> getRoles() {
        return totalRoles;
    }
}