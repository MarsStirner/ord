package ru.entity.model.document.viewFacts;

import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.user.User;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by EUpatov on 03.04.2017.
 */
public interface DocumentViewFact<T extends DocumentEntity> extends Serializable{

    T getDocument();

    void setDocument(T document);

    User getUser();

    void setUser(User user);

    LocalDateTime getViewDateTime();

    void setViewDateTime(LocalDateTime viewDateTime);
}
