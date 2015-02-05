package ru.entity.model.document.viewFacts;

import ru.entity.model.document.IncomingDocument;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.Date;

/**
 * Author: Upatov Egor <br>
 * Date: 02.02.2015, 15:21 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Entity
@Table(name = "dms_incoming_documents_views")
@IdClass(IncomingDocumentViewFactPrimaryKey.class)
public class IncomingDocumentViewFact {

    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    private IncomingDocument document;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date viewDateTime;

    public IncomingDocumentViewFact() {
    }

    public IncomingDocumentViewFact(IncomingDocument document, User user, Date viewDateTime) {
        this.document = document;
        this.user = user;
        this.viewDateTime = viewDateTime;
    }

    public IncomingDocument getDocument() {
        return document;
    }

    public void setDocument(IncomingDocument document) {
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
