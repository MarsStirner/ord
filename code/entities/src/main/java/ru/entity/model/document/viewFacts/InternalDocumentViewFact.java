package ru.entity.model.document.viewFacts;

import ru.entity.model.document.InternalDocument;
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
@Table(name = "internal_documents_views")
public class InternalDocumentViewFact  extends DocumentViewFact<InternalDocument>{
    public InternalDocumentViewFact(InternalDocument document, User user, LocalDateTime date) {
        super(document, user, date);
    }
}
