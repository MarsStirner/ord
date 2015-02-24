package ru.entity.model.document;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "documents_forms")
public class DocumentForm extends DictionaryEntity {

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj instanceof DocumentForm && getValue().equals(((DocumentForm) obj).getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Категория видов документа
     */
    private String category;

    /**
     * Описание вида документа
     */
    private String description;


    //TODO а вот давай забацай и везде используй
    /**
     * Роли-читатели

    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "mmRolesToDocumentForms")
    private Set<Role> roleReaders;
     */
    private static final long serialVersionUID = 7284023695000048879L;
}