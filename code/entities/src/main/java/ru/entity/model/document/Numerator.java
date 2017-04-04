package ru.entity.model.document;

import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.referenceBook.Nomenclature;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Нумератор
 *
 * @author Alexey Vagizov, Egor Upatov
 */
@Entity
@Table(name = "rbNumerators")
public class Numerator extends DeletableEntity {
    /**
     * Дата создания документа
     */
    @Column(name = "creationDate")
    private LocalDateTime creationDate;
    /**
     * Автор документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    /**
     * Тип документа (Входящий|Исходящий|Внутренний|Обращение граждан)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "documentType_id")
    private DocumentType documentType;


    /**
     * Номенклатура документа (NULL - wildcard)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nomenclature_id", nullable = true)
    private Nomenclature nomenclature;

    /**
     * Текущее значение счетчика
     */
    @Column(name = "value")
    private Integer value;

    /**
     * Краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text")
    private String shortDescription;

    /**
     * Дата начала действия нумератора
     */
    @Column(name = "startDate")
    private LocalDate startDate;

    /**
     * Дата окончания действия нумератора
     */
    @Column(name = "endDate")
    private LocalDate endDate;

    public Numerator() {
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Nomenclature getNomenclature() {
        return nomenclature;
    }

    public void setNomenclature(Nomenclature nomenclature) {
        this.nomenclature = nomenclature;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}