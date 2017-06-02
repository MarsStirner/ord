package ru.entity.model.document;

import org.apache.commons.lang3.StringUtils;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.AccessControlledDocumentEntity;
import ru.entity.model.referenceBook.*;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Обращения граждан
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "request_document")
@Access(AccessType.FIELD)
@AssociationOverrides({
        @AssociationOverride(name = "history", joinTable = @JoinTable(name = "request_document_history",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "entry_id")}
        )),
        @AssociationOverride(name = "personReaders", joinTable = @JoinTable(name = "request_document_person_readers",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "user_id")}
        )),
        @AssociationOverride(name = "personEditors", joinTable = @JoinTable(name = "request_document_person_editors",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "user_id")}
        )),
        @AssociationOverride(name = "roleReaders", joinTable = @JoinTable(name = "request_document_role_readers",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id")}
        )),
        @AssociationOverride(name = "roleEditors", joinTable = @JoinTable(name = "request_document_role_editors",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id")}
        ))
})
public class RequestDocument extends AccessControlledDocumentEntity {

    /**
     * Дата поступления
     */
    @Column(name = "deliveryDate", nullable = true)
    private LocalDateTime deliveryDate;

    /**
     * Срок исполнения
     */
    @Column(name = "executionDate", nullable = true)
    private LocalDateTime executionDate;


    /**
     * Ответсвтенный исполнитель
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id", nullable = true)
    private User responsible;

    /**
     * Тип отправителя
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senderType_id", nullable = true)
    private SenderType senderType;

    /**
     * Корреспондент
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contragent_id", nullable = true)
    private Contragent contragent;

    /**
     * Фамилия
     */
    @Column(name = "senderLastName")
    private String senderLastName;

    /**
     * Имя
     */
    @Column(name = "senderFirstName")
    private String senderFirstName;

    /**
     * Отчество
     */
    @Column(name = "senderMiddleName")
    private String senderMiddleName;

    /**
     * Адресаты
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "request_document_recipient",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "request_document_group_recipient",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "group_id")})
    private Set<Group> recipientGroups;


    /**
     * Регион
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;


    /**
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveryType_id")
    private DeliveryType deliveryType;

    /**
     * Номер поступившего
     */
    @Column(name = "receivedDocumentNumber")
    private String receivedDocumentNumber;

    /**
     * Номер ERP
     */
    @Column(name = "erpNumber")
    private String erpNumber;

    /**
     * Дата регистрации поступившего документа у корреспондента
     */
    @Column(name = "receivedDocumentDate")
    private LocalDateTime receivedDocumentDate;


    @Override
    public DocumentType getType() {
        return DocumentType.RequestDocument;
    }


    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getSenderLastName() {
        return senderLastName;
    }

    public void setSenderLastName(String lastName) {
        this.senderLastName = lastName;
    }

    public String getSenderFirstName() {
        return senderFirstName;

    }

    public void setSenderFirstName(String firstName) {
        this.senderFirstName = firstName;
    }

    public String getSenderMiddleName() {
        return senderMiddleName;
    }

    public void setSenderMiddleName(String middleName) {
        this.senderMiddleName = middleName;
    }

    public String getSenderDescription() {
        final StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(senderLastName)) {
            sb.append(senderLastName);
        }
        if (StringUtils.isNotEmpty(senderFirstName)) {
            if (sb.length() != 0) {
                sb.append(' ');
            }
            sb.append(senderFirstName);
        }
        if (StringUtils.isNotEmpty(senderMiddleName)) {
            if (sb.length() != 0) {
                sb.append(' ');
            }
            sb.append(senderMiddleName);
        }
        return sb.toString();
    }

    public String getSenderDescriptionShort() {
        final StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(senderLastName)) {
            sb.append(senderLastName);
        }
        if (StringUtils.isNotEmpty(senderFirstName)) {
            if (sb.length() != 0) {
                sb.append(' ');
            }
            sb.append(senderFirstName.charAt(0)).append('.');
        }
        if (StringUtils.isNotEmpty(senderMiddleName)) {
            if (sb.length() != 0) {
                sb.append(' ');
            }
            sb.append(senderMiddleName.charAt(0)).append('.');
        }
        return sb.toString();
    }

    public User getResponsible() {
        return responsible;
    }

    public void setResponsible(User responsible) {
        this.responsible = responsible;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getReceivedDocumentNumber() {
        return receivedDocumentNumber;
    }

    public void setReceivedDocumentNumber(String receivedDocumentNumber) {
        this.receivedDocumentNumber = receivedDocumentNumber;
    }

    public LocalDateTime getReceivedDocumentDate() {
        return receivedDocumentDate;
    }

    public void setReceivedDocumentDate(LocalDateTime receivedDocumentDate) {
        this.receivedDocumentDate = receivedDocumentDate;
    }

    public Set<Group> getRecipientGroups() {
        return recipientGroups;
    }

    public void setRecipientGroups(Set<Group> recipientGroups) {
        this.recipientGroups = recipientGroups;
    }

    public List<Group> getRecipientGroupsList() {
        List<Group> in_result = new ArrayList<>();
        if (recipientGroups != null) {
            in_result.addAll(recipientGroups);
        }
        return in_result;
    }

    public Set<User> getRecipientUsers() {
        return recipientUsers;
    }

    public void setRecipientUsers(Set<User> recipientUsers) {
        this.recipientUsers = recipientUsers;
    }

    public List<User> getRecipientUserList() {
        if (recipientUsers != null && !recipientUsers.isEmpty()) {
            return new ArrayList<>(recipientUsers);
        }
        return new ArrayList<>(0);
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Contragent getContragent() {
        return contragent;
    }

    public void setContragent(Contragent contragent) {
        this.contragent = contragent;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }
}