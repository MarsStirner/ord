package ru.entity.model.mapped;


import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Запись справочника
 */
@MappedSuperclass
public class DictionaryEntity extends IdentifiedEntity {
    /**
     * значение
     */
    @Column(name="value")
    private String value;

    /**
     * true - удалён, false или null - не удалён
     */
    @Column(name="deleted", nullable = false)
    private boolean deleted;

    /**
     * Конструктор по умолчанию.
     */
    public DictionaryEntity() {

    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    private static final long serialVersionUID = -8239024131091899733L;
}