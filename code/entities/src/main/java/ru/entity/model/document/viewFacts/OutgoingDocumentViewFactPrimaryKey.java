package ru.entity.model.document.viewFacts;

import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.user.User;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 04.02.2015, 13:42 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class OutgoingDocumentViewFactPrimaryKey implements Serializable {
    private OutgoingDocument document;
    private User user;

    public OutgoingDocumentViewFactPrimaryKey() {
    }
}
