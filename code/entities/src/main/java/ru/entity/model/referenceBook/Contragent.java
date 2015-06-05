package ru.entity.model.referenceBook;

import ru.entity.model.mapped.DictionaryEntity;
import ru.util.Descriptionable;

import javax.persistence.*;


/**
 * Контрагент
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "rbContragent")
public class Contragent extends DictionaryEntity implements Descriptionable{


    /**
     * Краткое наименование
     */
    @Column(name="shortName")
    private String shortName;

    /**
     * Тип контрагента
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id")
    private ContragentType type;

    public Contragent() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS & SETTERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    private static final long serialVersionUID = 1123233260758669450L;

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