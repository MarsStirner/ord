package ru.efive.sql.entity.user;

import ru.efive.sql.entity.DictionaryEntity;
import ru.efive.sql.entity.IdentifiedEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Upatov Egor <br>
 * Date: 18.08.2014, 18:51 <br>
 * Company: Korus Consulting IT <br>
 * Description: Справочник Типов контактной иформации (телефон мобильный \ телефон рабочий \ skype \ итд)<br>
 */
@Entity
@Table(name = "rbContactInfoType")
public class RbContactInfoType extends DictionaryEntity{

    /**
     * Наименование типа контактной информации  @Override(VALUE)
     */
    /**
     * Уникальный код справочника (для внутренних целей)
     */
    @Column(name = "code", nullable = false, unique = true)
    private String code;

    /**
     * Шаблон ввода контактной информации \ ?имя шаблона?
     */
    @Column(name = "valueMask", nullable = true)
    private String valueMask;

    //**************************************************** End OF DB Fields

    public RbContactInfoType() {
    }

    public RbContactInfoType(String name, String code) {
        this.setValue(name);
        this.code = code;
    }

    public RbContactInfoType(String name, String code, String valueMask) {
        this.setValue(name);
        this.code = code;
        this.valueMask = valueMask;
    }

    @Override
    public String toString(){
        return getValue();
    }

    //GET&SET

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValueMask() {
        return valueMask;
    }

    public void setValueMask(String valueMask) {
        this.valueMask = valueMask;
    }
}
