package ru.entity.model.document.viewFacts;

import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.viewFacts.composite_pk.IncomingDocumentViewFactPrimaryKey;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Author: Upatov Egor <br>
 * Date: 02.02.2015, 15:21 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Entity
@Table(name = "incoming_documents_views")
@IdClass(IncomingDocumentViewFactPrimaryKey.class)
public class IncomingDocumentViewFact implements DocumentViewFact<IncomingDocument> {

    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    private IncomingDocument document;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewDateTime")
    private LocalDateTime viewDateTime;

    public IncomingDocumentViewFact() {
    }

    @Override
    public IncomingDocument getDocument() {
        return document;
    }

    @Override
    public void setDocument(IncomingDocument document) {
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
