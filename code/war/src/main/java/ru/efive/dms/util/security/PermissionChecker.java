package ru.efive.dms.util.security;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.document.*;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import static ru.efive.dms.util.ApplicationDAONames.*;
import static ru.efive.dms.util.security.Permissions.ALL_PERMISSIONS;
import static ru.efive.dms.util.security.Permissions.Permission.EXECUTE;
import static ru.efive.dms.util.security.Permissions.Permission.READ;

/**
 * Author: Upatov Egor <br>
 * Date: 12.11.2014, 12:37 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для проверки прав доступа к документам \ поручениям
 */
@Stateless
public class PermissionChecker {

    @EJB(name = "indexManagement")
    private IndexManagementBean indexManagementBean;

    //Логгеры
    private static final Logger loggerIncomingDocument = LoggerFactory.getLogger("INCOMING_DOCUMENT");
    private static final Logger loggerOutgoingDocument = LoggerFactory.getLogger("OUTGOING_DOCUMENT");
    private static final Logger loggerInternalDocument = LoggerFactory.getLogger("INTERNAL_DOCUMENT");
    private static final Logger loggerRequestDocument = LoggerFactory.getLogger("REQUEST_DOCUMENT");
    private static final Logger loggerTask = LoggerFactory.getLogger("TASK");

    /**
     * @param user     пользователь для которого проверяем права
     * @param document входящий документ, на который проверяем права
     * @return структура с правами
     */
    public Permissions getPermissions(final User user, final IncomingDocument document) {
        /**
         * 1) админ
         * 2) автор
         * 3) исполнители
         * 4) список пользователей на редактирование
         * 5) список пользователей на просмотр
         * 6) список ролей на редактирование
         * 7) список ролей на просмотр
         * 8) руководитель
         * 9) пользователи адресаты
         * 10) группы адресаты
         * 11) пользователи, учавствующие в согласованиях
         * <SUMMARY>
         * Просмотр: 1-2-3-4-5-6-7-8-9-10-11
         * Редактирование документа: 1-2-4-6-8
         * Действия: 1-2-3-8-9-10
         */
        //1) админ
        if (user.isAdministrator()) {
            loggerIncomingDocument.debug("{}:Permission RWX granted: AdminRole", document.getId());
            return ALL_PERMISSIONS;
        }
        //2) автор
        if (user.equals(document.getAuthor())) {
            loggerIncomingDocument.debug("{}:Permission RWX granted: Author", document.getId());
            return ALL_PERMISSIONS;
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            loggerIncomingDocument.debug("{}:Permission RWX granted: Controller", document.getId());
            return ALL_PERMISSIONS;
        }
        final Permissions result = new Permissions();
        //   6) список ролей на редактирование
        //   7) список ролей на просмотр
        for (Role currentRole : user.getRoles()) {
            if (document.getRoleEditors().contains(currentRole)) {
                loggerIncomingDocument.debug("{}:Permission  RWX granted: RoleEditor [{}] ", document.getId(), currentRole.getName());
                return ALL_PERMISSIONS;
            }
            if (!result.hasPermission(READ) && document.getRoleReaders().contains(currentRole)) {
                loggerIncomingDocument.debug("{}:Permission R granted: RoleReader [{}] ", document.getId(), currentRole.getName());
                result.addPermission(READ);
            }
        }

        //4) список пользователей на редактирование
        for (User currentUser : document.getPersonEditors()) {
            if (user.equals(currentUser)) {
                loggerIncomingDocument.debug("{}:Permission RWX granted: PersonEditor", document.getId());
                return ALL_PERMISSIONS;
            }
        }

        //3) исполнитель
        for (User currentUser : document.getExecutors()) {
            if (user.equals(currentUser)) {
                loggerIncomingDocument.debug("{}:Permission RX granted: Executor", document.getId());
                result.addPermission(READ);
                result.addPermission(EXECUTE);
                break;
            }
        }

        //5) список пользователей на просмотр
        if (!result.hasPermission(READ)) {
            for (User currentUser : document.getPersonReaders()) {
                if (user.equals(currentUser)) {
                    loggerIncomingDocument.debug("{}:Permission R granted: PersonReader", document.getId());
                    result.addPermission(READ);
                    break;
                }
            }
        }

        //9) пользователи адресаты
        for (User currentUser : document.getRecipientUsers()) {
            if (user.equals(currentUser)) {
                loggerIncomingDocument.debug("{}:Permission RX granted: RecipientUser", document.getId());
                result.addPermission(READ);
                result.addPermission(EXECUTE);
                break;
            }
        }
        //10) группы адресаты
        for (Group currentGroup : user.getGroups()) {
            if (document.getRecipientGroups().contains(currentGroup)) {
                loggerIncomingDocument.debug("{}:Permission RX granted: RecipientGroup [{}] ", document.getId(), currentGroup.getValue());
                result.addPermission(READ);
                result.addPermission(EXECUTE);
                break;
            }
        }
        //11) пользователи, учавствующие в согласованиях
        if (!result.hasPermission(READ) && ((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(user, document.getUniqueId())) {
            loggerIncomingDocument.debug("{}:Permission R granted: TASK", document.getId());
            result.addPermission(READ);
        }
        loggerIncomingDocument.debug("Total permissions for [{}]: {}", document.getId(), result);
        return result;
    }

    /**
     * @param user     пользователь для которого проверяем права
     * @param document поручение, для которого проверяем права
     * @return структура с правами
     */
    public Permissions getPermissions(final User user, final Task document) {
        //1) админ
        if (user.isAdministrator()) {
            loggerTask.debug("{}:Permission RWX granted: AdminRole", document.getId());
            return ALL_PERMISSIONS;
        }
        //2) автор
        if (user.equals(document.getAuthor())) {
            loggerTask.debug("{}:Permission RWX granted: Author", document.getId());
            return ALL_PERMISSIONS;
        }
        //3) инициатор
        if (user.equals(document.getInitiator())) {
            loggerTask.debug("{}:Permission RWX granted: Initiator", document.getId());
            return ALL_PERMISSIONS;
        }
        final Permissions result = new Permissions();
        //4) Контролер
        if (user.equals(document.getController())) {
            loggerTask.debug("{}:Permission RX granted: Controller", document.getId());
            result.addPermission(READ);
            result.addPermission(EXECUTE);
        }
        //5) исполнитель
        for (User currentUser : document.getExecutors()) {
            if (user.equals(currentUser)) {
                loggerTask.debug("{}:Permission RX granted: Executor", document.getId());
                result.addPermission(READ);
                result.addPermission(EXECUTE);
                break;
            }
        }
        //Если не все права есть, то попробовать подтянуть права из документа-основания
        if (!result.hasPermission(READ) && !result.hasPermission(EXECUTE)) {
            if (StringUtils.isNotEmpty(document.getRootDocumentId())) {
                loggerTask.debug("Try to get permissions from rootDocument");
                result.mergePermissions(getPermissionsFromExternalDocument(user, document.getRootDocumentId(), loggerTask));
            }
        }
        loggerTask.debug("Total permissions for [{}]: {}", document.getId(), result);
        return result;
    }


    /**
     * @param user     пользователь для которого проверяем права
     * @param document исходящий документ, на который проверяем права
     * @return структура с правами
     */
    public Permissions getPermissions(final User user, final OutgoingDocument document) {
        /**
         * 1) админ
         * 2) автор
         * 3) исполнители
         * 4) список пользователей на редактирование
         * 5) список пользователей на просмотр
         * 6) список ролей на редактирование
         * 7) список ролей на просмотр
         * 8) руководитель
         * 9) пользователи адресаты  (NULL)
         * 10) группы адресаты   (NULL)
         * 11) пользователи, учавствующие в согласованиях 
         * <SUMMARY>
         * Просмотр: 1-2-3-4-5-6-7-8-9-10-11
         * Редактирование документа: 1-2-4-6-8
         * Действия: 1-2-3-8-9-10
         */
        //1) админ
        if (user.isAdministrator()) {
            loggerOutgoingDocument.debug("{}:Permission RWX granted: AdminRole", document.getId());
            return ALL_PERMISSIONS;
        }
        //2) автор
        if (user.equals(document.getAuthor())) {
            loggerOutgoingDocument.debug("{}:Permission RWX granted: Author", document.getId());
            return ALL_PERMISSIONS;
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            loggerOutgoingDocument.debug("{}:Permission RWX granted: Controller", document.getId());
            return ALL_PERMISSIONS;
        }

        final Permissions result = new Permissions();
        //3) исполнитель
        if (user.equals(document.getExecutor())) {
            loggerOutgoingDocument.debug("{}:Permission RX granted: Executor", document.getId());
            result.addPermission(READ);
            result.addPermission(EXECUTE);
        }

        //4) список пользователей на редактирование
        for (User currentUser : document.getPersonEditors()) {
            if (user.equals(currentUser)) {
                loggerOutgoingDocument.debug("{}:Permission RWX granted: PersonEditor", document.getId());
                return ALL_PERMISSIONS;
            }
        }
        //5) список пользователей на просмотр
        for (User currentUser : document.getPersonReaders()) {
            if (user.equals(currentUser)) {
                loggerOutgoingDocument.debug("{}:Permission R granted: PersonReader", document.getId());
                result.addPermission(READ);
                break;
            }
        }
        //   6) список ролей на редактирование
        //   7) список ролей на просмотр
        for (Role currentRole : user.getRoles()) {
            if (document.getRoleEditors().contains(currentRole)) {
                loggerOutgoingDocument.debug("{}:Permission  RWX granted: RoleEditor [{}] ", document.getId(), currentRole.getName());
                return ALL_PERMISSIONS;
            }
            if (!result.hasPermission(READ) && document.getRoleReaders().contains(currentRole)) {
                loggerOutgoingDocument.debug("{}:Permission R granted: RoleReader [{}] ", document.getId(), currentRole.getName());
                result.addPermission(READ);
            }
        }
        //9) пользователи адресаты
        // Отсутствуют
        //10) группы адресаты
        // Отсутствуют
        //11) пользователи, учавствующие в согласованиях
        if (!result.hasPermission(READ) && ((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(user, document.getUniqueId())) {
            loggerOutgoingDocument.debug("{}:Permission R granted: TASK", document.getId());
            result.addPermission(READ);
        }
        loggerOutgoingDocument.debug("Total permissions for [{}]: {}", document.getId(), result);
        return result;
    }


    /**
     * @param user     пользователь для которого проверяем права
     * @param document внутренний документ, на который проверяем права
     * @return структура с правами
     */
    public Permissions getPermissions(final User user, final InternalDocument document) {
        /**
         * 1) админ
         * 2) автор
         * 3) исполнители (NULL)
         * 4) список пользователей на редактирование
         * 5) список пользователей на просмотр
         * 6) список ролей на редактирование
         * 7) список ролей на просмотр
         * 8) руководитель
         * 9) пользователи адресаты
         * 10) группы адресаты
         * 11) пользователи, учавствующие в согласованиях
         * <SUMMARY>
         * Просмотр: 1-2-3-4-5-6-7-8-9-10-11
         * Редактирование документа: 1-2-4-6-8
         * Действия: 1-2-3-8-9-10
         */
        //1) админ
        if (user.isAdministrator()) {
            loggerInternalDocument.debug("{}:Permission RWX granted: AdminRole", document.getId());
            return ALL_PERMISSIONS;
        }
        //2) автор
        if (user.equals(document.getAuthor())) {
            loggerInternalDocument.debug("{}:Permission RWX granted: Author", document.getId());
            return ALL_PERMISSIONS;
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            loggerInternalDocument.debug("{}:Permission RWX granted: Controller", document.getId());
            return ALL_PERMISSIONS;
        }

        final Permissions result = new Permissions();
        //3) исполнитель
        //Отстутсвует

        //4) список пользователей на редактирование
        for (User currentUser : document.getPersonEditors()) {
            if (user.equals(currentUser)) {
                loggerInternalDocument.debug("{}:Permission RWX granted: PersonEditor", document.getId());
                return ALL_PERMISSIONS;
            }
        }
        //5) список пользователей на просмотр
        for (User currentUser : document.getPersonReaders()) {
            if (user.equals(currentUser)) {
                loggerInternalDocument.debug("{}:Permission R granted: PersonReader", document.getId());
                result.addPermission(READ);
                break;
            }
        }
        //   6) список ролей на редактирование
        //   7) список ролей на просмотр
        for (Role currentRole : user.getRoles()) {
            if (document.getRoleEditors().contains(currentRole)) {
                loggerInternalDocument.debug("{}:Permission  RWX granted: RoleEditor [{}] ", document.getId(), currentRole.getName());
                return ALL_PERMISSIONS;
            }
            if (!result.hasPermission(READ) && document.getRoleReaders().contains(currentRole)) {
                loggerInternalDocument.debug("{}:Permission R granted: RoleReader [{}] ", document.getId(), currentRole.getName());
                result.addPermission(READ);
            }
        }
        //9) пользователи адресаты
        for (User currentUser : document.getRecipientUsers()) {
            if (user.equals(currentUser)) {
                loggerInternalDocument.debug("{}:Permission RX granted: RecipientUser", document.getId());
                result.addPermission(READ);
                result.addPermission(EXECUTE);
                break;
            }
        }
        //10) группы адресаты
        for (Group currentGroup : user.getGroups()) {
            if (document.getRecipientGroups().contains(currentGroup)) {
                loggerInternalDocument.debug("{}:Permission RX granted: RecipientGroup [{}] ", document.getId(), currentGroup.getValue());
                result.addPermission(READ);
                result.addPermission(EXECUTE);
                break;
            }
        }
        //11) пользователи, учавствующие в согласованиях
        if (!result.hasPermission(READ) && ((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(user, document.getUniqueId())) {
            loggerInternalDocument.debug("{}:Permission R granted: TASK", document.getId());
            result.addPermission(READ);
        }
        loggerInternalDocument.debug("Total permissions for [{}]: {}", document.getId(), result);
        return result;
    }

    /**
     * @param user     пользователь для которого проверяем права
     * @param document входящий документ, на который проверяем права
     * @return структура с правами
     */
    public Permissions getPermissions(final User user, final RequestDocument document) {
        /**
         * 1) админ
         * 2) автор
         * 3) исполнители
         * 4) список пользователей на редактирование
         * 5) список пользователей на просмотр
         * 6) список ролей на редактирование
         * 7) список ролей на просмотр
         * 8) руководитель
         * 9) пользователи адресаты
         * 10) группы адресаты
         * 11) пользователи, учавствующие в согласованиях
         * <SUMMARY>
         * Просмотр: 1-2-3-4-5-6-7-8-9-10-11
         * Редактирование документа: 1-2-4-6-8
         * Действия: 1-2-3-8-9-10
         */
        //1) админ
        if (user.isAdministrator()) {
            loggerIncomingDocument.debug("{}:Permission RWX granted: AdminRole", document.getId());
            return ALL_PERMISSIONS;
        }
        //2) автор
        if (user.equals(document.getAuthor())) {
            loggerIncomingDocument.debug("{}:Permission RWX granted: Author", document.getId());
            return ALL_PERMISSIONS;
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            loggerIncomingDocument.debug("{}:Permission RWX granted: Controller", document.getId());
            return ALL_PERMISSIONS;
        }

        final Permissions result = new Permissions();
        //3) исполнитель
        if (user.equals(document.getExecutor())) {
            loggerIncomingDocument.debug("{}:Permission RX granted: Executor", document.getId());
            result.addPermission(READ);
            result.addPermission(EXECUTE);
        }

        //4) список пользователей на редактирование
        for (User currentUser : document.getPersonEditors()) {
            if (user.equals(currentUser)) {
                loggerIncomingDocument.debug("{}:Permission RWX granted: PersonEditor", document.getId());
                return ALL_PERMISSIONS;
            }
        }
        //5) список пользователей на просмотр
        for (User currentUser : document.getPersonReaders()) {
            if (user.equals(currentUser)) {
                loggerIncomingDocument.debug("{}:Permission R granted: PersonReader", document.getId());
                result.addPermission(READ);
                break;
            }
        }
        //   6) список ролей на редактирование
        //   7) список ролей на просмотр
        for (Role currentRole : user.getRoles()) {
            if (document.getRoleEditors().contains(currentRole)) {
                loggerIncomingDocument.debug("{}:Permission  RWX granted: RoleEditor [{}] ", document.getId(), currentRole.getName());
                return ALL_PERMISSIONS;
            }
            if (!result.hasPermission(READ) && document.getRoleReaders().contains(currentRole)) {
                loggerIncomingDocument.debug("{}:Permission R granted: RoleReader [{}] ", document.getId(), currentRole.getName());
                result.addPermission(READ);
            }
        }

        //9) пользователи адресаты
        for (User currentUser : document.getRecipientUsers()) {
            if (user.equals(currentUser)) {
                loggerIncomingDocument.debug("{}:Permission RX granted: RecipientUser", document.getId());
                result.addPermission(READ);
                result.addPermission(EXECUTE);
                break;
            }
        }
        //10) группы адресаты
        for (Group currentGroup : user.getGroups()) {
            if (document.getRecipientGroups().contains(currentGroup)) {
                loggerIncomingDocument.debug("{}:Permission RX granted: RecipientGroup [{}] ", document.getId(), currentGroup.getValue());
                result.addPermission(READ);
                result.addPermission(EXECUTE);
                break;
            }
        }
        //11) пользователи, учавствующие в согласованиях
        if (!result.hasPermission(READ) && ((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(user, document.getUniqueId())) {
            loggerIncomingDocument.debug("{}:Permission R granted: TASK", document.getId());
            result.addPermission(READ);
        }
        loggerIncomingDocument.debug("Total permissions for [{}]: {}", document.getId(), result);
        return result;
    }


    /**
     * Получение прав пользователя на документ (обычно для тех у кого есть документ-основание)
     *
     * @param user        пользователь для которого проверяем права
     * @param documentKey уникальный номер документа (например "incoming_0001")
     * @param logger      логгер, которые отписывает отладаочную инфу
     * @return права пользователя на документ
     */
    public Permissions getPermissionsFromExternalDocument(User user, String documentKey, Logger logger) {
        final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(documentKey);
        if (rootDocumentId != null) {
            if (documentKey.contains("incoming")) {
                //TODO String id method ?!?!?!? WHY??????
                final IncomingDocument rootDocument = ((IncomingDocumentDAOImpl) indexManagementBean.getContext().getBean(INCOMING_DOCUMENT_FORM_DAO)).findDocumentById(rootDocumentId.toString());
                if (rootDocument != null) {
                    final Permissions fromRootDocument = getPermissions(user, rootDocument);
                    logger.debug("ROOT_DOC<incoming[{}]> permissions: {}", rootDocumentId, fromRootDocument);
                    return fromRootDocument;
                } else {
                    logger.warn("ROOT_DOC<incoming[{}]> - not found", rootDocumentId);
                    return Permissions.EMPTY_PERMISSIONS;
                }
            } else if (documentKey.contains("outgoing")) {
                final OutgoingDocument rootDocument = ((OutgoingDocumentDAOImpl) indexManagementBean.getContext().getBean(OUTGOING_DOCUMENT_FORM_DAO)).get(rootDocumentId);
                if (rootDocument != null) {
                    final Permissions fromRootDocument = getPermissions(user, rootDocument);
                    logger.debug("ROOT_DOC<outgoing[{}]> permissions: {}", rootDocumentId, fromRootDocument);
                    return fromRootDocument;
                } else {
                    logger.warn("ROOT_DOC<outgoing[{}]> - not found", rootDocumentId);
                    return Permissions.EMPTY_PERMISSIONS;
                }
            } else if (documentKey.contains("internal")) {
                final InternalDocument rootDocument = ((InternalDocumentDAOImpl) indexManagementBean.getContext().getBean(INTERNAL_DOCUMENT_FORM_DAO)).get(rootDocumentId);
                if (rootDocument != null) {
                    final Permissions fromRootDocument = getPermissions(user, rootDocument);
                    logger.debug("ROOT_DOC<internal[{}]> permissions: {}", rootDocumentId, fromRootDocument);
                    return fromRootDocument;
                } else {
                    logger.warn("ROOT_DOC<internal[{}]> - not found", rootDocumentId);
                    return Permissions.EMPTY_PERMISSIONS;
                }
            } else if (documentKey.contains("request")) {
                final RequestDocument rootDocument = ((RequestDocumentDAOImpl) indexManagementBean.getContext().getBean(REQUEST_DOCUMENT_FORM_DAO)).get(rootDocumentId);
                if (rootDocument != null) {
                    final Permissions fromRootDocument = getPermissions(user, rootDocument);
                    logger.debug("ROOT_DOC<request[{}]> permissions: {}", rootDocumentId, fromRootDocument);
                    return fromRootDocument;
                } else {
                    logger.warn("ROOT_DOC<request[{}]> - not found", rootDocumentId);
                    return Permissions.EMPTY_PERMISSIONS;
                }
            } else {
                logger.warn("Unknown rootDocumentId [{}]", documentKey);
                return Permissions.EMPTY_PERMISSIONS;
            }
        } else {
            logger.warn("Cannot parse id part from rootDocumentId [{}]", documentKey);
            return Permissions.EMPTY_PERMISSIONS;
        }
    }


    /**
     * Получения прав текущего авторизованного пользователя (вместе со всей инфой)
     *
     * @param sessionManagement информация об авторизованном пользователе
     * @param document          входящий документ, на который проверяем права
     * @return суммарный набор прав для текущих данных по авторизации
     */
    public Permissions getPermissions(SessionManagementBean sessionManagement, IncomingDocument document) {
        if (sessionManagement.isAdministrator()) {
            loggerIncomingDocument.info("Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole",
                    sessionManagement.getLoggedUser().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(sessionManagement.getLoggedUser(), document);
        if (sessionManagement.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : sessionManagement.getSubstitutedUsers()) {
                loggerIncomingDocument.debug("Get permissions on substituted user [{}] {}",
                        currentUser.getId(), currentUser.getFullName()
                );
                Permissions subResult = getPermissions(currentUser, document);
                loggerIncomingDocument.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerIncomingDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        loggerIncomingDocument.info("Result permissions for user[{}] to document[{}] is {}",
                new Object[]{
                        sessionManagement.getLoggedUser().getId(),
                        document.getId(),
                        result
                }
        );
        return result;
    }

    /**
     * Получения прав текущего авторизованного пользователя (вместе со всей инфой)
     * @param sessionManagement информация об авторизованном пользователе
     * @param document          входящий документ, на который проверяем права
     * @return суммарный набор прав для текущих данных по авторизации
     */
    public Permissions getPermissions(SessionManagementBean sessionManagement, Task document) {
        if (sessionManagement.isAdministrator()) {
            loggerTask.info("Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole",
                    sessionManagement.getLoggedUser().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(sessionManagement.getLoggedUser(), document);
        if (sessionManagement.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : sessionManagement.getSubstitutedUsers()) {
                loggerTask.debug("Get permissions on substituted user [{}] {}",
                        currentUser.getId(), currentUser.getFullName()
                );
                Permissions subResult = getPermissions(currentUser, document);
                loggerTask.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerIncomingDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        loggerTask.info("Result permissions for user[{}] to document[{}] is {}",
                new Object[]{
                        sessionManagement.getLoggedUser().getId(),
                        document.getId(),
                        result
                }
        );
        return result;
    }

    public Permissions getPermissions(SessionManagementBean sessionManagement, OutgoingDocument document) {
        if (sessionManagement.isAdministrator()) {
            loggerOutgoingDocument.info("Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole",
                    sessionManagement.getLoggedUser().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(sessionManagement.getLoggedUser(), document);
        if (sessionManagement.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : sessionManagement.getSubstitutedUsers()) {
                loggerOutgoingDocument.debug("Get permissions on substituted user [{}] {}",
                        currentUser.getId(), currentUser.getFullName()
                );
                Permissions subResult = getPermissions(currentUser, document);
                loggerOutgoingDocument.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerIncomingDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        loggerOutgoingDocument.info("Result permissions for user[{}] to document[{}] is {}",
                new Object[]{
                        sessionManagement.getLoggedUser().getId(),
                        document.getId(),
                        result
                }
        );
        return result;
    }

    public Permissions getPermissions(SessionManagementBean sessionManagement, InternalDocument document) {
        if (sessionManagement.isAdministrator()) {
            loggerInternalDocument.info("Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole",
                    sessionManagement.getLoggedUser().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(sessionManagement.getLoggedUser(), document);
        if (sessionManagement.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : sessionManagement.getSubstitutedUsers()) {
                loggerInternalDocument.debug("Get permissions on substituted user [{}] {}",
                        currentUser.getId(), currentUser.getFullName()
                );
                Permissions subResult = getPermissions(currentUser, document);
                loggerInternalDocument.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerInternalDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        loggerInternalDocument.info("Result permissions for user[{}] to document[{}] is {}",
                new Object[]{
                        sessionManagement.getLoggedUser().getId(),
                        document.getId(),
                        result
                }
        );
        return result;
    }

    public Permissions getPermissions(SessionManagementBean sessionManagement, RequestDocument document) {
        if (sessionManagement.isAdministrator()) {
            loggerRequestDocument.info("Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole",
                    sessionManagement.getLoggedUser().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(sessionManagement.getLoggedUser(), document);
        if (sessionManagement.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : sessionManagement.getSubstitutedUsers()) {
                loggerRequestDocument.debug("Get permissions on substituted user [{}] {}",
                        currentUser.getId(), currentUser.getFullName()
                );
                Permissions subResult = getPermissions(currentUser, document);
                loggerRequestDocument.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerRequestDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        loggerRequestDocument.info("Result permissions for user[{}] to document[{}] is {}",
                new Object[]{
                        sessionManagement.getLoggedUser().getId(),
                        document.getId(),
                        result
                }
        );
        return result;
    }
}
