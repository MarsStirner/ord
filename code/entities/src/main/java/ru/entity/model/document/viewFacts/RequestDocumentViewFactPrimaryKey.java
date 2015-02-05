package ru.entity.model.document.viewFacts;

import ru.entity.model.document.RequestDocument;
import ru.entity.model.user.User;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 04.02.2015, 19:06 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class RequestDocumentViewFactPrimaryKey implements Serializable {
    private RequestDocument document;
    private User user;

    public RequestDocumentViewFactPrimaryKey() {
    }
}
