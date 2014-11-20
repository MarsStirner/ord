package ru.entity.model.user;

import ru.entity.model.mapped.AbstractEntity;

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
public class Substitution extends AbstractEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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
}
