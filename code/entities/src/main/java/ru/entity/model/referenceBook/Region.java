package ru.entity.model.referenceBook;

import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "rbRegion")
public class Region extends ReferenceBookEntity {
    /**
     * Категория
     */
    @Column(name = "category ")
    private String category;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}