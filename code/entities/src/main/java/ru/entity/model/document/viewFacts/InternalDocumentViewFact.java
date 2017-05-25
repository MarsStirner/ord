package ru.entity.model.document.viewFacts;

import ru.entity.model.document.IncomingDocument;
import ru.entity.model.document.InternalDocument;
import ru.entity.model.document.viewFacts.composite_pk.InternalDocumentViewFactPrimaryKey;
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
@Table(name = "internal_document_views")
@IdClass(InternalDocumentViewFactPrimaryKey.class)
public class InternalDocumentViewFact  implements DocumentViewFact<InternalDocument>{
    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    private InternalDocument document;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewDateTime")
    private LocalDateTime viewDateTime;

    @Override
    public InternalDocument getDocument() {
        return document;
    }

    @Override
    public void setDocument(InternalDocument document) {
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
