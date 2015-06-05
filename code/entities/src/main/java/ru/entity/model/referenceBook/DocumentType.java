package ru.entity.model.referenceBook;

import ru.entity.model.mapped.DictionaryEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Author: Upatov Egor <br>
 * Date: 02.06.2015, 15:19 <br>
 * Company: Korus Consulting IT <br>
 * Description: Справочник типов документов<br>
 */
@Entity
@Table(name = "rbDocumentType")
public class DocumentType extends DictionaryEntity {
    /**
     * Предопределенные коды для справочника типов документов
     */
    /**
     * Входящий
     */
    public static final String RB_CODE_INCOMING = "INCOMING";
    /**
     * Исходящий
     */
    public static final String RB_CODE_OUTGOING = "OUTGOING";
    /**
     * Внутренний
     */
    public static final String RB_CODE_INTERNAL = "INTERNAL";
    /**
     * Обращение граждан
     */
    public static final String RB_CODE_REQUEST = "REQUEST";
    /**
     * Поручение
     */
    public static final String RB_CODE_TASK = "TASK";
    /**
     * Бумажные экземпляры
     */
    public static final String RB_CODE_PAPER_COPY = "PAPER_COPY";
}
