package ru.entity.model.referenceBook;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "rbRegion")
public class Region extends DictionaryEntity {
    /**
     * Категория
     */
    @Column(name="category ")
    private String category;

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}