package ru.efive.dms.data;

import javax.persistence.Entity;
import javax.persistence.Table;

import ru.efive.sql.entity.DictionaryEntity;

@Entity
@Table(name = "dms_delivery_types")
public class DeliveryType extends DictionaryEntity {
	
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
	
	private static final long serialVersionUID = -3467238783774380876L;
}