package ru.entity.model.user;


import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.referenceBook.RbContactPointSystem;

import javax.persistence.*;

/**
 * Author: Upatov Egor <br>
 * Date: 18.08.2014, 19:29 <br>
 * Company: Korus Consulting IT <br>
 * See <a href="https://www.hl7.org/fhir/datatypes.html#ContactPoint">FHIR:ContactPoint</a>
 */

@Entity
@Table(name = "contactPoint")
public class ContactPoint extends IdentifiedEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "system_id", nullable = false)
    private RbContactPointSystem system;

    @Column(name = "value", nullable = true)
    private String value;

    public ContactPoint() {
    }

    public ContactPoint(User user, RbContactPointSystem type, String value) {
        this.user = user;
        this.system = type;
        this.value = value;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RbContactPointSystem getSystem() {
        return system;
    }

    public void setSystem(RbContactPointSystem system) {
        this.system = system;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final ContactPoint that = (ContactPoint) o;

        if (!user.equals(that.user)) {
            return false;
        }
        if (!system.equals(that.system)) {
            return false;
        }
        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + system.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
