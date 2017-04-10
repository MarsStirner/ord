package ru.entity.model.document.viewFacts.composite_pk;

import ru.entity.model.document.InternalDocument;
import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.user.User;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 02.02.2015, 15:50 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class OutgoingDocumentViewFactPrimaryKey implements Serializable {
    private OutgoingDocument document;
    private User user;

    public OutgoingDocument getDocument() {
        return document;
    }

    public void setDocument(OutgoingDocument document) {
        this.document = document;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}