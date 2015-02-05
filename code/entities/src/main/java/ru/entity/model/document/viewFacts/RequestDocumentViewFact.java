package ru.entity.model.document.viewFacts;

import ru.entity.model.document.RequestDocument;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "dms_request_documents_views")
@IdClass(RequestDocumentViewFactPrimaryKey.class)
public class RequestDocumentViewFact {

    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    private RequestDocument document;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date viewDateTime;

    public RequestDocumentViewFact() {
    }

    public RequestDocumentViewFact(RequestDocument document, User user, Date viewDateTime) {
        this.document = document;
        this.user = user;
        this.viewDateTime = viewDateTime;
    }

    public RequestDocument getDocument() {
        return document;
    }

    public void setDocument(RequestDocument document) {
        this.document = document;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getViewDateTime() {
        return viewDateTime;
    }

    public void setViewDateTime(Date viewDateTime) {
        this.viewDateTime = viewDateTime;
    }

}