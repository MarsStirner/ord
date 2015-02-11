package ru.entity.model.crm;

import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.*;


/**
 * Контрагент
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "contragents")
public class Contragent extends IdentifiedEntity {

    /**
     * Удален ли документ
     */
    @Column(name="deleted")
    private boolean deleted;

    /**
     * Полное наименование
     */
    @Column(name="fullName")
    private String fullName;

    /**
     * Краткое наименование
     */
    @Column(name="shortName")
    private String shortName;

    /**
     * Номенклатура
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id")
    private ContragentType type;

    public Contragent() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS & SETTERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public ContragentType getType() {
        return type;
    }

    public void setType(ContragentType type) {
        this.type = type;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    private static final long serialVersionUID = 1123233260758669450L;
}