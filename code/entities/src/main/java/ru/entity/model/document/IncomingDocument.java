package ru.entity.model.document;

import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.AccessControlledDocumentEntity;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.referenceBook.Nomenclature;
import ru.entity.model.user.User;
import ru.external.ProcessedData;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Входящий документ
 * По умолчанию все связи - LAZY (используются разные уровни критериев в DAO)
 *
 * @author Alexey Vagizov / Egor Upatov
 */
@Entity
@Table(name = "incoming_document")
@Access(AccessType.FIELD)
@AssociationOverrides({
        @AssociationOverride(name = "history", joinTable = @JoinTable(name = "incoming_document_history",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "entry_id")}
        )),
        @AssociationOverride(name = "personReaders", joinTable = @JoinTable(name = "incoming_document_person_readers",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "user_id")}
        )),
        @AssociationOverride(name = "personEditors", joinTable = @JoinTable(name = "incoming_document_person_editors",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "user_id")}
        )),
        @AssociationOverride(name = "roleReaders", joinTable = @JoinTable(name = "incoming_document_role_readers",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id")}
        )),
        @AssociationOverride(name = "roleEditors", joinTable = @JoinTable(name = "incoming_document_role_editors",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id")}
        ))
})
public class IncomingDocument extends AccessControlledDocumentEntity implements ProcessedData {

    /**
     * Количество приложений
     */
    @Column(name = "appendixiesCount", nullable = false)
    private int appendixiesCount = 0;

    /**
     * Количество экземпляров
     */
    @Column(name = "copiesCount", nullable = false)
    private int copiesCount = 0;

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
     * Номер поступившего
     */
    @Column(name = "receivedDocumentNumber", nullable = true)
    private String receivedDocumentNumber;

    /**
     * Дата регистрации поступившего документа у корреспондента
     */
    @Column(name = "receivedDocumentDate", nullable = true)
    private LocalDateTime receivedDocumentDate;


    /**
     * Количество страниц
     */
    @Column(name = "sheetsCount", nullable = false)
    private int sheetsCount;

    /**
     * Корреспондент
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contragent_id", nullable = true)
    private Contragent contragent;

    /**
     * Номенклатура
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id")
    private Nomenclature nomenclature;

    /**
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveryType_id", nullable = false)
    private DeliveryType deliveryType;


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Связанные сущности
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Адресаты
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "incoming_document_recipient",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "incoming_document_group_recipient",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "group_id")})
    private Set<Group> recipientGroups;


    /**
     * Исполнители
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "incoming_document_executor",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> executors;

    /**
     * END OF DATABASE FIELDS *****************************************************************************************
     */


    public Set<User> getRecipientUsers() {
        return recipientUsers;
    }

    public void setRecipientUsers(Set<User> recipientUsers) {
        this.recipientUsers = recipientUsers;
    }

    public List<User> getRecipientUserList() {
        if (recipientUsers != null && !recipientUsers.isEmpty()) {
            final ArrayList<User> result = new ArrayList<>(recipientUsers);
            Collections.sort(result);
            return result;
        }
        return new ArrayList<>(0);
    }

    public Contragent getContragent() {
        return contragent;
    }

    public void setContragent(Contragent contragent) {
        this.contragent = contragent;
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

    public Set<User> getExecutors() {
        return executors;
    }

    public void setExecutors(Set<User> executors) {
        this.executors = executors;
    }

    public List<User> getExecutorsList() {
        if (executors != null && !executors.isEmpty()) {
            return new ArrayList<>(executors);
        }
        return new ArrayList<>(0);
    }

    public Nomenclature getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(Nomenclature nomenclature) {
        this.nomenclature = nomenclature;
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

    public int getCopiesCount() {
        return copiesCount;
    }

    public void setCopiesCount(int copiesCount) {
        this.copiesCount = copiesCount;
    }

    public int getSheetsCount() {
        return sheetsCount;
    }

    public void setSheetsCount(int sheetsCount) {
        this.sheetsCount = sheetsCount;
    }

    public int getAppendixiesCount() {
        return appendixiesCount;
    }

    public void setAppendixiesCount(int appendixiesCount) {
        this.appendixiesCount = appendixiesCount;
    }

    public DocumentType getType() {
        return DocumentType.IncomingDocument;
    }

    public Set<Group> getRecipientGroups() {
        return recipientGroups;
    }

    public void setRecipientGroups(Set<Group> recipientGroups) {
        this.recipientGroups = recipientGroups;
    }

    /**
     * JSF-specific method cause we cannot iterate on Set (http://sfjsf.blogspot.ru/2006/03/usings-sets-with-uidata.html)
     *
     * @return List<Group>
     */
    public List<Group> getRecipientGroupsList() {
        if (recipientGroups == null || recipientGroups.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(recipientGroups);
        }
    }
}