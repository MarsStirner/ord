package ru.entity.model.referenceBook;



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
@Table(name = "rbDepartment")
public class Department extends DictionaryEntity {
    public Department() {
    }

    /**
     * code = UUID
     */
}
