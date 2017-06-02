package ru.entity.model.document;

import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.AccessControlledDocumentEntity;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Внутренний документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "internal_document")
@Access(AccessType.FIELD)
@AssociationOverrides({
        @AssociationOverride(name = "history", joinTable = @JoinTable(name = "internal_document_history",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "entry_id")}
        )),
        @AssociationOverride(name = "personReaders", joinTable = @JoinTable(name = "internal_document_person_readers",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "user_id")}
        )),
        @AssociationOverride(name = "personEditors", joinTable = @JoinTable(name = "internal_document_person_editors",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "user_id")}
        )),
        @AssociationOverride(name = "roleReaders", joinTable = @JoinTable(name = "internal_document_role_readers",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id")}
        )),
        @AssociationOverride(name = "roleEditors", joinTable = @JoinTable(name = "internal_document_role_editors",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id")}
        ))
})
public class InternalDocument extends AccessControlledDocumentEntity {

    /**
     * Срок исполнения
     */
    @Column(name = "executionDate")
    private LocalDateTime executionDate;

    /**
     * Ответственный , следит за сроками исполнения документов и пинает, если исполнители не успевают.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id", nullable = true)
    private User responsible;


    /**
     * Дата подписания
     */
    @Column(name = "signatureDate")
    private LocalDate signatureDate;


    /**
     * Регистрация изменений закрытого периода
     */
    @Column(name = "closePeriodRegistrationFlag")
    private boolean closePeriodRegistrationFlag = false;


    /**
     * Номер ERP
     */
    @Column(name = "erpNumber")
    private String erpNumber;

    /**
     * Адресаты
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "internal_document_recipient",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "internal_document_group_recipient",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "group_id")})
    private Set<Group> recipientGroups;



    public boolean isClosePeriodRegistrationFlag() {
        return closePeriodRegistrationFlag;
    }

    public void setClosePeriodRegistrationFlag(boolean closePeriodRegistrationFlag) {
        this.closePeriodRegistrationFlag = closePeriodRegistrationFlag;
    }

    public String getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(String erpNumber) {
        this.erpNumber = erpNumber;
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

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public LocalDate getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(LocalDate signatureDate) {
        this.signatureDate = signatureDate;
    }

    public User getResponsible() {
        return responsible;
    }

    public void setResponsible(User responsible) {
        this.responsible = responsible;
    }

    @Override
    public DocumentType getType() {
        return DocumentType.InternalDocument;
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

}