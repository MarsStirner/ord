package ru.entity.model.document;

import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.Date;

/**
 * Входящий документ
 * Да ну на... Серьезно? Вот я лох думал что это записная книжка...(
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_record_book_documents")
public class RecordBookDocument extends IdentifiedEntity {
    private static final long serialVersionUID = -5522881582616193416L;
    /**
     * Дата создания документа
     */
    @Column(name = "creationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;
    /**
     * Автор документа
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    /**
     * Краткое описание
     */
    @Column(columnDefinition = "text")
    private String shortDescription;
    /**
     * Описание
     */
    @Column(columnDefinition = "text")
    private String description;
    /**
     * Планируемая дата
     */
    @Column(name = "plannedDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date plannedDate;
    /**
     * Удален ли документ
     */
    @Column(name = "deleted", nullable = false)
    private boolean deleted;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(Date plannedDate) {
        this.plannedDate = plannedDate;
    }


}