package ru.hitsl.sql.dao.impl;

import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.*;
import ru.entity.model.document.viewFacts.*;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.util.ApplicationHelper;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author: Upatov Egor <br>
 * Date: 02.02.2015, 18:41 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Repository("viewFactDao")
@Transactional(propagation = Propagation.MANDATORY)
public class ViewFactDaoImpl implements ViewFactDao {

    //Логгеры
    private static final Logger log = LoggerFactory.getLogger(ViewFactDaoImpl.class);

    private static final Map<Class<? extends DocumentEntity>, Class<? extends DocumentViewFact>> map = new HashMap<>(5);

    @PostConstruct
    public void init(){
        map.put(IncomingDocument.class, IncomingDocumentViewFact.class);
        map.put(InternalDocument.class, InternalDocumentViewFact.class);
        map.put(OutgoingDocument.class, OutgoingDocumentViewFact.class);
        map.put(RequestDocument.class, RequestDocumentViewFact.class);
        map.put(Task.class, TaskDocumentViewFact.class);

        log.info("Init[{}] for work with [{}] and EntityManager[{}]",
                Integer.toHexString(hashCode()),
                DocumentViewFact.class.getSimpleName(),
                Integer.toHexString(em.hashCode())
        );
    }

    /**
     * Имя css-класса для просмотренных документов
     */
    private static final String VISITED_STYLE_CLASS_NAME = "visited";
    /**
     * Имя css-класса для непросмотренных документов
     */
    private static final String UNVISITED_STYLE_CLASS_NAME = "unvisited";



    @PersistenceContext(unitName = ApplicationHelper.ORD_PERSISTENCE_UNIT_NAME)
    private EntityManager em;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа со стилями списков
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void applyViewFlagsOnDocumentList(List<? extends DocumentEntity> documents, User loggedUser) {
        final List<DocumentViewFact> viewFacts = getDocumentsViewFacts(
                loggedUser,
                documents.stream().map(IdentifiedEntity::getId).collect(Collectors.toList()),
                map.get(documents.stream().findFirst().get().getClass())
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
                viewFact = new IncomingDocumentViewFact();
            } else if (document instanceof InternalDocument) {
                viewFact = new InternalDocumentViewFact();
            } else if (document instanceof OutgoingDocument) {
                viewFact = new OutgoingDocumentViewFact();
            } else if (document instanceof RequestDocument) {
                viewFact = new RequestDocumentViewFact();
            } else if (document instanceof Task) {
                viewFact = new TaskDocumentViewFact();
            }
            //User entity is detached!
            viewFact.setUser(user);
            viewFact.setDocument(document);
            viewFact.setViewDateTime(LocalDateTime.now());
            em.persist(viewFact);
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
        final DetachedCriteria criteria = DetachedCriteria.forClass(map.get(document.getClass()));
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("document", document));
        criteria.add(Restrictions.eq("user", user));
        final List resultList = criteria.getExecutableCriteria(em.unwrap(Session.class)).list();
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
    public List<DocumentViewFact> getDocumentsViewFacts(User loggedUser, List<Integer> idList, Class<? extends DocumentViewFact> clazz) {
        DetachedCriteria criteria = DetachedCriteria.forClass(clazz);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("document.id", idList));
        criteria.add(Restrictions.eq("user", loggedUser));
        return criteria.getExecutableCriteria(em.unwrap(Session.class)).list();
    }


}
