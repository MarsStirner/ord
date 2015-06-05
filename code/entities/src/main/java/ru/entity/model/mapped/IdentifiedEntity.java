package ru.entity.model.mapped;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

/**
 * Сущность, имеющая идентификатор
 */
@MappedSuperclass
public abstract class IdentifiedEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Integer id;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final IdentifiedEntity that = (IdentifiedEntity) o;
        return !(id != null ? !id.equals(that.id) : that.id != null);
    }

    private static final long serialVersionUID = -5373498855516075305L;
}