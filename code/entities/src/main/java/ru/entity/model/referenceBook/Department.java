package ru.entity.model.referenceBook;


import ru.entity.model.mapped.ReferenceBookEntity;

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
public class Department extends ReferenceBookEntity {
    public Department() {
    }

    /**
     * code = UUID
     */
}
