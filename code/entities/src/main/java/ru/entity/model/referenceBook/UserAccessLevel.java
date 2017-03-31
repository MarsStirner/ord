package ru.entity.model.referenceBook;

import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "rbUserAccessLevel")
public class UserAccessLevel extends ReferenceBookEntity {
    /**
     * Числовая характеристика уровня допуска
     */
    @Column(name = "level")
    private int level;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

}