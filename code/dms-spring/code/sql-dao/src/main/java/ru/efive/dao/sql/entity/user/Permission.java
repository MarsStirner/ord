package ru.efive.sql.entity.user;

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.enums.PermissionType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * Права доступа
 */
@Entity
@Table(name = "e5_system_permissions")
public class Permission extends IdentifiedEntity {

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PermissionType getPermissionType() {
		return permissionType;
	}

	public void setPermissionType(PermissionType permissionType) {
		this.permissionType = permissionType;
	}

	/** название */
	private String name;

	/** тип права доступа */
	@Enumerated(value = EnumType.STRING)
	private PermissionType permissionType;

	private static final long serialVersionUID = 5700077577266584372L;
}