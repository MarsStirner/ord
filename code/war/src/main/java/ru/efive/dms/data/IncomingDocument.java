package ru.efive.dms.data;

import java.util.*;
import javax.persistence.*;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.efive.crm.data.Contragent;
import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.DocumentType;
import ru.efive.sql.entity.user.Group;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.user.UserAccessLevel;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.wf.core.ProcessedData;

/**
 * Входящий документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_incoming_documents")
public class IncomingDocument extends IdentifiedEntity implements ProcessedData {

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
     * Дата создания документа
     */
    @Column(name = "creationDate", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Автор документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Руководитель
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "controller_id")
    private User controller;


    /**
     * Удален ли документ
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    /**
     * Дата поступления
     */
    @Column(name = "deliveryDate", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deliveryDate;

    /**
     * Срок исполнения
     */
    @Column(name = "executionDate", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date executionDate;

    /**
     * Номер поступившего
     */
    @Column(name = "receivedDocumentNumber", nullable = true)
    private String receivedDocumentNumber;

    /**
     * Дата регистрации поступившего документа у корреспондента
     */
    @Column(name = "receivedDocumentDate", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date receivedDocumentDate;

    /**
     * Номер входящего
     */
    @Column(name = "registrationNumber", nullable = true)
    private String registrationNumber;

    /**
     * Дата регистрации
     */
    @Column(name = "registrationDate", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date registrationDate;

    /**
     * Количество страниц
     */
    @Column(name = "sheetsCount", nullable = false)
    private int sheetsCount;

    /**
     * Краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text", nullable = true)
    private String shortDescription;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id", nullable = false)
    private int statusId;

    /**
     * Корреспондент
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "contragent_id", nullable = true)
    private Contragent contragent;

    /**
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "form_id", nullable = false)
    private DocumentForm form;

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

    /**
     * Номер короба
     */
    @Column(name = "boxNumber")
    private int boxNumber;

    /**
     * Номер фонда
     */
    @Column(name = "fundNumber")
    private int fundNumber;

    /**
     * Номер стеллажа
     */
    @Column(name = "standNumber")
    private int standNumber;

    /**
     * Номер полки
     */
    @Column(name = "shelfNumber")
    private int shelfNumber;

    /**
     * Предполагаемая дата возврата
     */
    @Column(name = "returnDate")
    private Date returnDate;

    /**
     * Ссылка на нумератор
     */
    @Column(name = "parentNumeratorId", nullable = true)
    private String parentNumeratorId;

    /**
     * Уровень допуска
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userAccessLevel_id", nullable = false)
    private UserAccessLevel userAccessLevel;

    /**
     * Номер ERP
     */
    @Column(name = "erpNumber")
    private String erpNumber;

    /**
     * Связанные сущности ********************************************************************************************
     */


    /**
     * История
     */
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "dms_incoming_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<HistoryEntry> history;

    /**
     * Адресаты
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_incoming_documents_recipients")
    private List<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_incoming_documents_recipient_groups",
            joinColumns = {@JoinColumn(name = "dms_incoming_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientGroups_id")})
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<Group> recipientGroups;


    /**
     * Пользователи-читатели
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_incoming_documents_person_readers")
    @IndexColumn(name = "ID1")
    private List<User> personReaders;

    /**
     * Пользователи-редакторы
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_incoming_documents_person_editors")
    @IndexColumn(name = "ID1")
    private List<User> personEditors;


    /**
     * Роли-читатели
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_incoming_documents_role_readers")
    @IndexColumn(name = "ID2")
    private List<Role> roleReaders;

    /**
     * Роли-редакторы
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_incoming_documents_role_editors")
    @IndexColumn(name = "ID2")
    private List<Role> roleEditors;

    /**
     * Исполнители
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinTable(name = "dms_incoming_documents_executors")
    @IndexColumn(name = "ID3")
    private List<User> executors;


    /**
     * END OF DATABASE FIELDS *****************************************************************************************
     */

    @Transient
    private int grouping = 100;

    @Transient
    private String WFResultDescription;

    @Transient
    private String date;


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

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<User> getRecipientUsers() {
        return recipientUsers;
    }

    public void setRecipientUsers(List<User> recipientUsers) {
        this.recipientUsers = recipientUsers;
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

    public Contragent getContragent() {
        return contragent;
    }

    public void setContragent(Contragent contragent) {
        this.contragent = contragent;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public User getController() {
        return controller;
    }

    public void setController(User controller) {
        this.controller = controller;
    }

    public List<User> getExecutors() {
        return executors;
    }

    public void setExecutors(List<User> executors) {
        this.executors = executors;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setNomenclature(Nomenclature nomenclature) {
        this.nomenclature = nomenclature;
    }

    public Nomenclature getNomenclature() {
        return nomenclature;
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

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setReceivedDocumentNumber(String receivedDocumentNumber) {
        this.receivedDocumentNumber = receivedDocumentNumber;
    }

    public String getReceivedDocumentNumber() {
        return receivedDocumentNumber;
    }

    public void setReceivedDocumentDate(Date receivedDocumentDate) {
        this.receivedDocumentDate = receivedDocumentDate;
    }

    public Date getReceivedDocumentDate() {
        return receivedDocumentDate;
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

    public void setFundNumber(int fundNumber) {
        this.fundNumber = fundNumber;
    }

    public int getFundNumber() {
        return fundNumber;
    }

    public void setStandNumber(int standNumber) {
        this.standNumber = standNumber;
    }

    public int getStandNumber() {
        return standNumber;
    }

    public void setShelfNumber(int shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    public int getShelfNumber() {
        return shelfNumber;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.IncomingDocument;
    }

    @Transient
    public DocumentStatus getDocumentStatus() {
        return DocumentType.getStatus(getDocumentType().getName(), statusId);
    }

    @Transient
    public void setDocumentStatus(DocumentStatus status) {
        statusId = status.getId();
    }

    @Override
    public String getBeanName() {
        return "in_doc";
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

    public String getWFResultDescription() {
        return this.WFResultDescription;
    }

    public void setWFResultDescription(String WFResultDescription) {
        this.WFResultDescription = WFResultDescription;
    }

    public void setExecutionStringDate(String date) {
        this.date = date;
    }

    public String getExecutionStringDate() {
        return this.date;
    }

    @Transient
    public String getUniqueId() {
        return getId() == 0 ? "" : "incoming_" + getId();
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

    /**
     * Добавление в историю еще одной записи, если история пуста, то она создается
     * @param historyEntry Запись в истории, которую надо добавить
     * @return статус добавления (true - успех)
     */
    public boolean addToHistory(final HistoryEntry historyEntry) {
        if (history == null) {
            this.history = new HashSet<HistoryEntry>(1);
        }
        return this.history.add(historyEntry);
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

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setUserAccessLevel(UserAccessLevel userAccessLevel) {
        this.userAccessLevel = userAccessLevel;
    }

    public UserAccessLevel getUserAccessLevel() {
        return userAccessLevel;
    }

    public void setParentNumeratorId(String parentNumeratorId) {
        this.parentNumeratorId = parentNumeratorId;
    }

    public String getParentNumeratorId() {
        return parentNumeratorId;
    }

    private static final long serialVersionUID = -5522881582616193416L;
}