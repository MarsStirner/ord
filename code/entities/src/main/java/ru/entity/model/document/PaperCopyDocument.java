package ru.entity.model.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;
import ru.external.ProcessedData;
import ru.util.ApplicationHelper;


/**
 * Входящий документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_paper_copy_documents")
public class PaperCopyDocument extends IdentifiedEntity implements ProcessedData {
    private static final long serialVersionUID = -5522881582616193416L;

    /**
     * Дата регистрации
     */
    private Date registrationDate;

    /**
     * Автор документа
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_paper_copy_documents_authors")
    private User author;

    /**
     * Краткое описание
     */
    @Column(columnDefinition = "text")
    private String shortDescription;

    /**
     * Архив для передачи
     */
    @Column(columnDefinition = "text")
    private String archiveTo;

    /**
     * Дата создания документа
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

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
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    @Transient
    private int grouping = 100;

    @Transient
    private String WFResultDescription;

    /**
     * Том дела
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_paper_copy_documents_by_office_keeping_volume_id")
    private OfficeKeepingVolume officeKeepingVolume;

    /**
     * Кому передан на руки в текущий момент
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_paper_copy_document_collectors")
    private User collector;


    /**
     * Предполагаемая дата возврата
     */
    private Date returnDate;

    /**
     * Удален ли документ
     */
    private boolean deleted;

    /**
     * История
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_paper_copy_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<HistoryEntry> history;

    /**
     * Ссылка на документ основание
     */
    //@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    //@JoinColumn(name="parentDocumentId", nullable=false)
    private String parentDocumentId;

    /**
     * Документ основание
     */
    @Transient
    private IncomingDocument inParentDocument = null;
    @Transient
    private OutgoingDocument outParentDocument = null;
    @Transient
    private InternalDocument internalParentDocument = null;
    @Transient
    private RequestDocument requestParentDocument = null;

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.PaperCopyDocument;
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
        return "paper_copy_document";
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

    @Transient
    public String getUniqueId() {
        return getId() == 0 ? "" : "paper_copy_" + getId();
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

    @Transient
    private String date;

    /**
     * Материал
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "material_id")
    private DocumentForm material;

    /**
     * Системный номер
     */
    private String registrationNumber;

    /**
     * Номер почтового отправления
     */
    private String messageNumber;

    /**
     * Контактное лицо
     */
    private String contact;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public DocumentForm getMaterial() {
        return material;
    }

    public void setMaterial(DocumentForm material) {
        this.material = material;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getMessageNumber() {
        return messageNumber;
    }

    public void setMessageNumber(String messageNumber) {
        this.messageNumber = messageNumber;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
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

    public OfficeKeepingVolume getOfficeKeepingVolume() {
        return officeKeepingVolume;
    }

    public void setOfficeKeepingVolume(OfficeKeepingVolume officeKeepingVolume) {
        this.officeKeepingVolume = officeKeepingVolume;
    }

    public User getCollector() {
        return collector;
    }

    public void setCollector(User collector) {
        this.collector = collector;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public void setParentDocumentId(String parentDocumentId) {
        this.parentDocumentId = parentDocumentId;
    }

    public String getParentDocumentId() {
        return parentDocumentId;
    }

    public void setParentDocument(IncomingDocument parentDocument) {
        this.inParentDocument = parentDocument;
    }

    public void setParentDocument(OutgoingDocument parentDocument) {
        this.outParentDocument = parentDocument;
    }

    public void setParentDocument(InternalDocument parentDocument) {
        this.internalParentDocument = parentDocument;
    }

    public void setParentDocument(RequestDocument parentDocument) {
        this.requestParentDocument = parentDocument;
    }

    public Object getParentDocument() {
        if (inParentDocument != null) return inParentDocument;
        if (outParentDocument != null) return outParentDocument;
        if (internalParentDocument != null) return internalParentDocument;
        if (requestParentDocument != null) return requestParentDocument;
        return null;
    }

    public void setCopiesCount(int copiesCount) {
        this.copiesCount = copiesCount;
    }

    public int getCopiesCount() {
        return copiesCount;
    }

    public void setArchiveTo(String archiveTo) {
        this.archiveTo = archiveTo;
    }

    public String getArchiveTo() {
        return archiveTo;
    }
}