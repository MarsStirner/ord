package ru.entity.model.user;



import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 16:24 <br>
 * Company: Korus Consulting IT <br>
 * Description: Справочник подразделений <br>
 */
@Entity
@Table(name = "dms_system_user_department")
public class Department extends DictionaryEntity {
    @Override
    public String toString(){
        return getValue();
    }
}
