package ru.entity.model.document;

import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.external.ProcessUser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Person extends User implements ProcessUser {
    private Set<Role> totalRoles;

    @Override
    public Set<Role> getRoles(){
        return totalRoles;
    }


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
        totalRoles = new HashSet<Role>(user.getRoles());
    }

    public Person(User user, List<User> substitutedUsers) {
        setCreated(user.getCreated());
        setDeleted(user.isDeleted());
        setEmail(user.getEmail());
        setFirstName(user.getFirstName());
        setId(user.getId());
        setLastName(user.getLastName());
        setLogin(user.getLogin());
        setMiddleName(user.getMiddleName());
        setPassword(user.getPassword());
        totalRoles = new HashSet<Role>(user.getRoles());
        for(User currentUser : substitutedUsers){
           totalRoles.addAll(currentUser.getRoles());
        }
    }

    private static final long serialVersionUID = -6200882661231446726L;
}