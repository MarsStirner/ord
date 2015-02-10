package ru.entity.model.document;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Nastya Peshekhonova
 */
@Entity
@Table(name = "dms_type")
public class Type extends IdentifiedEntity {
    private String name;

    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_status_type",
            joinColumns = {@JoinColumn(name = "type_id")},
            inverseJoinColumns = {@JoinColumn(name = "status_id")})
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<Status> statuses;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(Set<Status> statuses) {
        this.statuses = statuses;
    }

}
