package ru.efive.dms.data;

import javax.persistence.Entity;
import javax.persistence.Table;

import ru.efive.sql.entity.DictionaryEntity;

@Entity
@Table(name = "kladr_regions")
public class Region extends DictionaryEntity {
    private static final long serialVersionUID = -8907114855845335033L;

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Region)) {
            return false;
        }
        return getValue().equals(((Region) obj).getValue());
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