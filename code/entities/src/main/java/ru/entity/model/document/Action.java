package ru.entity.model.document;


import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Nastya Peshekhonova
 */
@Entity
@Table(name = "dms_action")
public class Action extends IdentifiedEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
