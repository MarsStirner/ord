package ru.entity.model.document;

import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.user.User;
import ru.entity.model.workflow.HistoryEntry;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Тома дел
 */
@Entity
@Table(name = "office_keeping_volume")
@Access(AccessType.FIELD)
public class OfficeKeepingVolume extends DeletableEntity {
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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "collector_id")
    private User collector;
    /**
     * Предполагаемая дата возврата
     */
    private LocalDate returnDate;
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
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;
    /**
     * Номенклатура дел
     */

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "parentFileId", nullable = false)
    private OfficeKeepingFile parentFile;
    /**
     * История
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "office_keeping_volume_history",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "entry_id")})
    private Set<HistoryEntry> history;



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

    public String getVolumeIndex() {
        return volumeIndex;
    }

    public void setVolumeIndex(String volumeIndex) {
        this.volumeIndex = volumeIndex;
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

    public OfficeKeepingFile getParentFile() {
        return parentFile;
    }

    public void setParentFile(OfficeKeepingFile parentFile) {
        this.parentFile = parentFile;
    }

    public Set<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
    }

    public List<HistoryEntry> getHistoryList() {
        List<HistoryEntry> result = new ArrayList<>();
        if (history != null) {
            result.addAll(history);
        }
        Collections.sort(result);
        return result;
    }

    public User getCollector() {
        return collector;
    }

    public void setCollector(User collector) {
        this.collector = collector;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public int getLimitUnitsCount() {
        return limitUnitsCount;
    }

    public void setLimitUnitsCount(int limitUnitsCount) {
        this.limitUnitsCount = limitUnitsCount;
    }

}