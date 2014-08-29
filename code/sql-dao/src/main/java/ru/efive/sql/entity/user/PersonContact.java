package ru.efive.sql.entity.user;

import ru.efive.sql.entity.IdentifiedEntity;

import javax.persistence.*;

/**
 * Author: Upatov Egor <br>
 * Date: 18.08.2014, 19:29 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Entity
@Table(name = "dms_system_user_contact")
public class PersonContact extends IdentifiedEntity{

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name="person", nullable = false)
    private User person;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "type", nullable = false)
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
}
