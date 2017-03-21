package ru.entity.model.document;

import org.apache.commons.lang3.StringUtils;
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
 * Обращения граждан
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_request_documents")
public class RequestDocument extends DeletableEntity implements ProcessedData {
    private static final long serialVersionUID = -5522881582616193416L;

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
     * Дата поступления
     */
    @Column(name = "deliveryDate", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date deliveryDate;

    /**
     * Автор документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Срок исполнения
     */
    @Column(name = "executionDate", nullable = true)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date executionDate;

    /**
     * Руководитель
     * TODO решить до конца вопрос с руководителем в обращениях граждан
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_request_documents_controllers")
    private User controller;

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
    @JoinTable(name = "dms_request_documents_recipients",
            joinColumns = {@JoinColumn(name = "dms_request_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientUsers_id")})
    private Set<User> recipientUsers;

    /**
     * Адресаты (группы)
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_request_documents_recipient_groups",
            joinColumns = {@JoinColumn(name = "dms_request_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientGroups_id")})
    private Set<Group> recipientGroups;

    /**
     * Пользователи-читатели
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_request_documents_person_readers",
            joinColumns = {@JoinColumn(name = "dms_request_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "personReaders_id")})
    private Set<User> personReaders;

    /**
     * Пользователи-редакторы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_request_documents_person_editors",
            joinColumns = {@JoinColumn(name = "dms_request_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "personEditors_id")})
    private Set<User> personEditors;

    /**
     * Роли-читатели
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_request_documents_role_readers",
            joinColumns = {@JoinColumn(name = "dms_request_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "roleReaders_id")})
    private Set<Role> roleReaders;

    /**
     * Роли-редакторы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_request_documents_role_editors",
            joinColumns = {@JoinColumn(name = "dms_request_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "roleEditors_id")})
    private Set<Role> roleEditors;


    /**
     * Краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text")
    private String shortDescription;

    /**
     * Дата создания документа
     */
    @Column(name="creationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

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
    @JoinColumn(name = "form_id")
    private DocumentForm form;

    /**
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveryType_id")
    private DeliveryType deliveryType;

    /**
     * Номер поступившего
     */
    @Column(name="receivedDocumentNumber")
    private String receivedDocumentNumber;

    /**
     * Номер ERP
     */
    @Column(name="erpNumber")
    private String erpNumber;

    /**
     * Дата регистрации поступившего документа у корреспондента
     */
    @Column(name="receivedDocumentDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date receivedDocumentDate;

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
     * Количество страниц
     */
    @Column(name = "sheetsCount", nullable = false)
    private int sheetsCount;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    @Transient
    private String WFResultDescription;

    /**
     * Номенклатура
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id")
    private Nomenclature nomenclature;


    /**
     * Является ли документ шаблоном
     */
    private boolean templateFlag;

    /**
     * История
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "dms_request_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    private Set<HistoryEntry> history;

    /**
     * Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     * TODO класс-обертка
     */
    @Transient
    private String styleClass;

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.RequestDocument;
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
        return "request_doc";
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

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
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

    @Transient
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

    @Transient
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

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    @Transient
    public String getStatusName() {
        return DocumentType.getStatusName(getType(), getStatusId());
    }

    @Transient
    public String getType() {
        return DocumentType.RequestDocument.getName();
    }

    public String getWFResultDescription() {
        return this.WFResultDescription;
    }

    public void setWFResultDescription(String WFResultDescription) {
        this.WFResultDescription = WFResultDescription;
    }

    @Transient
    public String getUniqueId() {
        return getId() == null ? "" : "request_" + getId();
    }

    public Set<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
    }

    @Transient
    public List<HistoryEntry> getHistoryList() {
        List<HistoryEntry> result = new ArrayList<>();
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
            this.history = new HashSet<>(1);
        }
        return this.history.add(historyEntry);
    }

    public Set<Group> getRecipientGroups() {
        return recipientGroups;
    }

    public void setRecipientGroups(Set<Group> recipientGroups) {
        this.recipientGroups = recipientGroups;
    }

    @Transient
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

    public Set<User> getPersonReaders() {
        return personReaders;
    }

    public void setPersonReaders(Set<User> personReaders) {
        this.personReaders = personReaders;
    }

    public List<User> getPersonReadersList() {
        if (personReaders != null && !personReaders.isEmpty()) {
            return new ArrayList<>(personReaders);
        }
        return new ArrayList<>(0);
    }

    public Set<Role> getRoleReaders() {
        return roleReaders;
    }

    public void setRoleReaders(Set<Role> roleReaders) {
        this.roleReaders = roleReaders;
    }

    public List<Role> getRoleReadersList(){
        if (roleReaders != null && !roleReaders.isEmpty()) {
            return new ArrayList<>(roleReaders);
        }
        return new ArrayList<>(0);
    }

    public Set<Role> getRoleEditors() {
        return roleEditors;
    }

    public void setRoleEditors(Set<Role> roleEditors) {
        this.roleEditors = roleEditors;
    }

    public List<Role> getRoleEditorsList() {
        if (roleEditors == null || roleEditors.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(roleEditors);
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
            return new ArrayList<>(0);
        } else {
            return new ArrayList<>(personEditors);
        }
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

    public boolean getTemplateFlag() {
        return templateFlag;
    }

    public void setTemplateFlag(boolean templateFlag) {
        this.templateFlag = templateFlag;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}