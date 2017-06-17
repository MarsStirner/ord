package ru.efive.dms.uifaces.beans.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.document.*;
import ru.entity.model.enums.*;
import ru.entity.model.referenceBook.*;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;
import ru.hitsl.sql.dao.interfaces.document.*;
import ru.hitsl.sql.dao.interfaces.referencebook.*;
import ru.util.ApplicationHelper;

import java.util.List;

/**
 * Created by EUpatov on 05.04.2017.
 */
@Component("refBookHelper")
@Transactional(value = "ordTransactionManager", propagation = Propagation.REQUIRED, readOnly = true)
public class ReferenceBookHelper {
    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;

    @Autowired
    @Qualifier("deliveryTypeDao")
    private DeliveryTypeDao deliveryTypeDao;

    @Autowired
    @Qualifier("userAccessLevelDao")
    private UserAccessLevelDao userAccessLevelDao;

    @Autowired
    @Qualifier("senderTypeDao")
    private SenderTypeDao senderTypeDao;

    @Autowired
    @Qualifier("incomingDocumentDao")
    private IncomingDocumentDao incomingDocumentDao;

    @Autowired
    @Qualifier("taskDao")
    private TaskDao taskDao;

    @Autowired
    @Qualifier("requestDocumentDao")
    private RequestDocumentDao requestDocumentDao;

    @Autowired
    @Qualifier("internalDocumentDao")
    private InternalDocumentDao internalDocumentDao;

    @Autowired
    @Qualifier("outgoingDocumentDao")
    private OutgoingDocumentDao outgoingDocumentDao;

    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;

    @Autowired
    @Qualifier("documentTypeDao")
    private DocumentTypeDao documentTypeDao;

    @Autowired
    @Qualifier("groupTypeDao")
    private GroupTypeDao groupTypeDao;

    @Autowired
    @Qualifier("roleDao")
    private RoleDao roleDao;

    @Autowired
    @Qualifier("contactInfoTypeDao")
    private ContactInfoTypeDao contactInfoTypeDao;

    @Autowired
    @Qualifier("contragentTypeDao")
    private ContragentTypeDao contragentTypeDao;

    public String getUserFullNameById(int id) {
        final User user = userDao.getItemBySimpleCriteria(id);
        return user != null ? user.getDescription() : "";
    }

    public List<DocumentForm> getDocumentFormsByCategory(String category) {
        return documentFormDao.findByDocumentTypeCode(category);
    }

    public List<DocumentForm> getDocumentForms() {
        return documentFormDao.getItems();
    }

    public List<DeliveryType> getDeliveryTypes() {
        return deliveryTypeDao.getItems();
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypeDao.getItems();
    }

    public List<Role> getRoles() {
        return roleDao.getItems();
    }

    public List<GroupType> getGroupTypes() {
        return groupTypeDao.getItems();
    }

    public List<RbContactPointSystem> getContactTypes() {
        return contactInfoTypeDao.getItems();
    }

    public List<UserAccessLevel> getUserAccessLevels() {
        return userAccessLevelDao.getItems();
    }

    public List<UserAccessLevel> getUserAccessLevelsGreaterOrEqualMaxValue(int level) {
        return userAccessLevelDao.findLowerThenLevel(level);
    }

    public List<SenderType> getSenderTypes() {
        return senderTypeDao.getItems();
    }

    public List<ContragentType> getContragentTypes() {
        return contragentTypeDao.getItems();
    }

    public String getLinkDescriptionByUniqueId(String documentKey) {
        if (!documentKey.isEmpty()) {
            final Integer rootDocumentId = ApplicationHelper.getIdFromUniqueIdString(documentKey);
            if (StringUtils.startsWith(documentKey, DocumentType.INCOMING.getCode())) {
                final IncomingDocument in_doc = incomingDocumentDao.getItemBySimpleCriteria(rootDocumentId);
                if (in_doc != null) {
                    return (in_doc.getRegistrationNumber() == null || in_doc.getRegistrationNumber().equals("") ? "Черновик входщяего документа от " + ApplicationHelper
                            .formatDate(in_doc.getCreationDate()) : "Входящий документ № " + in_doc
                            .getRegistrationNumber() + " от " + ApplicationHelper.formatDate(in_doc.getRegistrationDate()));
                } else {
                    return "";
                }

            } else if (StringUtils.startsWith(documentKey, DocumentType.OUTGOING.getCode())) {
                final OutgoingDocument out_doc = outgoingDocumentDao.getItemBySimpleCriteria(rootDocumentId);
                if (out_doc != null) {
                    return (out_doc.getRegistrationNumber() == null || out_doc.getRegistrationNumber().equals("") ? "Черновик исходящего документа от " + ApplicationHelper
                            .formatDate(out_doc.getCreationDate()) : "Исходящий документ № " + out_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(out_doc.getRegistrationDate()));
                } else {
                    return "";
                }

            } else if (StringUtils.startsWith(documentKey, DocumentType.INTERNAL.getCode())) {
                final InternalDocument internal_doc = internalDocumentDao.getItemBySimpleCriteria(rootDocumentId);
                if (internal_doc != null) {
                    return (internal_doc.getRegistrationNumber() == null || internal_doc.getRegistrationNumber().equals("") ? "Черновик внутреннего документа от " + ApplicationHelper.formatDate(
                            internal_doc.getCreationDate()
                    ) : "Внутренний документ № " + internal_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                            internal_doc.getRegistrationDate()
                    ));
                } else {
                    return "";
                }

            } else if (StringUtils.startsWith(documentKey, DocumentType.REQUEST.getCode())) {
                final RequestDocument request_doc = requestDocumentDao.getItemBySimpleCriteria(rootDocumentId);
                if (request_doc != null) {
                    return (request_doc.getRegistrationNumber() == null || request_doc.getRegistrationNumber().equals("") ? "Черновик обращения граждан от " + ApplicationHelper.formatDate(
                            request_doc.getCreationDate()
                    ) : "Обращение граждан № " + request_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                            request_doc.getRegistrationDate()
                    ));
                } else {
                    return "";
                }

            } else if (StringUtils.startsWith(documentKey, DocumentType.TASK.getCode())) {
                final Task task_doc = taskDao.getItemBySimpleCriteria(rootDocumentId);
                if (task_doc != null) {
                    return (task_doc.getRegistrationNumber() == null || task_doc.getRegistrationNumber().equals("") ? "Черновик поручения от " + ApplicationHelper
                            .formatDate(task_doc.getCreationDate()) : "Поручение № " + task_doc.getRegistrationNumber() + " от " + ApplicationHelper.formatDate(
                            task_doc.getCreationDate()
                    ));
                } else {
                    return "";
                }
            }
        }
        return "";
    }


}
