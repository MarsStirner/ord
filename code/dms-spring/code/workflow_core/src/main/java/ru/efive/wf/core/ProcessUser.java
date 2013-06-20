package ru.efive.wf.core;

import java.io.Serializable;
import java.util.Set;

import ru.efive.sql.entity.user.Role;

public interface ProcessUser extends Serializable {
	
	public int getId();
	public String getDescription();
	public String getEmail();
	public Set<Role> getRoles();
	
}