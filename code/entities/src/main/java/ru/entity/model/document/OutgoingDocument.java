package ru.entity.model.document;

import ru.entity.model.crm.Contragent;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.Role;
import ru.entity.model.user.User;
import ru.entity.model.user.UserAccessLevel;
import ru.entity.model.wf.HumanTaskTree;
import ru.external.AgreementIssue;
import ru.external.ProcessedData;

import javax.persistence.*;
import java.util.*;


/**
 * Исходящий документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_outgoing_documents")
public class OutgoingDocument extends IdentifiedEntity implements ProcessedData, AgreementIssue {
    private static final long serialVersionUID = -3273628760848307048L;

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
     * Дата создания документа
     */
    @Column(name = "creationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Автор
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    /**
     * Удален ли документ
     */
    @Column(name = "deleted")
    private boolean deleted;

    /**
     * Дата регистрации
     */
    @Column(name = "registrationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date registrationDate;

    /**
     * Номер исходящего
     */
    @Column(name = "registrationNumber")
    private String registrationNumber;


    /**
     * Количество страниц
     */
    @Column(name = "sheetsCount")
    private int sheetsCount;

    /**
     * краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text")
    private String shortDescription;

    /**
     * Дата подписания
     */
    @Column(name = "signatureDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date signatureDate;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    /**
     * Тип доставки
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deliveryType_id", nullable = true)
    private DeliveryType deliveryType;


    /**
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = true)
    private DocumentForm form;

    /**
     * Номенклатура
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id", nullable = true)
    private Nomenclature nomenclature;

    /**
     * Ссылка на документ основание
     */
    @Column(name = "reason_document_id")
    private String reasonDocumentId;

    /**
     * Уровень допуска
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userAccessLevel_id", nullable = true)
    private UserAccessLevel userAccessLevel;

    /**
     * Номер ERP
     */
    @Column(name = "erpNumber")
    private String erpNumber;

    /**
     * Адресаты -контрагенты
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_contragents",
            joinColumns = {@JoinColumn(name = "dms_outgoing_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "recipientContragents_id")})
    private Set<Contragent> recipientContragents;

    /**
     * Исполнитель
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id", nullable = true)
    private User executor;

    /**
     * Руководитель
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id", nullable = true)
    private User controller;

    /**
     * Пользователи-читатели
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_person_readers",
            joinColumns = {@JoinColumn(name = "dms_outgoing_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "personReaders_id")})
    private Set<User> personReaders;

    /**
     * Пользователи-редакторы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_person_editors",
            joinColumns = {@JoinColumn(name = "dms_outgoing_documents_id")},
            inverseJoinColumns = {@JoinColumn(name = "personEditors_id")})
    private Set<User> personEditors;

    /**
     * Пользователи-согласующие
     * TODO выпилить или переделать нормально
     */
    @Deprecated
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_agreementUsers")
    private Set<User> agreementUsers;

    /**
     * Роли-читатели
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_role_readers",
            joinColumns = {@JoinColumn(name="dms_outgoing_documents_id")},
            inverseJoinColumns = {@JoinColumn(name="roleReaders_id")}
    )
    private Set<Role> roleReaders;

    /**
     * Роли-редакторы
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_role_editors",
            joinColumns = {@JoinColumn(name="dms_outgoing_documents_id")},
            inverseJoinColumns = {@JoinColumn(name="roleEditors_id")}
    )
    private Set<Role> roleEditors;

    @Transient
    private String WFResultDescription;


    /**
     * История
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "dms_outgoing_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    private Set<HistoryEntry> history;


    /**
     * Дерево согласования
     * TODO выпилить или переделать
     */
    @Deprecated
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "dms_outgoing_document_agreement_tree",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "tree_id"))
    private HumanTaskTree agreementTree;


    /**
     * Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     * TODO сделать класс-обертку
     */
    @Transient
    private String styleClass;

    @Transient
    public String getUniqueId() {
        return getId() == 0 ? "" : "outgoing_" + getId();
    }

    public Date getCreationDate() {
        return this.creationDate;
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

    public Nomenclature getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(Nomenclature nomenclature) {
        this.nomenclature = nomenclature;
    }

    public Date getRegistrationDate() {
        return this.registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public List<Contragent> getRecipientContragents() {
        return new ArrayList<Contragent>(recipientContragents);
    }

    public void setRecipientContragents(List<Contragent> recipientContragents) {
        this.recipientContragents = new HashSet<Contragent>(recipientContragents);
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

    public User getController() {
        return controller;
    }

    public void setController(User controller) {
        this.controller = controller;
    }

    public Date getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(Date signatureDate) {
        this.signatureDate = signatureDate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.OutgoingDocument;
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
        return "out_doc";
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getWFResultDescription() {
        return this.WFResultDescription;
    }

    public void setWFResultDescription(String WFResultDescription) {
        this.WFResultDescription = WFResultDescription;
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

    public List<User> getPersonReaders() {
        return new ArrayList<User>(personReaders);
    }

    public void setPersonReaders(List<User> personReaders) {
        this.personReaders = new HashSet<User>(personReaders);
    }

    public List<Role> getRoleReaders() {
        return new ArrayList<Role>(roleReaders);
    }

    public void setRoleReaders(List<Role> roleReaders) {
        this.roleReaders = new HashSet<Role>(roleReaders);
    }

    public List<Role> getRoleEditors() {
        return new ArrayList<Role>(roleEditors);
    }

    public void setRoleEditors(List<Role> roleEditors) {
        this.roleEditors = new HashSet<Role>(roleEditors);
    }

    public List<User> getPersonEditors() {
        return new ArrayList<User>(personEditors);
    }

    public void setPersonEditors(List<User> personEditors) {
        this.personEditors = new HashSet<User>(personEditors);
    }

    public String getReasonDocumentId() {
        return reasonDocumentId;
    }

    public void setReasonDocumentId(String reasonDocumentId) {
        this.reasonDocumentId = reasonDocumentId;
    }

    @Override
    public HumanTaskTree getAgreementTree() {
        return agreementTree;
    }

    @Override
    public void setAgreementTree(HumanTaskTree agreementTree) {
        this.agreementTree = agreementTree;
    }

    public Set<User> getAgreementUsers() {
        return agreementUsers;
    }

    public void setAgreementUsers(Set<User> agreementUsers) {
        this.agreementUsers = agreementUsers;
    }

    public UserAccessLevel getUserAccessLevel() {
        return userAccessLevel;
    }

    public void setUserAccessLevel(UserAccessLevel userAccessLevel) {
        this.userAccessLevel = userAccessLevel;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

}