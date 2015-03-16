package ru.entity.model.document;

import org.hibernate.annotations.*;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.entity.model.user.UserAccessLevel;
import ru.entity.model.wf.HumanTaskTree;
import ru.external.AgreementIssue;
import ru.external.ProcessedData;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

/**
 * Внутренний документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_internal_documents")
public class InternalDocument extends IdentifiedEntity implements ProcessedData, AgreementIssue {
    private static final long serialVersionUID = -7971345050896379926L;

    /**
     * Количество приложений
     */
    @Column(name = "appendixiesCount", nullable = false)
    private int appendixiesCount;

    /**
     * Количество экземпляров
     */
    @Column(name = "copiesCount", nullable = false)
    private int copiesCount;

    /**
     * Дата создания документа
     */
    @Column(name = "creationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Удален ли документ
     */
    @Column(name = "deleted")
    private boolean deleted;

    /**
     * Срок исполнения
     */
    @Column(name = "executionDate")
    @Temporal(value = TemporalType.DATE)
    private Date executionDate;

    /**
     * регистрационный номер документа
     */
    @Column(name = "registrationNumber")
    private String registrationNumber;

    /**
     * Количество страниц
     */
    @Column(name = "sheetsCount")
    private int sheetsCount;

    /**
     * Краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text")
    private String shortDescription;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    /**
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "form_id")
    private DocumentForm form;

    /**
     * Инциатор документа (автор)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Ответственный , следит за сроками исполнения документов и пинает, если исполнители не успевают.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_id")
    private User responsible;


    /**
     * Дата регистрации
     */
    @Column(name = "registrationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date registrationDate;

    /**
     * Дата подписания
     */
    @Column(name = "signatureDate")
    @Temporal(value = TemporalType.DATE)
    private Date signatureDate;

    /**
     * Подписант
     */
    //TODO руководитель ?
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "signer_id")
    private User signer;


    /**
     * Уровень допуска
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userAccessLevel_id", nullable = true)
    private UserAccessLevel userAccessLevel;


    /**
     * Регистрация изменений закрытого периода
     */
    @Column(name="closePeriodRegistrationFlag")
    private boolean closePeriodRegistrationFlag = false;


    /**
     * Номер ERP
     */
    @Column(name = "erpNumber")
    private String erpNumber;


    @Temporal(value = TemporalType.TIMESTAMP)
    private Date factDate;


    /**
     * Адресаты (пользователи)
     */
    @ManyToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_recipients",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientUsers_id")})
    private List<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany
    @JoinTable(name = "dms_internal_documents_recipient_groups",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientGroups_id")})
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<Group> recipientGroups;


    /**
     * Пользователи-читатели
     */
    @ManyToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_person_readers")
    @IndexColumn(name = "ID1")
    private List<User> personReaders;

    /**
     * Пользователи-редакторы
     */
    @ManyToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_person_editors")
    @IndexColumn(name = "ID1")
    private List<User> personEditors;


    /**
     * Пользователи-согласующие
     */
    @ManyToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_agreementUsers")
    private Set<User> agreementUsers;

    /**
     * Роли-читатели
     */
    @ManyToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_role_readers")
    @IndexColumn(name = "ID2")
    private List<Role> roleReaders;

    /**
     * Роли-редакторы
     */
    @ManyToMany
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_role_editors")
    @IndexColumn(name = "ID2")
    private List<Role> roleEditors;

    @Transient
    private String WFResultDescription;


    public boolean isClosePeriodRegistrationFlag() {
        return closePeriodRegistrationFlag;
    }

    public void setClosePeriodRegistrationFlag(boolean closePeriodRegistrationFlag) {
        this.closePeriodRegistrationFlag = closePeriodRegistrationFlag;
    }


    /**
     * История
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "dms_internal_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<HistoryEntry> history;

    /**
     * Дерево согласования
     */
    @OneToOne
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_incoming_document_agreement_tree",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "tree_id"))
    @LazyToOne(LazyToOneOption.FALSE)
    private HumanTaskTree agreementTree;


    public String getWFResultDescription() {
        return this.WFResultDescription;
    }

    public void setWFResultDescription(String WFResultDescription) {
        this.WFResultDescription = WFResultDescription;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(String erpNumber) {
        this.erpNumber = erpNumber;
    }

    public List<User> getRecipientUsers() {
        return recipientUsers;
    }

    public void setRecipientUsers(List<User> recipientUsers) {
        this.recipientUsers = recipientUsers;
    }


    public List<User> getRecipientUsersList() {
        List<User> result = new ArrayList<User>();
        if (recipientUsers != null) {
            result.addAll(recipientUsers);
        }
        return result;
    }


    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public Date getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(Date signatureDate) {
        this.signatureDate = signatureDate;
    }

    public User getSigner() {
        return signer;
    }

    public void setSigner(User signer) {
        this.signer = signer;
    }

    public User getResponsible() {
        return responsible;
    }

    public void setResponsible(User responsible) {
        this.responsible = responsible;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }


    public Date getCreationDate() {
        return creationDate;
    }

    public void setForm(DocumentForm form) {
        this.form = form;
    }

    public DocumentForm getForm() {
        return form;
    }

    public void setCopiesCount(int copiesCount) {
        this.copiesCount = copiesCount;
    }

    public int getCopiesCount() {
        return copiesCount;
    }

    public void setSheetsCount(int sheetsCount) {
        this.sheetsCount = sheetsCount;
    }

    public int getSheetsCount() {
        return sheetsCount;
    }

    public void setAppendixiesCount(int appendixiesCount) {
        this.appendixiesCount = appendixiesCount;
    }

    public int getAppendixiesCount() {
        return appendixiesCount;
    }

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.InternalDocument;
    }

    @Transient
    public DocumentStatus getDocumentStatus() {
        return DocumentType.getStatus(getDocumentType().getName(), this.statusId);
    }

    @Transient
    public void setDocumentStatus(DocumentStatus status) {
        this.statusId = status.getId();
    }

    @Override
    public String getBeanName() {
        return "internal_doc";
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Transient
    public String getUniqueId() {
        return getId() == 0 ? "" : "internal_" + getId();
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
    }

    public Set<HistoryEntry> getHistory() {
        return history;
    }

    @Transient
    public List<HistoryEntry> getHistoryList() {
        List<HistoryEntry> result = new ArrayList<HistoryEntry>();
        if (history != null) {
            result.addAll(history);
        }
        Collections.sort(result);
        return result;
    }

    public List<User> getPersonReaders() {
        return personReaders;
    }

    public void setPersonReaders(List<User> personReaders) {
        this.personReaders = personReaders;
    }

    public List<Role> getRoleReaders() {
        return roleReaders;
    }

    public void setRoleReaders(List<Role> roleReaders) {
        this.roleReaders = roleReaders;
    }

    public void setRoleEditors(List<Role> roleEditors) {
        this.roleEditors = roleEditors;
    }

    public List<Role> getRoleEditors() {
        return roleEditors;
    }

    public void setPersonEditors(List<User> personEditors) {
        this.personEditors = personEditors;
    }

    public List<User> getPersonEditors() {
        return personEditors;
    }

    public void setAgreementUsers(Set<User> agreementUsers) {
        this.agreementUsers = agreementUsers;
    }

    public Set<User> getAgreementUsers() {
        return agreementUsers;
    }

    @Override
    public void setAgreementTree(HumanTaskTree agreementTree) {
        this.agreementTree = agreementTree;
    }

    @Override
    public HumanTaskTree getAgreementTree() {
        return agreementTree;
    }

    public void setUserAccessLevel(UserAccessLevel userAccessLevel) {
        this.userAccessLevel = userAccessLevel;
    }

    public UserAccessLevel getUserAccessLevel() {
        return userAccessLevel;
    }

    public void setRecipientGroups(Set<Group> recipientGroups) {
        this.recipientGroups = recipientGroups;
    }

    public Set<Group> getRecipientGroups() {
        return recipientGroups;
    }

    @Transient
    public List<Group> getRecipientGroupsList() {
        List<Group> in_result = new ArrayList<Group>();
        if (recipientGroups != null) {
            in_result.addAll(recipientGroups);
        }
        return in_result;
    }


    //TODO сделать класс-обертку
    /**
     * Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     */
    @Transient
    private String styleClass;

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}