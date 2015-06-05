package ru.entity.model.document;

import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.entity.model.wf.HumanTaskTree;
import ru.external.AgreementIssue;
import ru.external.ProcessedData;

import javax.persistence.*;
import java.util.*;

/**
 * Внутренний документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_internal_documents")
public class InternalDocument extends DeletableEntity implements ProcessedData, AgreementIssue {
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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = true)
    private DocumentForm form;

    /**
     * Инциатор документа (автор)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Ответственный , следит за сроками исполнения документов и пинает, если исполнители не успевают.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_id", nullable = true)
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
     * Руководитель
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id", nullable = true)
    private User controller;


    /**
     * Уровень допуска
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userAccessLevel_id", nullable = true)
    private UserAccessLevel userAccessLevel;


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
     * Адресаты (пользователи)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_internal_documents_recipients",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientUsers_id")})
    private Set<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_internal_documents_recipient_groups",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientGroups_id")})
    private Set<Group> recipientGroups;


    /**
     * Пользователи-читатели
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_internal_documents_person_readers",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "personReaders_id")})
    private Set<User> personReaders;

    /**
     * Пользователи-редакторы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_internal_documents_person_editors",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "personEditors_id")})
    private Set<User> personEditors;


    /**
     * Пользователи-согласующие
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_internal_documents_agreementUsers",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "agreementUsers_id")})
    private Set<User> agreementUsers;

    /**
     * Роли-читатели
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_internal_documents_role_readers",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "roleReaders_id")})
    private Set<Role> roleReaders;

    /**
     * Роли-редакторы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_internal_documents_role_editors",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "roleEditors_id")})
    private Set<Role> roleEditors;

    @Transient
    private String WFResultDescription;

    /**
     * Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     * TODO сделать класс-обертку
     */
    @Transient
    private String styleClass;
    /**
     * История
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "dms_internal_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    private Set<HistoryEntry> history;
    /**
     * Дерево согласования
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "dms_incoming_document_agreement_tree",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "tree_id")})
    private HumanTaskTree agreementTree;

    public boolean isClosePeriodRegistrationFlag() {
        return closePeriodRegistrationFlag;
    }

    public void setClosePeriodRegistrationFlag(boolean closePeriodRegistrationFlag) {
        this.closePeriodRegistrationFlag = closePeriodRegistrationFlag;
    }

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

    public Set<User> getRecipientUsers() {
        return recipientUsers;
    }

    public void setRecipientUsers(Set<User> recipientUsers) {
        this.recipientUsers = recipientUsers;
    }

    public List<User> getRecipientUserList() {
        if (recipientUsers != null && !recipientUsers.isEmpty()) {
            return new ArrayList<User>(recipientUsers);
        }
        return new ArrayList<User>(0);
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

    public User getController() {
        return controller;
    }

    public void setController(User controller) {
        this.controller = controller;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public DocumentForm getForm() {
        return form;
    }

    public void setForm(DocumentForm form) {
        this.form = form;
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

    @Transient
    public String getUniqueId() {
        return getId() == null ? "" : "internal_" + getId();
    }

    public Set<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
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


    public Set<User> getPersonReaders() {
        return personReaders;
    }

    public void setPersonReaders(Set<User> personReaders) {
        this.personReaders = personReaders;
    }

    public List<User> getPersonReadersList() {
        if (personReaders != null && !personReaders.isEmpty()) {
            return new ArrayList<User>(personReaders);
        }
        return new ArrayList<User>(0);
    }

    public Set<Role> getRoleReaders() {
        return roleReaders;
    }

    public void setRoleReaders(Set<Role> roleReaders) {
        this.roleReaders = roleReaders;
    }

    public List<Role> getRoleReadersList(){
        if (roleReaders != null && !roleReaders.isEmpty()) {
            return new ArrayList<Role>(roleReaders);
        }
        return new ArrayList<Role>(0);
    }

    public Set<Role> getRoleEditors() {
        return roleEditors;
    }

    public void setRoleEditors(Set<Role> roleEditors) {
        this.roleEditors = roleEditors;
    }

    public List<Role> getRoleEditorsList() {
        if (roleEditors == null || roleEditors.isEmpty()) {
            return new ArrayList<Role>(0);
        } else {
            return new ArrayList<Role>(roleEditors);
        }
    }

    public Set<User> getPersonEditors() {
        return personEditors;
    }

    public void setPersonEditors(Set<User> personEditors) {
        this.personEditors = personEditors;
    }

    public List<User> getPersonEditorsList() {
        if (personEditors == null || personEditors.isEmpty()) {
            return new ArrayList<User>(0);
        } else {
            return new ArrayList<User>(personEditors);
        }
    }

    public Set<User> getAgreementUsers() {
        return agreementUsers;
    }

    public void setAgreementUsers(Set<User> agreementUsers) {
        this.agreementUsers = agreementUsers;
    }

    @Override
    public HumanTaskTree getAgreementTree() {
        return agreementTree;
    }

    @Override
    public void setAgreementTree(HumanTaskTree agreementTree) {
        this.agreementTree = agreementTree;
    }

    public UserAccessLevel getUserAccessLevel() {
        return userAccessLevel;
    }

    public void setUserAccessLevel(UserAccessLevel userAccessLevel) {
        this.userAccessLevel = userAccessLevel;
    }

    public Set<Group> getRecipientGroups() {
        return recipientGroups;
    }

    public void setRecipientGroups(Set<Group> recipientGroups) {
        this.recipientGroups = recipientGroups;
    }

    @Transient
    public List<Group> getRecipientGroupsList() {
        List<Group> in_result = new ArrayList<Group>();
        if (recipientGroups != null) {
            in_result.addAll(recipientGroups);
        }
        return in_result;
    }


    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}