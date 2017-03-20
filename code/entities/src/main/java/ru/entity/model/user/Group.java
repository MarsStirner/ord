package ru.entity.model.user;


import ru.entity.model.mapped.DictionaryEntity;
import ru.entity.model.referenceBook.GroupType;

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
public class Group extends DictionaryEntity {

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
    @JoinTable(name = "mmPersonToGroup",
            joinColumns = {@JoinColumn(name = "group_id")},
            inverseJoinColumns = {@JoinColumn(name = "member_id")})
     private Set<User> members;

    /**
     * Категория
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupType_id")
    private GroupType groupType;

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

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getAuthor() {
        return author;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(final GroupType groupType) {
        this.groupType = groupType;
    }
}