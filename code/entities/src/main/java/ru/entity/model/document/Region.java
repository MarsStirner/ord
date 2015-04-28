package ru.entity.model.document;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "kladr_regions")
public class Region extends DictionaryEntity {
    //TODO comparable
    private static final long serialVersionUID = -8907114855845335033L;

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof Region && getValue().equals(((Region) obj).getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

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
     * Категория
     */
    private String category;

    /**
     * Описание
     */
    private String description;


}