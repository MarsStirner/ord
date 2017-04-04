package ru.entity.model.document.viewFacts;

import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by EUpatov on 03.04.2017.
 */
@MappedSuperclass
@IdClass(DocumentViewFactPrimaryKey.class)
public abstract class DocumentViewFact<T extends DocumentEntity> {
    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    protected T document;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    protected User user;


    @Column(name = "viewDateTime")
    protected LocalDateTime viewDateTime;

    public DocumentViewFact() {
    }

    public DocumentViewFact(T document, User user, LocalDateTime viewDateTime) {
        this.document = document;
        this.user = user;
        this.viewDateTime = viewDateTime;
    }

    public T getDocument() {
        return document;
    }

    public void setDocument(T document) {
        this.document = document;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getViewDateTime() {
        return viewDateTime;
    }

    public void setViewDateTime(LocalDateTime viewDateTime) {
        this.viewDateTime = viewDateTime;
    }
}
