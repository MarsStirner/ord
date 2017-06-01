package ru.entity.model.document;

import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.AccessControlledDocumentEntity;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.user.User;
import ru.external.ProcessedData;

import javax.persistence.*;
import java.time.LocalDateTime;


/**
 * Исходящий документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "outgoing_document")
@Access(AccessType.FIELD)
@AssociationOverrides({
        @AssociationOverride(name = "history", joinTable = @JoinTable(name = "outgoing_document_history",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "entry_id")}
        )),
        @AssociationOverride(name = "personReaders", joinTable = @JoinTable(name = "outgoing_document_person_readers",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "user_id")}
        )),
        @AssociationOverride(name = "personEditors", joinTable = @JoinTable(name = "outgoing_document_person_editors",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "user_id")}
        )),
        @AssociationOverride(name = "roleReaders", joinTable = @JoinTable(name = "outgoing_document_role_readers",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id")}
        )),
        @AssociationOverride(name = "roleEditors", joinTable = @JoinTable(name = "outgoing_document_role_editors",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id")}
        ))
})
public class OutgoingDocument extends AccessControlledDocumentEntity implements ProcessedData {

    /**
     * Количество приложений
     */
    @Column(name = "appendixiesCount")
    private int appendixiesCount;

    /**
     * Количество экземпляров
     */
    @Column(name = "copiesCount")
    private int copiesCount;

    /**
     * Количество страниц
     */
    @Column(name = "sheetsCount")
    private int sheetsCount;

    /**
     * Дата подписания
     */
    @Column(name = "signatureDate")
    private LocalDateTime signatureDate;

    /**
     * Тип доставки
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveryType_id", nullable = true)
    private DeliveryType deliveryType;

    /**
     * Ссылка на документ основание
     */
    @Column(name = "reason_document_id")
    private String reasonDocumentId;

    /**
     * Адресат -контрагент
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contragent_id")
    private Contragent contragent;

    /**
     * Исполнитель
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id", nullable = true)
    private User executor;

    public Contragent getContragent() {
        return contragent;
    }

    public void setContragent(final Contragent contragent) {
        this.contragent = contragent;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
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

    public User getExecutor() {
        return executor;
    }

    public void setExecutor(User executor) {
        this.executor = executor;
    }

    public LocalDateTime getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(LocalDateTime signatureDate) {
        this.signatureDate = signatureDate;
    }

    @Override
    public DocumentType getType() {
        return DocumentType.OutgoingDocument;
    }

    public String getReasonDocumentId() {
        return reasonDocumentId;
    }

    public void setReasonDocumentId(String reasonDocumentId) {
        this.reasonDocumentId = reasonDocumentId;
    }

}