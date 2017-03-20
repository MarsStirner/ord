package ru.entity.model.mapped;


import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Запись справочника (ID + deleted + value + code)
 */
@MappedSuperclass
public abstract class DictionaryEntity extends DeletableEntity {

    /**
     * Описание значения справочника
     */
    @Column(name = "value", nullable = false)
    protected String value;
    /*
    Уникальный код записи справочника
     */
    @Column(name = "code", nullable = false, unique = true)
    protected String code;

    /**
     * Конструктор по умолчанию.
     */
    public DictionaryEntity() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + code.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final DictionaryEntity that = (DictionaryEntity) o;

        return code.equals(that.code);

    }

    @Override
    public String toString() {
       return value;
    }

    public String toLogString(){
        String sb = getClass().getSimpleName() + '{' +
                "value='" + value + '\'' +
                ", code='" + code + '\'' +
                " ID[" + id + "]}";
        return sb;
    }


}