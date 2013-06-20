package ru.efive.sql.entity.document;

import ru.efive.sql.entity.IdentifiedEntity;

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
