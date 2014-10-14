package ru.external;

import java.io.Serializable;
import java.util.Set;

import ru.entity.model.user.Role;

public interface ProcessUser extends Serializable {

    public int getId();

    public String getFullName();

    public String getEmail();

    public Set<Role> getRoles();

}