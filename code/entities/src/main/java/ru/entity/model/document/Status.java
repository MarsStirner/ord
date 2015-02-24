package ru.entity.model.document;

import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Nastya Peshekhonova
 */
//TODO и где блеать таблица??!
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
