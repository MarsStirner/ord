package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.document.viewFacts.*;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.user.User;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 21:29 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface ViewFactDao {

    void applyViewFlagsOnDocumentList(List<? extends DocumentEntity> documents, User loggedUser);

    boolean registerViewFact(DocumentEntity document, User user);

    List<DocumentViewFact> getDocumentsViewFacts(User loggedUser, List<Integer> idList, Class<? extends DocumentEntity> clazz);
}
