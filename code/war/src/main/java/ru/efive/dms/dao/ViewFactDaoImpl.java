package ru.efive.dms.dao;

import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.sql.dao.GenericDAOHibernate;
import ru.entity.model.document.*;
import ru.entity.model.document.viewFacts.*;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 02.02.2015, 18:41 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class ViewFactDaoImpl extends GenericDAOHibernate {
    //Логгеры
    private static final Logger loggerIncomingDocument = LoggerFactory.getLogger("INCOMING_DOCUMENT");
    private static final Logger loggerOutgoingDocument = LoggerFactory.getLogger("OUTGOING_DOCUMENT");
    private static final Logger loggerInternalDocument = LoggerFactory.getLogger("INTERNAL_DOCUMENT");
    private static final Logger loggerRequestDocument = LoggerFactory.getLogger("REQUEST_DOCUMENT");
    private static final Logger loggerTask = LoggerFactory.getLogger("TASK");

    /**
     * Имя css-класса для просмотренных документов
     */
    private static final String VISITED_STYLE_CLASS_NAME = "visited";

    /**
     * Имя css-класса для непросмотренных документов
     */
    private static final String UNVISITED_STYLE_CLASS_NAME = "unvisited";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Работа со стилями списков
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Установить признак прочтения ВХОДЯЩИХ документов для списка, пользователем
     *
     * @param documents  список ВХОДЯЩих документов, в котором необходимо установить признаки прочтения
     * @param loggedUser пользователь чьи факты прочтения документов мы проверяем
     */
    public void applyViewFlagsOnIncomingDocumentList(List<IncomingDocument> documents, User loggedUser) {
        final List<IncomingDocumentViewFact> viewFacts = getIncomingDocumentsViewFacts(loggedUser, getIdList
                (documents));
        for (IncomingDocument document : documents) {
            document.setStyleClass(UNVISITED_STYLE_CLASS_NAME);
            for (IncomingDocumentViewFact viewFact : viewFacts) {
                if (document.getId() == viewFact.getDocument().getId()) {
                    document.setStyleClass(VISITED_STYLE_CLASS_NAME);
                    break;
                }
            }
        }
    }

    /**
     * Установить признак прочтения ИСХОДЯЩИХ документов для списка, пользователем
     *
     * @param documents  список ИСХОДЯЩИХ документов, в котором необходимо установить признаки прочтения
     * @param loggedUser пользователь чьи факты прочтения документов мы проверяем
     */
    public void applyViewFlagsOnOutgoingDocumentList(List<OutgoingDocument> documents, User loggedUser) {
        final List<OutgoingDocumentViewFact> viewFacts = getOutgoingDocumentsViewFacts(loggedUser, getIdList
                (documents));
        for (OutgoingDocument document : documents) {
            document.setStyleClass(UNVISITED_STYLE_CLASS_NAME);
            for (OutgoingDocumentViewFact viewFact : viewFacts) {
                if (document.getId() == viewFact.getDocument().getId()) {
                    document.setStyleClass(VISITED_STYLE_CLASS_NAME);
                    break;
                }
            }
        }
    }

    /**
     * Установить признак прочтения ВНУТРЕННИХ документов для списка, пользователем
     *
     * @param documents  список ВНУТРЕННИХ документов, в котором необходимо установить признаки прочтения
     * @param loggedUser пользователь чьи факты прочтения документов мы проверяем
     */
    public void applyViewFlagsOnInternalDocumentList(List<InternalDocument> documents, User loggedUser) {
        final List<InternalDocumentViewFact> viewFacts = getInternalDocumentsViewFacts(loggedUser, getIdList
                (documents));
        for (InternalDocument document : documents) {
            document.setStyleClass(UNVISITED_STYLE_CLASS_NAME);
            for (InternalDocumentViewFact viewFact : viewFacts) {
                if (document.getId() == viewFact.getDocument().getId()) {
                    document.setStyleClass(VISITED_STYLE_CLASS_NAME);
                    break;
                }
            }
        }
    }

    /**
     * Установить признак прочтения ВНУТРЕННИХ документов для списка, пользователем
     *
     * @param documents  список ВНУТРЕННИХ документов, в котором необходимо установить признаки прочтения
     * @param loggedUser пользователь чьи факты прочтения документов мы проверяем
     */
    public void applyViewFlagsOnRequestDocumentList(List<RequestDocument> documents, User loggedUser) {
        final List<RequestDocumentViewFact> viewFacts = getRequestDocumentsViewFacts(loggedUser, getIdList(documents));
        for (RequestDocument document : documents) {
            document.setStyleClass(UNVISITED_STYLE_CLASS_NAME);
            for (RequestDocumentViewFact viewFact : viewFacts) {
                if (document.getId() == viewFact.getDocument().getId()) {
                    document.setStyleClass(VISITED_STYLE_CLASS_NAME);
                    break;
                }
            }
        }
    }

    /**
     * Установить признак прочтения ВНУТРЕННИХ документов для списка, пользователем
     *
     * @param documents  список ВНУТРЕННИХ документов, в котором необходимо установить признаки прочтения
     * @param loggedUser пользователь чьи факты прочтения документов мы проверяем
     */
    public void applyViewFlagsOnTaskDocumentList(List<Task> documents, User loggedUser) {
        final List<TaskDocumentViewFact> viewFacts = getTaskDocumentsViewFacts(loggedUser, getIdList(documents));
        for (Task document : documents) {
            document.setStyleClass(UNVISITED_STYLE_CLASS_NAME);
            for (TaskDocumentViewFact viewFact : viewFacts) {
                if (document.getId() == viewFact.getDocument().getId()) {
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
    public boolean registerViewFact(IncomingDocument document, User user) {
        IncomingDocumentViewFact viewFact = getIncomingDocumentViewFact(document, user);
        if (viewFact == null) {
            viewFact = new IncomingDocumentViewFact(document, user, new LocalDateTime().toDate());
            getHibernateTemplate().save(viewFact);
            loggerIncomingDocument.debug("Document[{}] is not already viewed by user[{}]. Write new viewFact to DB.",
                    document.getId(), user.getId());
            return true;
        } else {
            loggerIncomingDocument.debug("Document[{}] has been viewed by user[{}] at \"{}\"",
                    document.getId(), user.getId(), viewFact.getViewDateTime());
            return false;
        }
    }

    /**
     * Регистрирует факт просмотра исходящего документа
     *
     * @param document документ, который просматривается
     * @param user     пользователь, который просматривает
     * @return true- зарегистрирован новый факт просмотра \ false - факт просмотра уже есть или не был зарегистророван
     */
    public boolean registerViewFact(OutgoingDocument document, User user) {
        OutgoingDocumentViewFact viewFact = getOutgoingDocumentViewFact(document, user);
        if (viewFact == null) {
            viewFact = new OutgoingDocumentViewFact(document, user, new LocalDateTime().toDate());
            getHibernateTemplate().save(viewFact);
            loggerOutgoingDocument.debug("Document[{}] is not already viewed by user[{}]. Write new viewFact to DB.",
                    document.getId(), user.getId());
            return true;
        } else {
            loggerOutgoingDocument.debug("Document[{}] has been viewed by user[{}] at \"{}\"",
                    document.getId(), user.getId(), viewFact.getViewDateTime());
            return false;
        }
    }

    /**
     * Регистрирует факт просмотра Внутреннего документа
     *
     * @param document документ, который просматривается
     * @param user     пользователь, который просматривает
     * @return true- зарегистрирован новый факт просмотра \ false - факт просмотра уже есть или не был зарегистророван
     */
    public boolean registerViewFact(InternalDocument document, User user) {
        InternalDocumentViewFact viewFact = getInternalDocumentViewFact(document, user);
        if (viewFact == null) {
            viewFact = new InternalDocumentViewFact(document, user, new LocalDateTime().toDate());
            getHibernateTemplate().save(viewFact);
            loggerInternalDocument.debug("Document[{}] is not already viewed by user[{}]. Write new viewFact to DB.",
                    document.getId(), user.getId());
            return true;
        } else {
            loggerInternalDocument.debug("Document[{}] has been viewed by user[{}] at \"{}\"",
                    document.getId(), user.getId(), viewFact.getViewDateTime());
            return false;
        }
    }


    /**
     * Регистрирует факт просмотра Обращения-документа
     *
     * @param document документ, который просматривается
     * @param user     пользователь, который просматривает
     * @return true- зарегистрирован новый факт просмотра \ false - факт просмотра уже есть или не был зарегистророван
     */
    public boolean registerViewFact(RequestDocument document, User user) {
        RequestDocumentViewFact viewFact = getRequestDocumentViewFact(document, user);
        if (viewFact == null) {
            viewFact = new RequestDocumentViewFact(document, user, new LocalDateTime().toDate());
            getHibernateTemplate().save(viewFact);
            loggerRequestDocument.debug("Document[{}] is not already viewed by user[{}]. Write new viewFact to DB.",
                    document.getId(), user.getId());
            return true;
        } else {
            loggerRequestDocument.debug("Document[{}] has been viewed by user[{}] at \"{}\"",
                    document.getId(), user.getId(), viewFact.getViewDateTime());
            return false;
        }
    }


    /**
     * Регистрирует факт просмотра Поручения
     *
     * @param document документ, который просматривается
     * @param user     пользователь, который просматривает
     * @return true- зарегистрирован новый факт просмотра \ false - факт просмотра уже есть или не был зарегистророван
     */
    public boolean registerViewFact(Task document, User user) {
        TaskDocumentViewFact viewFact = getTaskDocumentViewFact(document, user);
        if (viewFact == null) {
            viewFact = new TaskDocumentViewFact(document, user, new LocalDateTime().toDate());
            getHibernateTemplate().save(viewFact);
            loggerTask.debug("Document[{}] is not already viewed by user[{}]. Write new viewFact to DB.", document
                    .getId(), user.getId());
            return true;
        } else {
            loggerTask.debug("Document[{}] has been viewed by user[{}] at \"{}\"",
                    document.getId(), user.getId(), viewFact.getViewDateTime());
            return false;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Получение одиночных фактов по ключу (документ-пользователь)
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private IncomingDocumentViewFact getIncomingDocumentViewFact(IncomingDocument document, User user) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(IncomingDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("document", document));
        criteria.add(Restrictions.eq("user", user));
        final List resultList = getHibernateTemplate().findByCriteria(criteria);
        if (!resultList.isEmpty()) {
            return (IncomingDocumentViewFact) resultList.get(0);
        }
        return null;
    }

    private OutgoingDocumentViewFact getOutgoingDocumentViewFact(OutgoingDocument document, User user) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(OutgoingDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("document", document));
        criteria.add(Restrictions.eq("user", user));
        final List resultList = getHibernateTemplate().findByCriteria(criteria);
        if (!resultList.isEmpty()) {
            return (OutgoingDocumentViewFact) resultList.get(0);
        }
        return null;
    }

    private InternalDocumentViewFact getInternalDocumentViewFact(InternalDocument document, User user) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(InternalDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("document", document));
        criteria.add(Restrictions.eq("user", user));
        final List resultList = getHibernateTemplate().findByCriteria(criteria);
        if (!resultList.isEmpty()) {
            return (InternalDocumentViewFact) resultList.get(0);
        }
        return null;
    }


    private RequestDocumentViewFact getRequestDocumentViewFact(RequestDocument document, User user) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(RequestDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("document", document));
        criteria.add(Restrictions.eq("user", user));
        final List resultList = getHibernateTemplate().findByCriteria(criteria);
        if (!resultList.isEmpty()) {
            return (RequestDocumentViewFact) resultList.get(0);
        }
        return null;
    }

    private TaskDocumentViewFact getTaskDocumentViewFact(Task document, User user) {
        final DetachedCriteria criteria = DetachedCriteria.forClass(TaskDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("document", document));
        criteria.add(Restrictions.eq("user", user));
        final List resultList = getHibernateTemplate().findByCriteria(criteria);
        if (!resultList.isEmpty()) {
            return (TaskDocumentViewFact) resultList.get(0);
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
    public List<InternalDocumentViewFact> getInternalDocumentsViewFacts(User loggedUser, List<Integer> idList) {
        DetachedCriteria criteria = DetachedCriteria.forClass(InternalDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("document.id", idList));
        criteria.add(Restrictions.eq("user", loggedUser));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Получить список фактов просмотра списка ВХОДЯЩИХ документов для пользователя
     *
     * @param loggedUser пользователь чьи просмотры надо найти
     * @param idList     список идентификаторов входящих документов
     * @return список фактов прочтения для заданных документов и прользователя
     */
    public List<IncomingDocumentViewFact> getIncomingDocumentsViewFacts(User loggedUser, List<Integer> idList) {
        DetachedCriteria criteria = DetachedCriteria.forClass(IncomingDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("document.id", idList));
        criteria.add(Restrictions.eq("user", loggedUser));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Получить список фактов просмотра списка ИСХОДЯЩИХ документов для пользователя
     *
     * @param loggedUser пользователь чьи просмотры надо найти
     * @param idList     список идентификаторов входящих документов
     * @return список фактов прочтения для заданных документов и прользователя
     */
    public List<OutgoingDocumentViewFact> getOutgoingDocumentsViewFacts(User loggedUser, List<Integer> idList) {
        DetachedCriteria criteria = DetachedCriteria.forClass(OutgoingDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("document.id", idList));
        criteria.add(Restrictions.eq("user", loggedUser));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Получить список фактов просмотра списка ОБРАЩЕНИЙ документов для пользователя
     *
     * @param loggedUser пользователь чьи просмотры надо найти
     * @param idList     список идентификаторов входящих документов
     * @return список фактов прочтения для заданных документов и прользователя
     */
    public List<RequestDocumentViewFact> getRequestDocumentsViewFacts(User loggedUser, List<Integer> idList) {
        DetachedCriteria criteria = DetachedCriteria.forClass(RequestDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("document.id", idList));
        criteria.add(Restrictions.eq("user", loggedUser));
        return getHibernateTemplate().findByCriteria(criteria);
    }

    /**
     * Получить список фактов просмотра списка ПОРУЧЕНИЙ документов для пользователя
     *
     * @param loggedUser пользователь чьи просмотры надо найти
     * @param idList     список идентификаторов входящих документов
     * @return список фактов прочтения для заданных документов и прользователя
     */
    public List<TaskDocumentViewFact> getTaskDocumentsViewFacts(User loggedUser, List<Integer> idList) {
        DetachedCriteria criteria = DetachedCriteria.forClass(TaskDocumentViewFact.class);
        criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.in("document.id", idList));
        criteria.add(Restrictions.eq("user", loggedUser));
        return getHibernateTemplate().findByCriteria(criteria);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получить список идентификаторов из списка документов (по фатку любого типа наследника от IdentifiedEntity)
     *
     * @param documents список документов, список идентифкаторов которого надо получить как результат
     * @return список идентификаторов документов (int)
     */
    private List<Integer> getIdList(final List<? extends IdentifiedEntity> documents) {
        final List<Integer> idList = new ArrayList<Integer>(documents.size());
        for (IdentifiedEntity document : documents) {
            idList.add(document.getId());
        }
        return idList;
    }
}
