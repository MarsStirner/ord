package ru.entity.model.mapped;


import ru.entity.model.user.User;

import javax.persistence.*;

/**
 * Базовый класс - документ
 */
@MappedSuperclass
public abstract class DocumentEntity extends DeletableEntity {
    /**
     * Автор документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    /**
     * Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     */
    @Transient
    private String styleClass;

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

}