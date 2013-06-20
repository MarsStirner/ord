package ru.efive.sql.entity.document;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.efive.sql.entity.IdentifiedEntity;

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

    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_action_type",
            joinColumns = {@JoinColumn(name = "type_id")},
            inverseJoinColumns = {@JoinColumn(name = "action_id")})
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<Action> actions;

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

    public Set<Action> getActions() {
        return actions;
    }

    public void setActions(Set<Action> actions) {
        this.actions = actions;
    }
}
