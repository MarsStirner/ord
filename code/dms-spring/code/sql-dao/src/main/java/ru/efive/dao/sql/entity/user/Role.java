package ru.efive.sql.entity.user;

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.enums.RoleType;

import javax.persistence.*;

/**
 * Роль пользователя системы
 */
@Entity
@Table(name = "dms_system_roles")
public class Role extends IdentifiedEntity {

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRoleType(RoleType roleType) {
		this.roleType = roleType;
	}

	public RoleType getRoleType() {
		return roleType;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
        if( !( obj instanceof Role ) ) {
            return false;
        }
        return getName().equals( ((Role)obj).getName() ) && getRoleType().equals(((Role)obj).getRoleType());
    }
	
	@Override
    public int hashCode() {
        return getName().hashCode() + getRoleType().ordinal();
    }

	/** название */
	private String name;
	
	/** тип права доступа */
	@Enumerated(value = EnumType.STRING)
	private RoleType roleType;
	
	/**
	 * email
	 */
	private String email;

	private static final long serialVersionUID = -4121985925621903659L;
}