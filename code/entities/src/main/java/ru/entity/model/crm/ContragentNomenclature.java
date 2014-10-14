package ru.entity.model.crm;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Номенклатура контрагента
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "contragents_id_by_nomenclature")
public class ContragentNomenclature extends DictionaryEntity {

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


    /**
     * Категория номенклатуры
     */
    private String category;

    /**
     * Описание номенклатуры
     */
    private String description;

    private static final long serialVersionUID = 7864611988027689367L;
}
