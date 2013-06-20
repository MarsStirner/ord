package ru.efive.dms.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import ru.efive.crm.data.Contact;
import ru.efive.crm.data.Contragent;
import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.DocumentType;
import ru.efive.sql.entity.user.Role;
import ru.efive.sql.entity.user.User;
import ru.efive.sql.entity.user.UserAccessLevel;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.wf.core.AgreementIssue;
import ru.efive.wf.core.ProcessedData;
import ru.efive.wf.core.data.HumanTaskTree;

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
     * Дата создания документа
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Вид документа
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private DocumentForm form;

    /**
     * краткое описание
     */
    @Column(columnDefinition = "text")
    private String shortDescription;

    /**
     * Номер исходящего
     */
    private String registrationNumber;

    /**
     * Дата регистрации
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date registrationDate;

    /**
     * Адресаты - контакты
     */
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_outgoing_documents_contacts")
    private Set<Contact> recipientPersons;


    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_outgoing_documents_contragents")
    @IndexColumn(name = "ID")
    private List<Contragent> recipientContragents;

    /**
     * Дата отсылки
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date sendingDate;

    /**
     * Тип доставки
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    private DeliveryType deliveryType;

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
     * Исполнитель
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_executors")
    private User executor;

    /**
     * Подписант
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_signers")
    private User signer;

    /**
     * Дата подписания
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date signatureDate;


    /**
     * Автор
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "dms_outgoing_documents_authors")
    private User author;

    /**
     * Зарегистрирован ли документ
     */
    private boolean registered;

    /**
     * Номенклатура
     */

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name = "nomenclature_id")
    private Nomenclature nomenclature;

    /**
     * Входящий документ основание
     */

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    //@JoinTable(name="dms_outgoings_cause_in_doc",
    //joinColumns = @JoinColumn(name="FLIGHT_ID"),
    //inverseJoinColumns = @JoinColumn(name="COMP_ID")
    //)
    //@IndexColumn(name="INDEX_COL")
    private IncomingDocument causeIncomingDocument;

    /**
     * Пользователи-читатели
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinTable(name = "dms_outgoing_documents_person_readers")
    @IndexColumn(name = "ID1")
    private List<User> personReaders;

    /**
     * Пользователи-редакторы
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinTable(name = "dms_outgoing_documents_person_editors")
    @IndexColumn(name = "ID1")
    private List<User> personEditors;

    /**
     * Пользователи-согласующие
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinTable(name = "dms_outgoing_documents_agreementUsers")
    private Set<User> agreementUsers;

    /**
     * Роли-читатели
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinTable(name = "dms_outgoing_documents_role_readers")
    @IndexColumn(name = "ID2")
    private List<Role> roleReaders;

    /**
     * Роли-редакторы
     */
    @ManyToMany(cascade = CascadeType.REFRESH)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinTable(name = "dms_outgoing_documents_role_editors")
    @IndexColumn(name = "ID2")
    private List<Role> roleEditors;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    /**
     * Ссылка на документ основание
     */
    @Column(name = "reason_document_id")
    private String reasonDocumentId;

    /**
     * Группировка
     */
    @Transient
    private int grouping = 100;

    @Transient
    private String WFResultDescription;

    /**
     * Удален ли документ
     */
    private boolean deleted;

    /**
     * Является ли документ шаблоном
     */
    private boolean templateFlag;

    /**
     * История
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_outgoing_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    @LazyCollection(LazyCollectionOption.EXTRA)
    private Set<HistoryEntry> history;

    /**
     * Уровень допуска
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private UserAccessLevel userAccessLevel;

    /**
     * Дерево согласования
     */
    @OneToOne
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_outgoing_document_agreement_tree",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "tree_id"))
    @LazyToOne(LazyToOneOption.PROXY)
    private HumanTaskTree agreementTree;

    /**
     * Том дела
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinTable(name = "dms_incoming_documents_by_office_keeping_volume_id")
    private OfficeKeepingVolume officeKeepingVolume;

    @Transient
    public String getUniqueId() {
        return getId() == 0 ? "" : "outgoing_" + getId();


    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setForm(DocumentForm form) {
        this.form = form;
    }

    public DocumentForm getForm() {
        return form;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public IncomingDocument getCauseIncomingDocument() {
        return causeIncomingDocument;
    }

    public void setCauseIncomingDocument(IncomingDocument causeIncomingDocument) {
        this.causeIncomingDocument = causeIncomingDocument;
    }


    public void setNomenclature(Nomenclature nomenclature) {
        this.nomenclature = nomenclature;
    }

    ;

    public Nomenclature getNomenclature() {
        return nomenclature;
    }

    ;

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate() {
        return this.registrationDate;
    }

    public void setRecipientPersons(Set<Contact> recipientPersons) {
        this.recipientPersons = recipientPersons;
    }

    public Set<Contact> getRecipientPersons() {
        return recipientPersons;
    }

    public void setRecipientContragents(List<Contragent> recipientContragents) {
        this.recipientContragents = recipientContragents;
    }

    public List<Contragent> getRecipientContragents() {
        return recipientContragents;
    }

    public void setSendingDate(Date sendingDate) {
        this.sendingDate = sendingDate;
    }

    public Date getSendingDate() {
        return this.sendingDate;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
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

    public void setExecutor(User executor) {
        this.executor = executor;
    }

    public User getExecutor() {
        return executor;
    }

    public void setSigner(User signer) {
        this.signer = signer;
    }

    public User getSigner() {
        return signer;
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

    public void setRegistered(boolean isRegistered) {
        this.registered = isRegistered;
    }

    public boolean isRegistered() {
        return registered;
    }

    @Transient
    public String getTypeAlias() {
        return "Исходящий документ";
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

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getGrouping() {
        return grouping;
    }

    public void setGrouping(int grouping) {
        this.grouping = grouping;
    }

    public String getWFResultDescription() {
        return this.WFResultDescription;
    }

    public void setWFResultDescription(String WFResultDescription) {
        this.WFResultDescription = WFResultDescription;
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


    public void setReasonDocumentId(String reasonDocumentId) {
        this.reasonDocumentId = reasonDocumentId;
    }

    public String getReasonDocumentId() {
        return reasonDocumentId;
    }


    @Override
    public void setAgreementTree(HumanTaskTree agreementTree) {
        this.agreementTree = agreementTree;
    }

    @Override
    public HumanTaskTree getAgreementTree() {
        return agreementTree;
    }

    public void setOfficeKeepingVolume(OfficeKeepingVolume officeKeepingVolume) {
        this.officeKeepingVolume = officeKeepingVolume;
    }

    public OfficeKeepingVolume getOfficeKeepingVolume() {
        return officeKeepingVolume;
    }

    public void setAgreementUsers(Set<User> agreementUsers) {
        this.agreementUsers = agreementUsers;
    }

    public Set<User> getAgreementUsers() {
        return agreementUsers;
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

}