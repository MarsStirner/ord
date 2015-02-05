package ru.entity.model.document.viewFacts;

import ru.entity.model.document.InternalDocument;
import ru.entity.model.user.User;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 04.02.2015, 14:39 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class InternalDocumentViewFactPrimaryKey implements Serializable{
    private InternalDocument document;
    private User user;

    public InternalDocumentViewFactPrimaryKey() {
    }
}
