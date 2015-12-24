package ru.entity.model.document;

import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.DeletableEntity;
import ru.external.ProcessedData;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Дело
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_office_keeping_files")
public class OfficeKeepingFile extends DeletableEntity implements ProcessedData {

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.OfficeKeepingFile;
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
        return "officeKeepingFile";
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

    public void setFileIndex(String fileIndex) {
        this.fileIndex = fileIndex;
    }

    public String getFileIndex() {
        return fileIndex;
    }

    public void setVolumes(Set<OfficeKeepingVolume> volumes) {
        this.volumes = volumes;
    }

    public Set<OfficeKeepingVolume> getVolumes() {
        return volumes;
    }

    @Transient
    public List<OfficeKeepingVolume> getVolumesList() {
        List<OfficeKeepingVolume> result = new ArrayList<>();
        if (volumes != null) {
            result.addAll(volumes);
        }
        return result;
    }

    @Transient
    public List<OfficeKeepingVolume> getRegistratedVolumesList() {
        List<OfficeKeepingVolume> result = new ArrayList<>();
        if (volumes != null && !volumes.isEmpty()) {
            for (OfficeKeepingVolume volume : volumes) {
                if (volume.getDocumentStatus().getId() >= 2) {
                    result.add(volume);
                }
            }
        }
        return result;
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
    }

    public Set<HistoryEntry> getHistory() {
        return history;
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

    public void setWFResultDescription(String wFResultDescription) {
        WFResultDescription = wFResultDescription;
    }

    public String getWFResultDescription() {
        return WFResultDescription;
    }

    /**
     * Индекс дела
     */
    @Column(columnDefinition = "text")
    private String fileIndex;

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
     * Количество единиц хранения
     */
    private int unitsCount;

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
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    /**
     * Номенклатура дел
     */
    //@OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    //@Cascade({ org.hibernate.annotations.CascadeType.ALL })
    //@JoinTable(name="dms_office_keeping_volumes_by_files")
    //@LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "parentFile")
    private Set<OfficeKeepingVolume> volumes;

    /**
     * История
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "dms_office_keeping_file_history",
            joinColumns = {@JoinColumn(name = "file_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    private Set<HistoryEntry> history;

    @Transient
    private String WFResultDescription;

    private static final long serialVersionUID = -638563758311092558L;

}