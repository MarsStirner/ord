package ru.entity.model.mapped;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Author: Upatov Egor <br>
 * Date: 01.06.2015, 20:55 <br>
 * Company: Korus Consulting IT <br>
 * Description: Суперкласс, наследующий от Identified и имеющий поле в БД - deleted (boolean NOT NULL)<br>
 */
@MappedSuperclass
public abstract class DeletableEntity extends IdentifiedEntity {

    @Column(name = "deleted", nullable = false)
    protected boolean deleted;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }
}
