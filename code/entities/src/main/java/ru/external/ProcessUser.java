package ru.external;

import ru.entity.model.user.Role;

import java.io.Serializable;
import java.util.Set;

public interface ProcessUser extends Serializable{

    public int getId();

    public String getEmail();

    public Set<Role> getRoles();

}