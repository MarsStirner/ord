package ru.entity.model.user;


import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.*;

/**
 * Author: Upatov Egor <br>
 * Date: 18.08.2014, 19:29 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Entity
@Table(name = "dms_system_user_contact")
public class PersonContact extends IdentifiedEntity {

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private User person;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private RbContactInfoType type;

    @Column(name = "value", nullable = true)
    private String value;

    public PersonContact() {
    }

    public PersonContact(User person, RbContactInfoType type, String value) {
        this.person = person;
        this.type = type;
        this.value = value;
    }

    public User getPerson() {
        return person;
    }

    public void setPerson(User person) {
        this.person = person;
    }

    public RbContactInfoType getType() {
        return type;
    }

    public void setType(RbContactInfoType type) {
        this.type = type;
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

        final PersonContact that = (PersonContact) o;

        if (!person.equals(that.person)) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }
        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + person.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
