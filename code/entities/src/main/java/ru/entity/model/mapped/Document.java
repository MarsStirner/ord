package ru.entity.model.mapped;


import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.Date;

/**
 * Базовый класс - документ
 */
@MappedSuperclass
public class Document extends DeletableEntity {

    private static final long serialVersionUID = -5542939516927639639L;
    /**
     * Дата создания
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;
    /**
     * Дата последнего редактирования
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date modified;
    /**
     * Автор
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private User author;
    /**
     * Последний редактор
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private User editor;

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getEditor() {
        return editor;
    }

    public void setEditor(User editor) {
        this.editor = editor;
    }
}