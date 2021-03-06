package ru.entity.model.referenceBook;



import ru.entity.model.mapped.DictionaryEntity;

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
public class ContactInfoType extends DictionaryEntity {

    public static final String RB_CODE_MOBILE_PHONE = "mobilePhone";
    public static final String RB_CODE_WORK_PHONE = "workPhone";
    public static final String RB_CODE_PHONE = "phone";
    public static final String RB_CODE_INTERNAL_PHONE = "internalPhone";
    public static final String RB_CODE_EMAIL = "email";

    /**
     * Шаблон ввода контактной информации \ ?имя шаблона?
     */
    @Column(name = "valueMask", nullable = true)
    private String valueMask;

    //**************************************************** End OF DB Fields

    public ContactInfoType() {
    }

    public ContactInfoType(String name, String code) {
        this.setValue(name);
        this.code = code;
    }

    public ContactInfoType(String name, String code, String valueMask) {
        this.setValue(name);
        this.code = code;
        this.valueMask = valueMask;
    }


    //GET&SET

    public String getValueMask() {
        return valueMask;
    }

    public void setValueMask(String valueMask) {
        this.valueMask = valueMask;
    }
}
