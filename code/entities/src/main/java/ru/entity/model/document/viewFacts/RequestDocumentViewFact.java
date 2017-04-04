package ru.entity.model.document.viewFacts;

import ru.entity.model.document.RequestDocument;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "request_documents_views")
public class RequestDocumentViewFact extends DocumentViewFact<RequestDocument>{

    public RequestDocumentViewFact(RequestDocument document, User user, LocalDateTime date) {
        super(document, user, date);
    }
}