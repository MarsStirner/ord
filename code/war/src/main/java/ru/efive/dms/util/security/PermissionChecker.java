package ru.efive.dms.util.security;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.entity.model.document.*;
import ru.entity.model.user.User;
import ru.external.AuthorizationData;
import ru.hitsl.sql.dao.*;
import ru.util.ApplicationHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.efive.dms.util.security.Permissions.*;
import static ru.efive.dms.util.security.Permissions.Permission.READ;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.*;

/**
 * Author: Upatov Egor <br>
 * Date: 12.11.2014, 12:37 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для проверки прав доступа к документам \ поручениям
 */
@Named("permissionChecker")
@ApplicationScoped
public class PermissionChecker implements Serializable {


    //Логгеры
    private static final Logger loggerIncomingDocument = LoggerFactory.getLogger("INCOMING_DOCUMENT");
    private static final Logger loggerOutgoingDocument = LoggerFactory.getLogger("OUTGOING_DOCUMENT");
    private static final Logger loggerInternalDocument = LoggerFactory.getLogger("INTERNAL_DOCUMENT");
    private static final Logger loggerRequestDocument = LoggerFactory.getLogger("REQUEST_DOCUMENT");
    private static final Logger loggerTask = LoggerFactory.getLogger("TASK");
    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagementBean;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // По User
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
         * Редактирование документа: 1-2-3-4-6-8
         * Действия: 1-2-3-8
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
        //3) исполнитель
        if (document.getExecutors().contains(user)) {
            loggerIncomingDocument.debug("{}:Permission RXW granted: Executor", document.getId());
            return ALL_PERMISSIONS;
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            loggerIncomingDocument.debug("{}:Permission RWX granted: Controller", document.getId());
            return ALL_PERMISSIONS;
        }
        // 4) список пользователей на редактирование
        if (document.getPersonEditors() != null && document.getPersonEditors().contains(user)) {
            loggerIncomingDocument.debug("{}:Permission RW granted: PersonEditor", document.getId());
            return RW_PERMISSIONS;
        }
        // 6) список ролей на редактирование
        if (document.getRoleEditors() != null && user.getRoles() != null && CollectionUtils.containsAny(document.getRoleEditors(), user.getRoles())) {
            loggerIncomingDocument.debug("{}:Permission RW granted: RoleEditor", document.getId());
            return RW_PERMISSIONS;
        }

        // 5) список пользователей на просмотр
        if (document.getPersonReaders() != null && document.getPersonReaders().contains(user)) {
            loggerIncomingDocument.debug("{}:Permission R granted: PersonReader", document.getId());
            return R_PERMISSIONS;
        }
        // 7) список ролей на просмотр
        if (document.getRoleReaders() != null && user.getRoles() != null && CollectionUtils.containsAny(document.getRoleReaders(), user.getRoles())) {
            loggerIncomingDocument.debug("{}:Permission R granted: RoleReader", document.getId());
            return R_PERMISSIONS;
        }

        // 9) пользователи адресаты
        if (document.getRecipientUsers() != null && document.getRecipientUsers().contains(user)) {
            loggerIncomingDocument.debug("{}:Permission R granted: RecipientUser", document.getId());
            return R_PERMISSIONS;
        }

        //10) группы адресаты
        if (document.getRecipientGroups() != null && user.getGroups() != null && CollectionUtils.containsAny(
                document.getRecipientGroups(), user.getGroups()
        )) {
            loggerIncomingDocument.debug("{}:Permission R granted: RecipientGroup", document.getId());
            return R_PERMISSIONS;
        }
        return EMPTY_PERMISSIONS;
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
        //4) Контролер
        if (user.equals(document.getController())) {
            loggerTask.debug("{}:Permission RWX granted: Controller", document.getId());
            return ALL_PERMISSIONS;
        }
        //5) исполнитель
        if (document.getExecutors() != null && document.getExecutors().contains(user)) {
            loggerTask.debug("{}:Permission RWX granted: Executor", document.getId());
            return ALL_PERMISSIONS;
        }
        // подтянуть права из документа-основания
        if (StringUtils.isNotEmpty(document.getRootDocumentId())) {
            loggerTask.debug("Try to get permissions from rootDocument[{}}", document.getRootDocumentId());
            final Permissions result = getPermissionsFromExternalDocument(user, document.getRootDocumentId(), loggerTask);
            loggerTask.debug("Total permissions from RootDocument for Task[{}]: {}", document.getId(), result);
            return result;
        }
        return EMPTY_PERMISSIONS;
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
         * Редактирование документа: 1-2-3-4-6-8
         * Действия: 1-2-3-8
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
        //3) исполнитель
        if (user.equals(document.getExecutor())) {
            loggerOutgoingDocument.debug("{}:Permission RWX granted: Executor", document.getId());
            return ALL_PERMISSIONS;
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            loggerOutgoingDocument.debug("{}:Permission RWX granted: Controller", document.getId());
            return ALL_PERMISSIONS;
        }

        // 4) список пользователей на редактирование
        if (document.getPersonEditors() != null && document.getPersonEditors().contains(user)) {
            loggerOutgoingDocument.debug("{}:Permission RW granted: PersonEditor", document.getId());
            return RW_PERMISSIONS;
        }
        // 6) список ролей на редактирование
        if (document.getRoleEditors() != null && user.getRoles() != null && CollectionUtils.containsAny(document.getRoleEditors(), user.getRoles())) {
            loggerOutgoingDocument.debug("{}:Permission RW granted: RoleEditor", document.getId());
            return RW_PERMISSIONS;
        }

        // 5) список пользователей на просмотр
        if (document.getPersonReaders() != null && document.getPersonReaders().contains(user)) {
            loggerOutgoingDocument.debug("{}:Permission R granted: PersonReader", document.getId());
            return R_PERMISSIONS;
        }
        // 7) список ролей на просмотр
        if (document.getRoleReaders() != null && user.getRoles() != null && CollectionUtils.containsAny(document.getRoleReaders(), user.getRoles())) {
            loggerOutgoingDocument.debug("{}:Permission R granted: RoleReader", document.getId());
            return R_PERMISSIONS;
        }

        //9) пользователи адресаты
        // Отсутствуют
        //10) группы адресаты
        // Отсутствуют

        return EMPTY_PERMISSIONS;
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
         * 3) исполнители (NULL)\ Тут будет ответственный
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
         * Редактирование документа: 1-2-3-4-6-8
         * Действия: 1-2-3-8
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
        // 3) Ответстенный
        if (user.equals(document.getResponsible())) {
            loggerInternalDocument.debug("{}:Permission RWX granted: Responsible", document.getId());
            return ALL_PERMISSIONS;
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            loggerInternalDocument.debug("{}:Permission RWX granted: Signer", document.getId());
            return ALL_PERMISSIONS;
        }

        // 4) список пользователей на редактирование
        if (document.getPersonEditors() != null && document.getPersonEditors().contains(user)) {
            loggerInternalDocument.debug("{}:Permission RW granted: PersonEditor", document.getId());
            return RW_PERMISSIONS;
        }
        // 6) список ролей на редактирование
        if (document.getRoleEditors() != null && user.getRoles() != null && CollectionUtils.containsAny(document.getRoleEditors(), user.getRoles())) {
            loggerInternalDocument.debug("{}:Permission RW granted: RoleEditor", document.getId());
            return RW_PERMISSIONS;
        }

        // 5) список пользователей на просмотр
        if (document.getPersonReaders() != null && document.getPersonReaders().contains(user)) {
            loggerInternalDocument.debug("{}:Permission R granted: PersonReader", document.getId());
            return R_PERMISSIONS;
        }
        // 7) список ролей на просмотр
        if (document.getRoleReaders() != null && user.getRoles() != null && CollectionUtils.containsAny(document.getRoleReaders(), user.getRoles())) {
            loggerInternalDocument.debug("{}:Permission R granted: RoleReader", document.getId());
            return R_PERMISSIONS;
        }

        // 9) пользователи адресаты
        if (document.getRecipientUsers() != null && document.getRecipientUsers().contains(user)) {
            loggerInternalDocument.debug("{}:Permission R granted: RecipientUser", document.getId());
            return R_PERMISSIONS;
        }

        //10) группы адресаты
        if (document.getRecipientGroups() != null && user.getGroups() != null && CollectionUtils.containsAny(
                document.getRecipientGroups(), user.getGroups()
        )) {
            loggerInternalDocument.debug("{}:Permission R granted: RecipientGroup", document.getId());
            return R_PERMISSIONS;
        }
        return EMPTY_PERMISSIONS;
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
         * Редактирование документа: 1-2-3-4-6-8
         * Действия: 1-2-3-8
         */
        //1) админ
        if (user.isAdministrator()) {
            loggerRequestDocument.debug("{}:Permission RWX granted: AdminRole", document.getId());
            return ALL_PERMISSIONS;
        }
        //2) автор
        if (user.equals(document.getAuthor())) {
            loggerRequestDocument.debug("{}:Permission RWX granted: Author", document.getId());
            return ALL_PERMISSIONS;
        }
        //3) исполнитель
        if (user.equals(document.getResponsible())) {
            loggerRequestDocument.debug("{}:Permission RWX granted: Responsible", document.getId());
            return ALL_PERMISSIONS;
        }
        //8) руководитель
        if (user.equals(document.getController())) {
            loggerRequestDocument.debug("{}:Permission RWX granted: Controller", document.getId());
            return ALL_PERMISSIONS;
        }

        // 4) список пользователей на редактирование
        if (document.getPersonEditors() != null && document.getPersonEditors().contains(user)) {
            loggerRequestDocument.debug("{}:Permission RW granted: PersonEditor", document.getId());
            return RW_PERMISSIONS;
        }
        // 6) список ролей на редактирование
        if (document.getRoleEditors() != null && user.getRoles() != null && CollectionUtils.containsAny(document.getRoleEditors(), user.getRoles())) {
            loggerRequestDocument.debug("{}:Permission RW granted: RoleEditor", document.getId());
            return RW_PERMISSIONS;
        }

        // 5) список пользователей на просмотр
        if (document.getPersonReaders() != null && document.getPersonReaders().contains(user)) {
            loggerRequestDocument.debug("{}:Permission R granted: PersonReader", document.getId());
            return R_PERMISSIONS;
        }
        // 7) список ролей на просмотр
        if (document.getRoleReaders() != null && user.getRoles() != null && CollectionUtils.containsAny(document.getRoleReaders(), user.getRoles())) {
            loggerRequestDocument.debug("{}:Permission R granted: RoleReader", document.getId());
            return R_PERMISSIONS;
        }

        // 9) пользователи адресаты
        if (document.getRecipientUsers() != null && document.getRecipientUsers().contains(user)) {
            loggerRequestDocument.debug("{}:Permission R granted: RecipientUser", document.getId());
            return R_PERMISSIONS;
        }

        //10) группы адресаты
        if (document.getRecipientGroups() != null && user.getGroups() != null && CollectionUtils.containsAny(
                document.getRecipientGroups(), user.getGroups()
        )) {
            loggerRequestDocument.debug("{}:Permission R granted: RecipientGroup", document.getId());
            return R_PERMISSIONS;
        }
        return EMPTY_PERMISSIONS;
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
                final IncomingDocument rootDocument = ((IncomingDocumentDAOImpl) indexManagementBean.getContext().getBean(INCOMING_DOCUMENT_FORM_DAO))
                        .getItemById(rootDocumentId);
                if (rootDocument != null) {
                    final Permissions fromRootDocument = getPermissions(user, rootDocument);
                    logger.debug("ROOT_DOC<incoming[{}]> permissions: {}", rootDocumentId, fromRootDocument);
                    return fromRootDocument;
                } else {
                    logger.warn("ROOT_DOC<incoming[{}]> - not found", rootDocumentId);
                    return Permissions.EMPTY_PERMISSIONS;
                }
            } else if (documentKey.contains("outgoing")) {
                final OutgoingDocument rootDocument = ((OutgoingDocumentDAOImpl) indexManagementBean.getContext().getBean(OUTGOING_DOCUMENT_FORM_DAO))
                        .getItemById(rootDocumentId);
                if (rootDocument != null) {
                    final Permissions fromRootDocument = getPermissions(user, rootDocument);
                    logger.debug("ROOT_DOC<outgoing[{}]> permissions: {}", rootDocumentId, fromRootDocument);
                    return fromRootDocument;
                } else {
                    logger.warn("ROOT_DOC<outgoing[{}]> - not found", rootDocumentId);
                    return Permissions.EMPTY_PERMISSIONS;
                }
            } else if (documentKey.contains("internal")) {
                final InternalDocument rootDocument = ((InternalDocumentDAOImpl) indexManagementBean.getContext().getBean(INTERNAL_DOCUMENT_FORM_DAO))
                        .getItemById(rootDocumentId);
                if (rootDocument != null) {
                    final Permissions fromRootDocument = getPermissions(user, rootDocument);
                    logger.debug("ROOT_DOC<internal[{}]> permissions: {}", rootDocumentId, fromRootDocument);
                    return fromRootDocument;
                } else {
                    logger.warn("ROOT_DOC<internal[{}]> - not found", rootDocumentId);
                    return Permissions.EMPTY_PERMISSIONS;
                }
            } else if (documentKey.contains("request")) {
                final RequestDocument rootDocument = ((RequestDocumentDAOImpl) indexManagementBean.getContext().getBean(REQUEST_DOCUMENT_FORM_DAO))
                        .getItemById(rootDocumentId);
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // По AuthorizationData
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Получения прав текущего авторизованного пользователя (вместе со всей инфой)
     *
     * @param auth     информация об авторизованном пользователе
     * @param document входящий документ, на который проверяем права
     * @return суммарный набор прав для текущих данных по авторизации
     */
    public Permissions getPermissions(final AuthorizationData auth, final IncomingDocument document) {
        if (auth.isAdministrator()) {
            loggerIncomingDocument.info(
                    "Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole", auth.getAuthorized().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(auth.getAuthorized(), document);
        if (auth.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : auth.getSubstitutedUsers()) {
                loggerIncomingDocument.debug("Get permissions on substituted user [{}] {}", currentUser.getId(), currentUser.getDescription());
                Permissions subResult = getPermissions(currentUser, document);
                loggerIncomingDocument.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerIncomingDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        if (!result.hasPermission(READ)) {
            //11) пользователи, учавствующие в согласованиях
            if (((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(auth, document.getUniqueId())) {
                loggerIncomingDocument.debug("{}:Permission R granted: TASK", document.getId());
                result.addPermission(READ);
            }
        }
        loggerIncomingDocument.info("Result permissions for user[{}] to document[{}] is {}", auth.getAuthorized().getId(), document.getId(), result);
        return result;
    }

    /**
     * Получения прав текущего авторизованного пользователя (вместе со всей инфой)
     *
     * @param auth     информация об авторизованном пользователе
     * @param document входящий документ, на который проверяем права
     * @return суммарный набор прав для текущих данных по авторизации
     */
    public Permissions getPermissions(AuthorizationData auth, Task document) {
        if (auth.isAdministrator()) {
            loggerTask.info("Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole", auth.getAuthorized().getId(), document.getId());
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(auth.getAuthorized(), document);
        if (auth.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : auth.getSubstitutedUsers()) {
                loggerTask.debug("Get permissions on substituted user [{}] {}", currentUser.getId(), currentUser.getDescription());
                Permissions subResult = getPermissions(currentUser, document);
                loggerTask.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerTask.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        if (!result.hasPermission(READ)) {
            //11) пользователи, учавствующие в согласованиях
            if (((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(auth, document.getUniqueId())) {
                loggerTask.debug("{}:Permission R granted: TASK", document.getId());
                result.addPermission(READ);
            }
        }
        loggerTask.info("Result permissions for user[{}] to document[{}] is {}", auth.getAuthorized().getId(), document.getId(), result);
        return result;
    }

    public Permissions getPermissions(AuthorizationData auth, OutgoingDocument document) {
        if (auth.isAdministrator()) {
            loggerOutgoingDocument.info(
                    "Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole", auth.getAuthorized().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(auth.getAuthorized(), document);
        if (auth.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : auth.getSubstitutedUsers()) {
                loggerOutgoingDocument.debug("Get permissions on substituted user [{}] {}", currentUser.getId(), currentUser.getDescription());
                Permissions subResult = getPermissions(currentUser, document);
                loggerOutgoingDocument.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerOutgoingDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        if (!result.hasPermission(READ)) {
            //11) пользователи, учавствующие в согласованиях
            if (((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(auth, document.getUniqueId())) {
                loggerOutgoingDocument.debug("{}:Permission R granted: TASK", document.getId());
                result.addPermission(READ);
            }
        }
        loggerOutgoingDocument.info("Result permissions for user[{}] to document[{}] is {}", auth.getAuthorized().getId(), document.getId(), result);
        return result;
    }

    public Permissions getPermissions(AuthorizationData auth, InternalDocument document) {
        if (auth.isAdministrator()) {
            loggerInternalDocument.info(
                    "Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole", auth.getAuthorized().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(auth.getAuthorized(), document);
        if (auth.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : auth.getSubstitutedUsers()) {
                loggerInternalDocument.debug("Get permissions on substituted user [{}] {}", currentUser.getId(), currentUser.getDescription());
                Permissions subResult = getPermissions(currentUser, document);
                loggerInternalDocument.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerInternalDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        if (!result.hasPermission(READ)) {
            //11) пользователи, учавствующие в согласованиях
            if (((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(auth, document.getUniqueId())) {
                loggerInternalDocument.debug("{}:Permission R granted: TASK", document.getId());
                result.addPermission(READ);
            }
        }
        loggerInternalDocument.info("Result permissions for user[{}] to document[{}] is {}", auth.getAuthorized().getId(), document.getId(), result);
        return result;
    }

    public Permissions getPermissions(AuthorizationData auth, RequestDocument document) {
        if (auth.isAdministrator()) {
            loggerRequestDocument.info(
                    "Result permissions for user[{}] to document[{}] is ALL, granted by: AdminRole", auth.getAuthorized().getId(), document.getId()
            );
            return ALL_PERMISSIONS;
        }
        final Permissions result = getPermissions(auth.getAuthorized(), document);
        if (auth.isSubstitution() && !result.hasAllPermissions()) {
            for (User currentUser : auth.getSubstitutedUsers()) {
                loggerRequestDocument.debug("Get permissions on substituted user [{}] {}", currentUser.getId(), currentUser.getDescription());
                Permissions subResult = getPermissions(currentUser, document);
                loggerRequestDocument.debug("Sub permissions: {}", subResult.toString());
                result.mergePermissions(subResult);
                if (result.hasAllPermissions()) {
                    loggerRequestDocument.debug("Reached ALL permissions on this substitution");
                    break;
                }
            }
        }
        if (!result.hasPermission(READ)) {
            //11) пользователи, учавствующие в согласованиях
            if (((TaskDAOImpl) indexManagementBean.getContext().getBean(TASK_DAO)).isAccessGrantedByAssociation(auth, document.getUniqueId())) {
                loggerRequestDocument.debug("{}:Permission R granted: TASK", document.getId());
                result.addPermission(READ);
            }
        }
        loggerRequestDocument.info("Result permissions for user[{}] to document[{}] is {}", auth.getAuthorized().getId(), document.getId(), result);
        return result;
    }
}
