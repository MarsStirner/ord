package ru.entity.model.document;

import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;


/**
 * Входящий документ
 * Да ну на... Серьезно? Вот я лох думал что это записная книжка...(
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_record_book_documents")
public class RecordBookDocument extends DeletableEntity {
    /**
     * Дата создания документа
     */
    @Column(name = "creationDate")
    private LocalDateTime creationDate;
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
    private LocalDateTime plannedDate;

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

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(LocalDateTime plannedDate) {
        this.plannedDate = plannedDate;
    }
}