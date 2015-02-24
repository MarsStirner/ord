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

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    ;

    public Date getCreationDate() {
        return creationDate;
    }

    ;


    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setPlannedDate(Date plannedDate) {
        this.plannedDate = plannedDate;
    }

    public Date getPlannedDate() {
        return plannedDate;
    }

    /**
     * Автор документа
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "record_book_author")
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
     * Дата создания документа
     */
    //@Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Планируемая дата
     */
    private Date plannedDate;

    /**
     * Удален ли документ
     */
    private boolean deleted;

    private static final long serialVersionUID = -5522881582616193416L;


}