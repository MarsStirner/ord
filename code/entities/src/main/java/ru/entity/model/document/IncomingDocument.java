package ru.entity.model.document;

import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.*;
import ru.entity.model.user.Group;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.external.ProcessedData;

import javax.persistence.*;
import java.util.*;


/**
 * Входящий документ
 * По умолчанию все связи - LAZY (используются разные уровни критериев в DAO)
 *
 * @author Alexey Vagizov / Egor Upatov
 */
@Entity
@Table(name = "dms_incoming_documents")
public class IncomingDocument extends DeletableEntity implements ProcessedData {

    private static final long serialVersionUID = -5522881582616193416L;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id")
    private User controller;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contragent_id", nullable = true)
    private Contragent contragent;

    /**
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
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
     * Ссылка на нумератор
     */
    @Column(name = "parentNumeratorId", nullable = true)
    private String parentNumeratorId;

    /**
     * Уровень допуска
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userAccessLevel_id", nullable = false)
    private UserAccessLevel userAccessLevel;

    /**
     * Номер ERP
     */
    @Column(name = "erpNumber")
    private String erpNumber;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Связанные сущности
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * История
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    private Set<HistoryEntry> history;
    /**
     * Адресаты
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_documents_recipients",
            joinColumns = {@JoinColumn(name = "dms_incoming_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientUsers_id")})
    private Set<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_documents_recipient_groups",
            joinColumns = {@JoinColumn(name = "dms_incoming_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientGroups_id")})
    private Set<Group> recipientGroups;

    /**
     * Пользователи-читатели
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_documents_person_readers",
            joinColumns = {@JoinColumn(name = "dms_incoming_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "personReaders_id")})
    private Set<User> personReaders;

    /**
     * Пользователи-редакторы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_documents_person_editors",
            joinColumns = {@JoinColumn(name = "dms_incoming_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "personEditors_id")})
    private Set<User> personEditors;

    /**
     * Роли-читатели
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_documents_role_readers",
            joinColumns = {@JoinColumn(name = "dms_incoming_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "roleReaders_id")})
    private Set<Role> roleReaders;

    /**
     * Роли-редакторы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_documents_role_editors",
            joinColumns = {@JoinColumn(name = "dms_incoming_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "roleEditors_id")})
    private Set<Role> roleEditors;

    /**
     * Исполнители
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_documents_executors",
            joinColumns = {@JoinColumn(name = "dms_incoming_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "executors_id")})
    private Set<User> executors;
    /**
     * END OF DATABASE FIELDS *****************************************************************************************
     */

    @Transient
    private String WFResultDescription;


    /**
     * Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     * TODO сделать класс-обертку
     */
    @Transient
    private String styleClass;

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

    public Set<User> getExecutors() {
        return executors;
    }

    public void setExecutors(Set<User> executors) {
        this.executors = executors;
    }

    public List<User> getExecutorsList() {
        if (executors != null && !executors.isEmpty()) {
            return new ArrayList<User>(executors);
        }
        return new ArrayList<User>(0);
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Nomenclature getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(Nomenclature nomenclature) {
        this.nomenclature = nomenclature;
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

    public Date getReceivedDocumentDate() {
        return receivedDocumentDate;
    }

    public void setReceivedDocumentDate(Date receivedDocumentDate) {
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


    public String getWFResultDescription() {
        return this.WFResultDescription;
    }

    public void setWFResultDescription(String WFResultDescription) {
        this.WFResultDescription = WFResultDescription;
    }

    @Transient
    public String getUniqueId() {
        return getId() == null ? "" : "incoming_" + getId();
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

    /**
     * Добавление в историю еще одной записи, если история пуста, то она создается
     *
     * @param historyEntry Запись в истории, которую надо добавить
     * @return статус добавления (true - успех)
     */
    public boolean addToHistory(final HistoryEntry historyEntry) {
        if (history == null) {
            this.history = new HashSet<HistoryEntry>(1);
        }
        return this.history.add(historyEntry);
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
            return new ArrayList<Group>(0);
        } else {
            return new ArrayList<Group>(recipientGroups);
        }
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

    public UserAccessLevel getUserAccessLevel() {
        return userAccessLevel;
    }

    public void setUserAccessLevel(UserAccessLevel userAccessLevel) {
        this.userAccessLevel = userAccessLevel;
    }

    public String getParentNumeratorId() {
        return parentNumeratorId;
    }

    public void setParentNumeratorId(String parentNumeratorId) {
        this.parentNumeratorId = parentNumeratorId;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }



}