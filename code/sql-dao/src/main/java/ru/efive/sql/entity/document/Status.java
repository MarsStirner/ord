package ru.efive.sql.entity.document;

import ru.efive.sql.entity.IdentifiedEntity;

import javax.persistence.*;

/**
 * @author Nastya Peshekhonova
 */
@Entity
@Table(name = "dms_status")
public class Status extends IdentifiedEntity {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Status(String name, int id) {
        this.name = name;
    }
}
