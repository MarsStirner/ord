package ru.entity.model.crm;

import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.*;

/**
 * Контакты
 * (контактные лица различных организаций (предположение))
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "contacts")
public class Contact extends IdentifiedEntity {
    /**
     * имя
     */
    @Column(name = "firstName")
    private String firstName;

    /**
     * фамилия
     */
    @Column(name = "lastName")
    private String lastName;

    /**
     * отчество
     */
    @Column(name = "middleName")
    private String middleName;

    /**
     * Организация
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contragent_id", nullable = true)
    private Contragent contragent;


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Contragent getContragent() {
        return contragent;
    }

    public void setContragent(Contragent contragent) {
        this.contragent = contragent;
    }


    /**
     * полное имя
     */
    @Transient
    public String getDescription() {
        return lastName + " " + (firstName != null && !firstName.equals("") ? firstName + " " : "") +
                (middleName != null && !middleName.equals("") ? middleName : "");
    }

    /**
     * краткая форма полного имени
     */
    @Transient
    public String getDescriptionShort() {
        return lastName + " " + (firstName != null && !firstName.equals("") ? firstName.substring(0, 1) + ". " : "") +
                (middleName != null && !middleName.equals("") ? middleName.substring(0, 1) + "." : "");
    }



    private static final long serialVersionUID = 2676252421021362937L;
}
