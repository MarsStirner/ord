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
 * Нумератор
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_numerators")
public class Numerator extends IdentifiedEntity implements ProcessedData {
    private static final long serialVersionUID = -5522881582616193416L;

    /**
     * Тип документа (Входящий|Исходящий|Внутренний|Обращение граждан)
     */
    @JoinColumn(name = "documentTypeKey")
    private String documentTypeKey;

    /**
     * Автор документа
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_numerators_authors")
    private User author;

    /**
     * Дата создания документа
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Индекс нумератора
     */
    private int numeratorIndex;

    /**
     * Формат номера
     */
    @Column(columnDefinition = "text")
    private String numberFormat;

    /**
     * Краткое описание
     */
    @Column(columnDefinition = "text")
    private String shortDescription;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    /**
     * Том дела
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_numerators_by_office_keeping_volume_id")
    private OfficeKeepingVolume officeKeepingVolume;

    /**
     * История
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_numerators_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<HistoryEntry> history;


    /**
     * Удален ли документ
     */
    private boolean deleted;

    @Transient
    private String WFResultDescription;

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.Numerator;
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
        return "numerator";
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
    private String date;

    @Transient
    public String getUniqueId() {
        return getId() == 0 ? "" : "numerator_" + getId();
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


    public void setOfficeKeepingVolume(OfficeKeepingVolume officeKeepingVolume) {
        this.officeKeepingVolume = officeKeepingVolume;
    }

    public OfficeKeepingVolume getOfficeKeepingVolume() {
        return officeKeepingVolume;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getAuthor() {
        return author;
    }

    public void setDocumentTypeKey(String documentTypeKey) {
        this.documentTypeKey = documentTypeKey;
    }

    public String getDocumentTypeKey() {
        return documentTypeKey;
    }

    public void setNumeratorIndex(int numeratorIndex) {
        this.numeratorIndex = numeratorIndex;
    }

    public int getNumeratorIndex() {
        return numeratorIndex;
    }

    public void setNumberFormat(String numberFormat) {
        this.numberFormat = numberFormat;
    }

    public String getNumberFormat() {
        return numberFormat;
    }
}