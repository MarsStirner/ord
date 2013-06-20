package ru.efive.dms.data;

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

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.DocumentType;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.wf.core.ProcessedData;

/**
 * Входящий документ
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_scan_documents")
public class ScanCopyDocument extends IdentifiedEntity implements ProcessedData {
    private static final long serialVersionUID = 3511219252425549481L;
    /**
     * Автор документа
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_scan_documents_authors")
    private User author;

    /**
     * Краткое описание
     */
    @Column(columnDefinition = "text")
    private String shortDescription;

    @Transient
    private String date;

    /**
     * Дата создания документа
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

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
     * Удален ли документ
     */
    private boolean deleted;

    /**
     * История
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_scan_document_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<HistoryEntry> history;

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
        return DocumentType.ScanCopyDocument.getName();
    }

    @Override
    public String getBeanName() {
        return "scan_doc";
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
        return getId() == 0 ? "" : "scan_" + getId();
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
}