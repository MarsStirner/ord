package ru.efive.sql.entity.user;

import javax.persistence.Entity;
import javax.persistence.Table;

import ru.efive.sql.entity.DictionaryEntity;

@Entity
@Table(name = "user_access_levels")
public class UserAccessLevel extends DictionaryEntity {

	private static final long serialVersionUID = 2190610453672852499L;

	@Override
	public String toString() {
		return getValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
        if(!(obj instanceof UserAccessLevel)) {
            return false;
        }
        return getValue().equals( ((UserAccessLevel)obj).getValue() );
    }
	
	@Override
    public int hashCode() {
        return getValue().hashCode();
    }	
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}		

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	/**
	 * Числовая характеристика уровня допуска
	 */
	private int level;
	
	/**
	 * Описание уровня допуска
	 */
	private String description;
}