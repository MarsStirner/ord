package ru.entity.model.user;


import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Upatov Egor <br>
 * Date: 19.08.2014, 16:22 <br>
 * Company: Korus Consulting IT <br>
 * Description: Справочник должностей<br>
 */
@Entity
@Table(name = "rbPosition")
public class Position extends DictionaryEntity {
    @Override
    public String toString(){
        return getValue();
    }
}
