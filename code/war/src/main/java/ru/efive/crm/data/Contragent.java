package ru.efive.crm.data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import ru.efive.sql.entity.IdentifiedEntity;

/**
 * Контрагент
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "contragents")
public class Contragent extends IdentifiedEntity {

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

    public ContragentNomenclature getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(ContragentNomenclature nomenclature) {
        this.nomenclature = nomenclature;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }


    /**
     * Полное наименование
     */
    private String fullName;

    /**
     * Краткое наименование
     */
    private String shortName;

    /**
     * Номенклатура
     */
    @OneToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "nomenclature_id")
    private ContragentNomenclature nomenclature;

    /**
     * Удален ли документ
     */
    private boolean deleted;

    private static final long serialVersionUID = 1123233260758669450L;
}