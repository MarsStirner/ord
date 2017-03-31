package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.document.*;
import ru.entity.model.document.viewFacts.*;
import ru.entity.model.user.User;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 21:29 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface ViewFactDao {
    void applyViewFlagsOnIncomingDocumentList(List<IncomingDocument> documents, User loggedUser);

    void applyViewFlagsOnOutgoingDocumentList(List<OutgoingDocument> documents, User loggedUser);

    void applyViewFlagsOnInternalDocumentList(List<InternalDocument> documents, User loggedUser);

    void applyViewFlagsOnRequestDocumentList(List<RequestDocument> documents, User loggedUser);

    void applyViewFlagsOnTaskDocumentList(List<Task> documents, User loggedUser);

    boolean registerViewFact(IncomingDocument document, User user);

    boolean registerViewFact(OutgoingDocument document, User user);

    boolean registerViewFact(InternalDocument document, User user);

    boolean registerViewFact(RequestDocument document, User user);

    boolean registerViewFact(Task document, User user);

    List<InternalDocumentViewFact> getInternalDocumentsViewFacts(User loggedUser, List<Integer> idList);

    List<IncomingDocumentViewFact> getIncomingDocumentsViewFacts(User loggedUser, List<Integer> idList);

    List<OutgoingDocumentViewFact> getOutgoingDocumentsViewFacts(User loggedUser, List<Integer> idList);

    List<RequestDocumentViewFact> getRequestDocumentsViewFacts(User loggedUser, List<Integer> idList);

    List<TaskDocumentViewFact> getTaskDocumentsViewFacts(User loggedUser, List<Integer> idList);
}
