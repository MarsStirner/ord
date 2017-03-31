package ru.entity.model.referenceBook;

import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.*;

/**
 * Справочник видов документов
 */
@Entity
@Table(name = "rbDocumentForm")
public class DocumentForm extends ReferenceBookEntity {

    /**
     * Договор
     **/
    public static final String RB_CODE_INCOMING_CONTRACT = "INCOMING_CONTRACT";
    /**
     * Жалоба
     **/
    public static final String RB_CODE_INCOMING_CLAIM = "INCOMING_CLAIM";
    /**
     * Заявление
     **/
    public static final String RB_CODE_INCOMING_STATEMENT = "INCOMING_STATEMENT";
    /**
     * Обращение
     **/
    public static final String RB_CODE_INCOMING_APPEAL = "INCOMING_APPEAL";
    /**
     * Письмо
     **/
    public static final String RB_CODE_INCOMING_LETTER = "INCOMING_LETTER";
    /**
     * План
     **/
    public static final String RB_CODE_INCOMING_PLAN = "INCOMING_PLAN";
    /**
     * Постановление
     **/
    public static final String RB_CODE_INCOMING_RESOLUTION = "INCOMING_RESOLUTION";
    /**
     * Постановление коллегии МЗиСР РФ
     **/
    public static final String RB_CODE_INCOMING_RESOLUTION_MZISR_RF = "INCOMING_RESOLUTION_MZISR_RF";
    /**
     * Предписание
     **/
    public static final String RB_CODE_INCOMING_PRESCRIPTION = "INCOMING_PRESCRIPTION";
    /**
     * Претензия
     **/
    public static final String RB_CODE_INCOMING_PRETENSION = "INCOMING_PRETENSION";
    /**
     * Приказ
     **/
    public static final String RB_CODE_INCOMING_ORDER = "INCOMING_ORDER";
    /**
     * Протокол
     **/
    public static final String RB_CODE_INCOMING_PROTOCOL = "INCOMING_PROTOCOL";
    /**
     * Уведомление
     **/
    public static final String RB_CODE_INCOMING_NOTIFICATION = "INCOMING_NOTIFICATION";
    /**
     * Распоряжение
     **/
    public static final String RB_CODE_INCOMING_DISPOSAL = "INCOMING_DISPOSAL";
    /**
     * Договор
     **/
    public static final String RB_CODE_OUTGOING_CONTRACT = "OUTGOING_CONTRACT";
    /**
     * Жалоба
     **/
    public static final String RB_CODE_OUTGOING_CLAIM = "OUTGOING_CLAIM";
    /**
     * Заявление
     **/
    public static final String RB_CODE_OUTGOING_STATEMENT = "OUTGOING_STATEMENT";
    /**
     * Обращение
     **/
    public static final String RB_CODE_OUTGOING_APPEAL = "OUTGOING_APPEAL";
    /**
     * Письмо
     **/
    public static final String RB_CODE_OUTGOING_LETTER = "OUTGOING_LETTER";
    /**
     * План
     **/
    public static final String RB_CODE_OUTGOING_PLAN = "OUTGOING_PLAN";
    /**
     * Постановление
     **/
    public static final String RB_CODE_OUTGOING_RESOLUTION = "OUTGOING_RESOLUTION";
    /**
     * Предписание
     **/
    public static final String RB_CODE_OUTGOING_PRESCRIPTION = "OUTGOING_PRESCRIPTION";
    /**
     * Претензия
     **/
    public static final String RB_CODE_OUTGOING_PRETENSION = "OUTGOING_PRETENSION";
    /**
     * Приказ
     **/
    public static final String RB_CODE_OUTGOING_ORDER = "OUTGOING_ORDER";
    /**
     * Приказ
     **/
    public static final String RB_CODE_INTERNAL_ORDER = "INTERNAL_ORDER";
    /**
     * Жалоба
     **/
    public static final String RB_CODE_REQUEST_CLAIM = "REQUEST_CLAIM";
    /**
     * Благодарность
     **/
    public static final String RB_CODE_REQUEST_GRATITUDE = "REQUEST_GRATITUDE";
    /**
     * Заявка на лечение
     **/
    public static final String RB_CODE_REQUEST_TREATMENT_CLAIM = "REQUEST_TREATMENT_CLAIM";
    /**
     * Распоряжение
     **/
    public static final String RB_CODE_INTERNAL_DISPOSAL = "INTERNAL_DISPOSAL";
    /**
     * Методическое пособие
     **/
    public static final String RB_CODE_INTERNAL_MANUAL = "INTERNAL_MANUAL";
    /**
     * Инструкция
     **/
    public static final String RB_CODE_INTERNAL_INSTRUCTION = "INTERNAL_INSTRUCTION";
    /**
     * Служебная записка
     **/
    public static final String RB_CODE_INTERNAL_OFFICE_MEMO = "INTERNAL_OFFICE_MEMO";
    /**
     * Информационное письмо
     **/
    public static final String RB_CODE_INTERNAL_INFO_LETTER = "INTERNAL_INFO_LETTER";
    /**
     * Правила внутреннего распорядка
     **/
    public static final String RB_CODE_INTERNAL_INTERNAL_REGULATIONS = "INTERNAL_INTERNAL_REGULATIONS";
    /**
     * Положение
     **/
    public static final String RB_CODE_INTERNAL_STATE = "INTERNAL_STATE";
    /**
     * Резолюция
     **/
    public static final String RB_CODE_TASK_RESOLUTION = "TASK_RESOLUTION";
    /**
     * Поручение
     **/
    public static final String RB_CODE_TASK_ASSIGMENT = "TASK_ASSIGMENT";
    /**
     * Задача
     **/
    public static final String RB_CODE_TASK_TASK = "TASK_TASK";
    /**
     * Заявление
     **/
    public static final String RB_CODE_TASK_STATEMENT = "TASK_STATEMENT";
    /**
     * Обращение
     **/
    public static final String RB_CODE_TASK_APPEAL = "TASK_APPEAL";
    /**
     * Заявка
     **/
    public static final String RB_CODE_TASK_REQUEST = "TASK_REQUEST";
    /**
     * Гарантийное письмо
     **/
    public static final String RB_CODE_INTERNAL_GARANTEE_LETTER = "INTERNAL_GARANTEE_LETTER";
    /**
     * Запрос
     **/
    public static final String RB_CODE_REQUEST_REQUEST = "REQUEST_REQUEST";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentType_id", nullable = false)
    private DocumentType documentType;

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(final DocumentType documentType) {
        this.documentType = documentType;
    }

}