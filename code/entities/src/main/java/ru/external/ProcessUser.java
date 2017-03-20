package ru.external;

import ru.entity.model.user.Role;

import java.io.Serializable;
import java.util.Set;

public interface ProcessUser extends Serializable{

    Integer getId();

    String getEmail();

    Set<Role> getRoles();

}