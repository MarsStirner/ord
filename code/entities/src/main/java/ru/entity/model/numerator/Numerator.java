package ru.entity.model.numerator;

import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Нумератор
 *
 * @author Alexey Vagizov, Egor Upatov
 */
@Entity
@Table(name = "numerator")
public class Numerator extends DeletableEntity {
    /**
     * Краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text")
    protected String shortDescription;
    /**
     * Префикс номера нумератора (ранее номенклатура)
     **/
    @Column(name = "prefix")
    private String prefix;
    /**
     * Стартовый номер нумератора (откуда начинается нумерация)
     **/
    @Column(name = "startNumber")
    private Integer startNumber;
    /**
     * Дата начала действия нумератора
     */
    @Column(name = "begDate", nullable = false)
    private LocalDate begDate;
    /**
     * Дата окончания действия нумератора
     */
    @Column(name = "endDate")
    private LocalDate endDate;
    /**
     * Приоритет нумератора над остальными
     **/
    @Column(name = "priority")
    private Integer priority;

    /* К чему привязываются нумераторы */
    /**
     * Тип документа, NULL - любой
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentType_id", nullable = true)
    private DocumentType documentType;

    /**
     * Вид документа, NULL - любой
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentForm_id", nullable = true)
    private DocumentForm documentForm;

    /**
     * Руководитель, NULL - любой
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id", nullable = true)
    private User controller;

    /**
     * Контрагент, NULL - любой
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contragent_id", nullable = true)
    private Contragent contragent;

    public Numerator() {
        priority = 1000;
        startNumber = 0;
        begDate = LocalDate.now();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(Integer startNumber) {
        this.startNumber = startNumber;
    }

    public LocalDate getBegDate() {
        return begDate;
    }

    public void setBegDate(LocalDate begDate) {
        this.begDate = begDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public DocumentForm getDocumentForm() {
        return documentForm;
    }

    public void setDocumentForm(DocumentForm documentForm) {
        this.documentForm = documentForm;
    }

    public User getController() {
        return controller;
    }

    public void setController(User controller) {
        this.controller = controller;
    }

    public Contragent getContragent() {
        return contragent;
    }

    public void setContragent(Contragent contragent) {
        this.contragent = contragent;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}