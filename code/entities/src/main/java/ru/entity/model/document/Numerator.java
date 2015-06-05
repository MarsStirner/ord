package ru.entity.model.document;

import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.referenceBook.Nomenclature;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.Date;

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
    @Column(name="creationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;
    /**
     * Автор документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

     /**
     * Тип документа (Входящий|Исходящий|Внутренний|Обращение граждан)
      * */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "documentType_id")
    private DocumentType documentType;


    /**
     * Номенклатура документа (NULL - wildcard)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="nomenclature_id", nullable = true)
    private Nomenclature nomenclature;

    /**
     * Текущее значение счетчика
     */
    @Column(name="value")
    private Integer value;

    /**
     * Краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text")
    private String shortDescription;

    /**
     * Дата начала действия нумератора
     */
    @Column(name="startDate")
    @Temporal(value = TemporalType.DATE)
    private Date startDate;

    /**
     * Дата окончания действия нумератора
     */
    @Column(name="endDate")
    @Temporal(value = TemporalType.DATE)
    private Date endDate;

    public Numerator() {
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }
}