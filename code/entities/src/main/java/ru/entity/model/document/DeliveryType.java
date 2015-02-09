package ru.entity.model.document;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Типы доставки документов
 */
@Entity
@Table(name = "dms_delivery_types")
public class DeliveryType extends DictionaryEntity {


    /**
     * Универсальный код типа доставки
     * @see ru.util.StoredCodes.DeliveryType
     */
    @Column(name="code")
    private String code;

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof DeliveryType && getValue().equals(((DeliveryType) obj).getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    private static final long serialVersionUID = -3467238783774380876L;
}