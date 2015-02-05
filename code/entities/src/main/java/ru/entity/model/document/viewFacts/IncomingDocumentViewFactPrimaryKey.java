package ru.entity.model.document.viewFacts;

import ru.entity.model.document.IncomingDocument;
import ru.entity.model.user.User;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 02.02.2015, 15:50 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class IncomingDocumentViewFactPrimaryKey implements Serializable {
    private IncomingDocument document;
    private User user;

    public IncomingDocumentViewFactPrimaryKey() {
    }
}