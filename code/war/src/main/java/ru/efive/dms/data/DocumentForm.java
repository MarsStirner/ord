package ru.efive.dms.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import ru.efive.sql.entity.DictionaryEntity;
import ru.efive.sql.entity.user.Role;

@Entity
@Table(name = "documents_forms")
public class DocumentForm extends DictionaryEntity {

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof DocumentForm)) {
            return false;
        }
        return getValue().equals(((DocumentForm) obj).getValue());
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

    public Set<Role> getRoleReaders() {
        return roleReaders;
    }

    public void setRoleReaders(Set<Role> roleReaders) {
        this.roleReaders = roleReaders;
    }

    @Transient
    public List<Role> getRoleReadersList() {
        List<Role> result = new ArrayList<Role>();
        if (roleReaders != null) {
            result.addAll(roleReaders);
        }
        return result;
    }

    /**
     * Категория видов документа
     */
    private String category;

    /**
     * Описание вида документа
     */
    private String description;


    /**
     * Роли-читатели
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(name = "dms_documet_forms_role_readers")
    private Set<Role> roleReaders;

    private static final long serialVersionUID = 7284023695000048879L;
}