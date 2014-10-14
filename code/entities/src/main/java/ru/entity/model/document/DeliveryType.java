package ru.entity.model.document;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "dms_delivery_types")
public class DeliveryType extends DictionaryEntity {

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

    private static final long serialVersionUID = -3467238783774380876L;
}