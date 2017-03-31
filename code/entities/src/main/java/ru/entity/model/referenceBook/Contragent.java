package ru.entity.model.referenceBook;

import ru.entity.model.mapped.ReferenceBookEntity;
import ru.util.Descriptionable;

import javax.persistence.*;


/**
 * Контрагент
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "rbContragent")
public class Contragent extends ReferenceBookEntity implements Descriptionable {


    private static final long serialVersionUID = 1123233260758669450L;
    /**
     * Краткое наименование
     */
    @Column(name = "shortName")
    private String shortName;
    /**
     * Тип контрагента
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private ContragentType type;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS & SETTERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Contragent() {
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

    /**
     * Получить полное описание сущности
     *
     * @return строка с полным описанием сущности
     */
    @Override
    public String getDescription() {
        return toString();
    }

    /**
     * Получить краткое описание сущности
     *
     * @return строка с кратким описанием сущности
     */
    @Override
    public String getDescriptionShort() {
        return getShortName();
    }
}