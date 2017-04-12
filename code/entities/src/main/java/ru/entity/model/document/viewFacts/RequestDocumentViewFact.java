package ru.entity.model.document.viewFacts;

import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.RequestDocument;
import ru.entity.model.document.viewFacts.composite_pk.RequestDocumentViewFactPrimaryKey;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "request_documents_views")
@IdClass(RequestDocumentViewFactPrimaryKey.class)
public class RequestDocumentViewFact implements DocumentViewFact<RequestDocument>{
    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    private RequestDocument document;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewDateTime")
    private LocalDateTime viewDateTime;

    @Override
    public RequestDocument getDocument() {
        return document;
    }

    @Override
    public void setDocument(RequestDocument document) {
        this.document = document;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public LocalDateTime getViewDateTime() {
        return viewDateTime;
    }

    @Override
    public void setViewDateTime(LocalDateTime viewDateTime) {
        this.viewDateTime = viewDateTime;
    }
}
