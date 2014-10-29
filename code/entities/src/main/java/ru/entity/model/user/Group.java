package ru.entity.model.user;



import javax.persistence.*;

import org.hibernate.annotations.Type;
import ru.entity.model.enums.GroupType;
import ru.entity.model.mapped.DictionaryEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Группа пользователей
 */
@Entity
@Table(name = "groups")
public class Group extends DictionaryEntity {

    public List<User> getMembersList() {

        List<User> result = new ArrayList<User>();
        if (members != null) {
            result.addAll(members);
        }
        Collections.sort(result, new Comparator<User>() {
            @Override
            public int compare(User o1, User o2) {
                return o1.getFullName().compareTo(o2.getFullName());
            }
        });
        return result;
    }

    public List<User> getMembersListWithEmptyUser() {
        List<User> result = getMembersList();
        User empty = new User();
        empty.setLastName("");
        empty.setFirstName("");
        empty.setMiddleName("");
        result.add(0, empty);
        return result;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
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


    /**
     * пользователи
     */
    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @JoinTable(name = "groups_dms_system_persons",
            joinColumns = {@JoinColumn(name = "groups_id")},
            inverseJoinColumns = {@JoinColumn(name = "members_id")})
    @Type(type = "java.util.Set")
    private Set<User> members = new HashSet<User>();

    /**
     * Категория
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "categoryId")
    private GroupType category;

    /**
     * Описание
     */
    private String description;

    /**
     * Алиас
     */
    private String alias;

    /**
     * Автор документа
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "group_authors")
    private User author;

    private static final long serialVersionUID = 6366882739076305392L;
}