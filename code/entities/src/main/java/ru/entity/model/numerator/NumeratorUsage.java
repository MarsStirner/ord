package ru.entity.model.numerator;

import ru.entity.model.mapped.DocumentEntity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Таблица связка документов и используемых нумераторов с указанием номера
 *
 * @autor Egor Upatov
 */
@Entity(name = "NumeratorUsage")
@Table(name = "numerator_usage")
public class NumeratorUsage {
    /**
     * Уникальный ключ документа
     *
     * @see DocumentEntity#getUniqueId()
     */
    @Id
    @Column(name = "document_id", nullable = false)
    protected String documentId;


    /**
     * Ссылка на использованный нумератор
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numerator_id", nullable = false)
    private Numerator numerator;

    /**
     * Используемый номер в рамках нумератора
     **/
    @Column(name = "number", nullable = false)
    private Integer number;

    /**
     * Дата начала действия нумератора
     */
    @Column(name = "registration_date", nullable = false)
    private LocalDateTime registrationDate;

    public NumeratorUsage() {
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Numerator getNumerator() {
        return numerator;
    }

    public void setNumerator(Numerator numerator) {
        this.numerator = numerator;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "NumeratorUsage[" + documentId + "]{ numerator=" + numerator.getId() + ", number=" + number + ", registrationDate=" + registrationDate + '}';
    }
}