package ru.entity.model.crm;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Upatov Egor <br>
 * Date: 10.02.2015, 18:27 <br>
 * Company: Korus Consulting IT <br>
 * Description: Справочник типов контрагентов<br>
 */
@Entity
@Table(name="rbContragentType")
public class ContragentType extends DictionaryEntity {

    @Column(name = "category", unique = true)
    private String category;

    public ContragentType() {
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
