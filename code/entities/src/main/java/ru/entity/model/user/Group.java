package ru.entity.model.user;


import ru.entity.model.enums.GroupType;
import ru.entity.model.mapped.DictionaryEntity;

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
    @JoinTable(name = "groups_dms_system_persons",
            joinColumns = {@JoinColumn(name = "groups_id")},
            inverseJoinColumns = {@JoinColumn(name = "members_id")})
     private Set<User> members;

    /**
     * Категория
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoryId")
    private GroupType category;

    /**
     * Алиас
     */
    @Column(name="alias")
    private String alias;



    public List<User> getMembersList() {
        List<User> result = new ArrayList<User>();
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

    public void setCategory(GroupType category) {
        this.category = category;
    }

    public GroupType getCategory() {
        return category;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getAuthor() {
        return author;
    }

    private static final long serialVersionUID = 6366882739076305392L;
}