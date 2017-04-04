package ru.entity.model.user;

import ru.entity.model.mapped.DeletableEntity;

import javax.persistence.*;
import java.time.LocalDate;



/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 18:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Entity
@Table(name = "user_substitutions")
@NamedQueries({
        @NamedQuery(name = "Substitutions.getAll", query = "SELECT a FROM Substitution a")
})
public class Substitution extends DeletableEntity {

    @Column(name = "startDate")
    private LocalDate startDate;

    @Column(name = "endDate")
    @Temporal(TemporalType.DATE)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "person_id", nullable = false)
    private User person;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "substitution_id", nullable = false)
    private User substitution;

    @Column(name = "type", nullable = false)
    private int type;

    public Substitution() {
    }

    public Substitution(LocalDate startDate, LocalDate endDate, User person, User substitution, int type) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.person = person;
        this.substitution = substitution;
        this.type = type;
    }
    //Getters & Setters

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public User getPerson() {
        return person;
    }

    public void setPerson(User person) {
        this.person = person;
    }

    public User getSubstitution() {
        return substitution;
    }

    public void setSubstitution(User substitution) {
        this.substitution = substitution;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Substitution[");
        sb.append("id=").append(getId());
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", deleted=").append(deleted);
        sb.append(", person=");
        if (person != null) {
            sb.append('[').append(person.getId()).append(' ').append(person.getDescription()).append(']');
        } else {
            sb.append("NULL");
        }
        sb.append(", substitution=");
        if (substitution != null) {
            sb.append('[').append(substitution.getId()).append(' ').append(substitution.getDescription()).append(']');
        } else {
            sb.append("NULL");
        }
        sb.append(", type=").append(type);
        sb.append(']');
        return sb.toString();
    }
}
