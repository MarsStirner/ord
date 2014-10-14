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
import ru.util.ApplicationHelper;

import javax.persistence.CascadeType;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

/**
 * Входящий документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_internal_documents")
public class InternalDocument extends IdentifiedEntity implements ProcessedData, AgreementIssue {
    private static final long serialVersionUID = -7971345050896379926L;

    /**
     * Номер входящего
     */
    private String registrationNumber;

    /**
     * Номер ERP
     */

    private String erpNumber;

    /**
     * Дата поступления
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deliveryDate;

    /**
     * Инциатор документа (автор)
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "initiator_id")
    private User initiator;

    /**
     * Дата регистрации
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date registrationDate;

    /**
     * Срок исполнения
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date factDate;
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date executionDate;

    /**
     * Дата подписания
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date signatureDate;

    /**
     * Контролер
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "controller_id")
    private User controller;


    /**
     * Подписант
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name = "signer_id")
    private User signer;

    /**
     * Адресаты (пользователи)
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_recipients")
    private List<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_internal_documents_recipient_groups",
            joinColumns = {@JoinColumn(name = "dms_internal_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientGroups_id")})
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<Group> recipientGroups;


    /**
     * Ответственный
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "responsible_id")
    private User responsible;

    /**
     * Пользователи-читатели
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_person_readers")
    @IndexColumn(name = "ID1")
    private List<User> personReaders;

    /**
     * Пользователи-редакторы
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_person_editors")
    @IndexColumn(name = "ID1")
    private List<User> personEditors;


    /**
     * Пользователи-согласующие
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_agreementUsers")
    private Set<User> agreementUsers;

    /**
     * Роли-читатели
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_role_readers")
    @IndexColumn(name = "ID2")
    private List<Role> roleReaders;

    /**
     * Роли-редакторы
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_internal_documents_role_editors")
    @IndexColumn(name = "ID2")
    private List<Role> roleEditors;

    /**
     * Краткое описание
     */
    @Column(columnDefinition = "text")
    private String shortDescription;

    /**
     * Дата создания документа
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Вид документа
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "form_id")
    private DocumentForm form;

    /**
     * Количество экземпляров
     */
    private int copiesCount;

    /**
     * Количество страниц
     */
    private int sheetsCount;

    /**
     * Количество приложений
     */
    private int appendixiesCount;

    /**
     * Номер фонда
     */
    private int fundNumber;

    /**
     * Номер стеллажа
     */
    private int standNumber;

    /**
     * Номер полки
     */
    private int shelfNumber;

    /**
     * Номер короба
     */
    private int boxNumber;


    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    @Transient
    private int grouping = 100;

    @Transient
    private String WFResultDescription;

    /**
     * Уровень допуска
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private UserAccessLevel userAccessLevel;

    /**
     * Удален ли документ
     */
    private boolean deleted;

    /**
     * Является ли документ шаблоном
     */
    private boolean templateFlag;

    public boolean isClosePeriodRegistrationFlag() {
        return closePeriodRegistrationFlag;
    }

    public void setClosePeriodRegistrationFlag(boolean closePeriodRegistrationFlag) {
        this.closePeriodRegistrationFlag = closePeriodRegistrationFlag;
    }

    /**
     * Регистрация изменений закрытого периода
     */
    private boolean closePeriodRegistrationFlag = false;

    /**
     * История
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
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

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
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

    public User getController() {
        return controller;
    }

    public void setController(User controller) {
        this.controller = controller;
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

    ;

    public Date getCreationDate() {
        return creationDate;
    }

    ;

    public void setForm(DocumentForm form) {
        this.form = form;
    }

    ;

    public DocumentForm getForm() {
        return form;
    }

    ;

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

    public int getGrouping() {
        return grouping;
    }

    public void setGrouping(int grouping) {
        this.grouping = grouping;
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
        Collections.sort(result, new Comparator<HistoryEntry>() {
            public int compare(HistoryEntry o1, HistoryEntry o2) {
                Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                c1.setTime(o1.getCreated());
                Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                c2.setTime(o2.getCreated());
                return c1.compareTo(c2);
            }
        });
        return result;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public void setShelfNumber(int shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    public int getShelfNumber() {
        return shelfNumber;
    }

    public void setStandNumber(int standNumber) {
        this.standNumber = standNumber;
    }

    public int getStandNumber() {
        return standNumber;
    }

    public void setFundNumber(int fundNumber) {
        this.fundNumber = fundNumber;
    }

    public int getFundNumber() {
        return fundNumber;
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

    public void setTemplateFlag(boolean templateFlag) {
        this.templateFlag = templateFlag;
    }

    public boolean getTemplateFlag() {
        return templateFlag;
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
}