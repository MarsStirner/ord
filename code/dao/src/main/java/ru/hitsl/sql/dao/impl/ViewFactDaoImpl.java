package ru.hitsl.sql.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.*;
import ru.entity.model.document.viewFacts.*;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author: Upatov Egor <br>
 * Date: 02.02.2015, 18:41 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Repository("viewFactDao")
@Transactional("ordTransactionManager")
public class ViewFactDaoImpl implements ViewFactDao {

    //Логгеры
    private static final Logger log = LoggerFactory.getLogger("INCOMING_DOCUMENT");

    /**
     * Имя css-класса для просмотренных документов
     */
    private static final String VISITED_STYLE_CLASS_NAME = "visited";
    /**
     * Имя css-класса для непросмотренных документов
     */
    private static final String UNVISITED_STYLE_CLASS_NAME = "unvisited";
    @Autowired
    private SessionFactory sessionFactory;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа со стилями списков
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void applyViewFlagsOnDocumentList(List<? extends DocumentEntity> documents, User loggedUser) {
        final List<DocumentViewFact> viewFacts = getDocumentsViewFacts(
                loggedUser,
                documents.stream().map(IdentifiedEntity::getId).collect(Collectors.toList()),
                documents.stream().findFirst().get().getClass()
        );
        for (DocumentEntity document : documents) {
            document.setStyleClass(UNVISITED_STYLE_CLASS_NAME);
            for (DocumentViewFact viewFact : viewFacts) {
                if (Objects.equals(document.getId(), viewFact.getDocument().getId())) {
                    document.setStyleClass(VISITED_STYLE_CLASS_NAME);
                    break;
                }
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Регистрация фактов просмотра (создать факт если еще нет)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Регистрирует факт просмотра входящего документа
     *
     * @param document документ, который просматривается
     * @param user     пользователь, который просматривает
     * @return true- зарегистрирован новый факт просмотра \ false - факт просмотра уже есть или не был зарегистророван
     */
    @Override
    public boolean registerViewFact(DocumentEntity document, User user) {
        DocumentViewFact viewFact = getDocumentViewFact(document, user);
        if (viewFact == null) {
            if (document instanceof IncomingDocument) {
                viewFact = new IncomingDocumentViewFact((IncomingDocument) document, user, LocalDateTime.now());
            } else if (document instanceof InternalDocument) {
                viewFact = new InternalDocumentViewFact((InternalDocument) document, user, LocalDateTime.now());
            } else if (document instanceof OutgoingDocument) {
                viewFact = new OutgoingDocumentViewFact((OutgoingDocument) document, user, LocalDateTime.now());
            } else if (document instanceof RequestDocument) {
                viewFact = new RequestDocumentViewFact((RequestDocument) document, user, LocalDateTime.now());
            } else if (document instanceof Task) {
                viewFact = new TaskDocumentViewFact((Task) document, user, LocalDateTime.now());
            }
            sessionFactory.getCurrentSession().save(viewFact);
            log.debug("Document[{}] is not already viewed by user[{}]. Write new viewFact to DB.", document.getId(), user.getId());
            return true;
        } else {
            log.debug("Document[{}] has been viewed by user[{}] at \"{}\"",
                    document.getId(), user.getId(), viewFact.getViewDateTime());
            return false;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Получение одиночных фактов по ключу (документ-пользователь)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private DocumentViewFact getDocumentViewFact(DocumentEntity document, User user) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(document.getClass());
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("document", document));
        criteria.add(Restrictions.eq("user", user));
        final List resultList = criteria.getExecutableCriteria(sessionFactory.getCurrentSession()).list();
        if (!resultList.isEmpty()) {
            return (DocumentViewFact) resultList.get(0);
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Получение Списков фактов просмотров (список идентификаторов документов + пользователь)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Получить список фактов просмотра списка ВНУТРЕННИХ документов для пользователя
     *
     * @param loggedUser пользователь чьи просмотры надо найти
     * @param idList     список идентификаторов входящих документов
     * @return список фактов прочтения для заданных документов и прользователя
     */
    @Override
    public List<DocumentViewFact> getDocumentsViewFacts(User loggedUser, List<Integer> idList, Class<? extends DocumentEntity> clazz) {
        DetachedCriteria criteria = DetachedCriteria.forClass(clazz);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("document.id", idList));
        criteria.add(Restrictions.eq("user", loggedUser));
        return criteria.getExecutableCriteria(sessionFactory.getCurrentSession()).list();
    }


}
