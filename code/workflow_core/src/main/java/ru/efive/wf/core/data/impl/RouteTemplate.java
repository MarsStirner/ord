package ru.efive.wf.core.data.impl;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import ru.efive.sql.entity.document.Document;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.wf.core.data.HumanTaskTree;

/**
 * Настройка маршрута
 */
@Entity
@Table(name = "wf_route_templates")
public class RouteTemplate extends Document {

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTaskTree(HumanTaskTree taskTree) {
        this.taskTree = taskTree;
    }

    public HumanTaskTree getTaskTree() {
        return taskTree;
    }

    public List<User> getReaders() {
        return readers;
    }

    public void setReaders(List<User> readers) {
        this.readers = readers;
    }

    public List<Role> getReaderRoles() {
        return readerRoles;
    }

    public void setReaderRoles(List<Role> readerRoles) {
        this.readerRoles = readerRoles;
    }

    public List<User> getEditors() {
        return editors;
    }

    public void setEditors(List<User> editors) {
        this.editors = editors;
    }

    public List<Role> getEditorRoles() {
        return editorRoles;
    }

    public void setEditorRoles(List<Role> editorRoles) {
        this.editorRoles = editorRoles;
    }


    private String name;

    /**
     * Дерево согласования
     */
    @OneToOne(fetch = FetchType.EAGER)
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    private HumanTaskTree taskTree;

    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "wf_route_template_readers",
            joinColumns = {@JoinColumn(name = "template_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<User> readers;

    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "wf_route_template_reader_roles",
            joinColumns = {@JoinColumn(name = "template_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Role> readerRoles;

    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "wf_route_template_editors",
            joinColumns = {@JoinColumn(name = "template_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<User> editors;

    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "wf_route_template_editor_roles",
            joinColumns = {@JoinColumn(name = "template_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private List<Role> editorRoles;


    private static final long serialVersionUID = 9039334783680513371L;
}