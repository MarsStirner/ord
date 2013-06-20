package ru.efive.sql.entity.enums;

import javax.persistence.Entity;
import javax.persistence.Table;

import ru.efive.sql.entity.DictionaryEntity;

@Entity
@Table(name = "dms_group_types")
public class GroupType extends DictionaryEntity {
	
	private static final long serialVersionUID = -4562473719426906910L;

	@Override
	public String toString() {
		return getValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
        if(!(obj instanceof DictionaryEntity)) {
            return false;
        }
        return getValue().equals( ((DictionaryEntity)obj).getValue() );
    }
	
	@Override
    public int hashCode() {
        return getValue().hashCode();
    }
		
}