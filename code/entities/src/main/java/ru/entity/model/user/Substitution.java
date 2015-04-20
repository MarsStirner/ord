package ru.entity.model.user;

import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.*;
import java.util.Date;

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
public class Substitution extends IdentifiedEntity{

    @Column(name = "startDate")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "endDate")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name="deleted", nullable = false)
    private boolean deleted;

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

    public Substitution(Date startDate, Date endDate, User person, User substitution, int type) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.person = person;
        this.substitution = substitution;
        this.type = type;
    }
    //Getters & Setters

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Substitution[");
        sb.append("id=").append(getId());
        sb.append(", startDate=").append(startDate);
        sb.append(", endDate=").append(endDate);
        sb.append(", deleted=").append(deleted);
        sb.append(", person=");
        if(person != null) {
            sb.append('[').append(person.getId()).append(' ').append(person.getDescription()).append(']');
        } else {
            sb.append("NULL");
        }
        sb.append(", substitution=");
        if(substitution != null) {
            sb.append('[').append(substitution.getId()).append(' ').append(substitution.getDescription()).append(']');
        } else {
            sb.append("NULL");
        }
        sb.append(", type=").append(type);
        sb.append(']');
        return sb.toString();
    }
}
