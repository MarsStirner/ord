package ru.entity.model.document;

import ru.entity.model.mapped.IdentifiedEntity;

import javax.persistence.*;


/**
 * Дело
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_office_keeping_records")
public class OfficeKeepingRecord extends IdentifiedEntity {

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

    public int getKeepingPeriod() {
        return keepingPeriod;
    }

    public void setKeepingPeriod(int keepingPeriod) {
        this.keepingPeriod = keepingPeriod;
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

    public void setRecordIndex(String recordIndex) {
        this.recordIndex = recordIndex;
    }

    public String getRecordIndex() {
        return recordIndex;
    }

    /**
     * Индекс дела
     */
    @Column(columnDefinition = "text")
    private String recordIndex;

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
     * Срок хранения
     */
    private int keepingPeriod;

    /**
     * Количество единиц хранения
     */
    private int unitsCount;

    /**
     * Удален ли документ
     */
    private boolean deleted;

    private static final long serialVersionUID = -638563758311092558L;

}