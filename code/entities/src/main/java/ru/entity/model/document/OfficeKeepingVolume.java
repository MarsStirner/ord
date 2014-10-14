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
 * Дело
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_office_keeping_volumes")
public class OfficeKeepingVolume extends IdentifiedEntity implements ProcessedData {

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.OfficeKeepingVolume;
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
        return "officeKeepingVolume";
    }


    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getKeepingPeriodReasons() {
        return keepingPeriodReasons;
    }

    public void setKeepingPeriodReasons(String keepingPeriodReasons) {
        this.keepingPeriodReasons = keepingPeriodReasons;
    }

    public int getUnitsCount() {
        return unitsCount;
    }

    public void setUnitsCount(int unitsCount) {
        this.unitsCount = unitsCount;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setVolumeIndex(String volumeIndex) {
        this.volumeIndex = volumeIndex;
    }

    public String getVolumeIndex() {
        return volumeIndex;
    }

    public String getFundNumber() {
        return fundNumber;
    }

    public void setFundNumber(String fundNumber) {
        this.fundNumber = fundNumber;
    }

    public String getStandNumber() {
        return standNumber;
    }

    public void setStandNumber(String standNumber) {
        this.standNumber = standNumber;
    }

    public String getShelfNumber() {
        return shelfNumber;
    }

    public void setShelfNumber(String shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    public String getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        this.boxNumber = boxNumber;
    }

    public void setParentFile(OfficeKeepingFile parentFile) {
        this.parentFile = parentFile;
    }

    public OfficeKeepingFile getParentFile() {
        return parentFile;
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

    public void setWFResultDescription(String WFResultDescription) {
        this.WFResultDescription = WFResultDescription;
    }

    public String getWFResultDescription() {
        return WFResultDescription;
    }

    public void setCollector(User collector) {
        this.collector = collector;
    }

    public User getCollector() {
        return collector;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setLimitUnitsCount(int limitUnitsCount) {
        this.limitUnitsCount = limitUnitsCount;
    }

    public int getLimitUnitsCount() {
        return limitUnitsCount;
    }

    /**
     * Номер фонда
     */
    private String fundNumber;

    /**
     * Номер стеллажа
     */
    private String standNumber;

    /**
     * Номер полки
     */
    private String shelfNumber;

    /**
     * Номер короба
     */
    private String boxNumber;

    /**
     * Кому передан на руки в текущий момент
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_office_keeping_volume_collectors")
    private User collector;


    /**
     * Предполагаемая дата возврата
     */
    private Date returnDate;

    /**
     * Индекс дела
     */
    @Column(columnDefinition = "text")
    private String volumeIndex;

    /**
     * Заголовок
     */
    @Column(columnDefinition = "text")
    private String shortDescription;

    /**
     * Примечания
     */
    @Column(columnDefinition = "text")
    private String comments;


    /**
     * Срок хранения и статьи
     */
    private String keepingPeriodReasons;

    /**
     * Максимальное количество единиц хранения
     */
    private int limitUnitsCount;

    /**
     * Количество единиц хранения
     */
    private int unitsCount;

    /**
     * Удален ли документ
     */
    private boolean deleted;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    /**
     * Номенклатура дел
     */

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "parentFileId", nullable = false)
    //@JoinTable(name="dms_office_keeping_volumes_by_files")
    private OfficeKeepingFile parentFile;

    /**
     * История
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_office_keeping_volume_history",
            joinColumns = {@JoinColumn(name = "file_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
    private Set<HistoryEntry> history;

    @Transient
    private String WFResultDescription;

    private static final long serialVersionUID = -638563758311092558L;

}