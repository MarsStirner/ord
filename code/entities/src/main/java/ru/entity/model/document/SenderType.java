package ru.entity.model.document;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;



@Entity
@Table(name = "dms_sender_types")
public class SenderType extends DictionaryEntity {

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof SenderType && getValue().equals(((SenderType) obj).getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    /**
     * Категория типа отправителей документа
     */
    private String category;

    /**
     * Описание типа отправителя документа
     */
    private String description;

    private static final long serialVersionUID = -3467238783774380876L;
}