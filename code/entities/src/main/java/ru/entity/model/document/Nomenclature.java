package ru.entity.model.document;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Номенклатура документа
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "rbNomenclature")
public class Nomenclature extends DictionaryEntity {

    /**
     * Категория номенклатуры
     */
    @Column(name="code", unique = true)
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private static final long serialVersionUID = 7864611988027689367L;
}
