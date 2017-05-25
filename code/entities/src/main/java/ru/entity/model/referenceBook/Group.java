package ru.entity.model.referenceBook;


import ru.entity.model.mapped.ReferenceBookEntity;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Группа пользователей
 */
@Entity
@Table(name = "groups")
public class Group extends ReferenceBookEntity {

    public static final java.lang.String RB_CODE_MANAGERS = "MANAGERS";
    /**
     * Автор документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;


    /**
     * пользователи
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "mmUserToGroup",
            joinColumns = {@JoinColumn(name = "group_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> members;

    /**
     * Категория
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private GroupType type;

    public List<User> getMembersList() {
        List<User> result = new ArrayList<>();
        if (members != null) {
            result.addAll(members);
        }
        Collections.sort(result);
        return result;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public GroupType getType() {
        return type;
    }

    public void setType(final GroupType type) {
        this.type = type;
    }
}