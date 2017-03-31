package ru.entity.model.wf;

import ru.entity.model.mapped.Document;
import ru.entity.model.referenceBook.Role;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Настройка маршрута
 */
@Entity
@Table(name = "wf_route_templates")
public class RouteTemplate extends Document {

    private static final long serialVersionUID = 9039334783680513371L;
    private String name;
    /**
     * Дерево согласования
     */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private HumanTaskTree taskTree;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "wf_route_template_readers",
            joinColumns = {@JoinColumn(name = "template_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> readers;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "wf_route_template_reader_roles",
            joinColumns = {@JoinColumn(name = "template_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})

    private Set<Role> readerRoles;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "wf_route_template_editors",
            joinColumns = {@JoinColumn(name = "template_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> editors;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "wf_route_template_editor_roles",
            joinColumns = {@JoinColumn(name = "template_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private Set<Role> editorRoles;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HumanTaskTree getTaskTree() {
        return taskTree;
    }

    public void setTaskTree(HumanTaskTree taskTree) {
        this.taskTree = taskTree;
    }

    public List<User> getReaders() {
        if (readers != null && !readers.isEmpty()) {
            return new ArrayList<>(readers);
        } else {
            return new ArrayList<>(0);
        }
    }

    public void setReaders(List<User> readers) {
        if (this.readers != null) {
            this.readers.clear();
            this.readers.addAll(readers);
        } else {
            this.readers = new HashSet<>(readers);
        }
    }

    public List<Role> getReaderRoles() {
        if (readerRoles != null && !readerRoles.isEmpty()) {
            return new ArrayList<>(readerRoles);
        } else {
            return new ArrayList<>(0);
        }
    }

    public void setReaderRoles(List<Role> readerRoles) {
        if (this.readerRoles != null) {
            this.readerRoles.clear();
            this.readerRoles.addAll(readerRoles);
        } else {
            this.readerRoles = new HashSet<>(readerRoles);
        }
    }

    public List<User> getEditors() {
        if (editors != null && !editors.isEmpty()) {
            return new ArrayList<>(editors);
        } else {
            return new ArrayList<>(0);
        }
    }

    public void setEditors(List<User> editors) {
        if (this.editors != null) {
            this.editors.clear();
            this.editors.addAll(editors);
        } else {
            this.editors = new HashSet<>(editors);
        }
    }

    public List<Role> getEditorRoles() {
        if (editorRoles != null && !editorRoles.isEmpty()) {
            return new ArrayList<>(editorRoles);
        } else {
            return new ArrayList<>(0);
        }
    }

    public void setEditorRoles(List<Role> editorRoles) {
        if (this.editorRoles != null) {
            this.editorRoles.clear();
            this.editorRoles.addAll(editorRoles);
        } else {
            this.editorRoles = new HashSet<>(editorRoles);
        }
    }
}