package ru.entity.model.enums;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

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
        return obj != null && obj instanceof DictionaryEntity && getValue().equals(((DictionaryEntity) obj).getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

}