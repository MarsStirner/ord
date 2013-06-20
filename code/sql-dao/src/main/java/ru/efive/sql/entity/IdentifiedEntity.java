package ru.efive.sql.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Сущность, имеющая идентификатор
 */
@MappedSuperclass
public class IdentifiedEntity extends AbstractEntity {

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IdentifiedEntity other = (IdentifiedEntity) obj;
        if (getId() != other.getId())
            return false;
        return true;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private static final long serialVersionUID = -5373498855516075305L;
}