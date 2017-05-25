package ru.entity.model.document.viewFacts;

import ru.entity.model.document.InternalDocument;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.viewFacts.composite_pk.OutgoingDocumentViewFactPrimaryKey;
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
@Table(name = "outgoing_document_views")
@IdClass(OutgoingDocumentViewFactPrimaryKey.class)
public class OutgoingDocumentViewFact implements DocumentViewFact<OutgoingDocument>{
    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    private OutgoingDocument document;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewDateTime")
    private LocalDateTime viewDateTime;

    @Override
    public OutgoingDocument getDocument() {
        return document;
    }

    @Override
    public void setDocument(OutgoingDocument document) {
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
