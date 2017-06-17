package ru.hitsl.sql.dao.util;

import ru.entity.model.document.OfficeKeepingVolume;
import ru.entity.model.referenceBook.Contragent;
import ru.entity.model.referenceBook.DeliveryType;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.user.User;
import ru.entity.model.workflow.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;

/**
 * Author: Upatov Egor <br>
 * Date: 05.03.2015, 16:43 <br>
 * Company: Korus Consulting IT <br>
 * Description: Список ключей для карты, используемой в поиске по критериям <br>
 */
public final class DocumentSearchMapKeys {

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Общие
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Статус {@link Status}
     */
    public static final String STATUS = "status";

    /**
     * Статус не равен {@link Status}
     */
    public static final String STATUS_NOT = "status.not";


    /**
     * Список статусов {@link Collection<Status>}
     */
    public static final String STATUSES = "statuses";

    /**
     * Статус документа в строковом представлении {@link String}
     */
    public static final String STATUS_CODE = "status.code";

    /**
     * Список статусов документа в строковом представлении {@link Collection<String>}
     */
    public static final String STATUSES_CODE = "statuses.code";

    /**
     * Автор документа {@link User}
     */
    public static final String AUTHOR = "author";

    /**
     * Список авторов документа {@link Collection<User>}
     */
    public static final String AUTHORS = "authors";

    /**
     * Руководитель документа {@link User}
     */
    public static final String CONTROLLER = "controller";

    /**
     * Вид документа {@link DocumentForm}
     */
    public static final String FORM = "form";

    /**
     * Вид документа (наименование) {@link String}
     */
    public static final String FORM_VALUE = "form.value";

    /**
     * Код типа документа в виде документа {@link String}
     */
    @Deprecated
    public static final String FORM_DOCUMENT_TYPE_CODE = "form.documentType.code";

    /**
     * регистрационный номер {@link String}
     */
    public static final String REGISTRATION_NUMBER = "registrationNumber";

    /**
     * Дата регистрации от {@link LocalDateTime}
     */
    public static final String REGISTRATION_DATE_START = "registrationDate.start";

    /**
     * Дата регистрации до {@link LocalDateTime} {@link LocalDate}
     */
    public static final String REGISTRATION_DATE_END = "registrationDate.end";

    /**
     * Дата создания от {@link LocalDateTime}
     */
    public static final String CREATION_DATE_START = "creationDate.start";

    /**
     * Дата создания до {@link LocalDateTime} {@link LocalDate}
     */
    public static final String CREATION_DATE_END = "creationDate.end";

    /**
     * регистрационный номер {@link String}
     */
    public static final String SHORT_DESCRIPTION = "shortDescription";


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Специфичные для конкретных типов / видов документов
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Дата подписания от {@link LocalDateTime}
     * {@link DocumentType#INTERNAL}
     * {@link DocumentType#OUTGOING}
     */
    public static final String SIGNATURE_DATE_START = "signatureDate.start";

    /**
     * Дата подписания до {@link LocalDateTime} {@link LocalDate}
     * {@link DocumentType#INTERNAL}
     * {@link DocumentType#OUTGOING}
     */
    public static final String SIGNATURE_DATE_END = "signatureDate.end";

    /**
     * Дата исполнения равна {@link LocalDateTime}
     * {@link DocumentType#REQUEST}
     */
    public static final String EXECUTION_DATE = "executionDate";

    /**
     * Дата исполнения от {@link LocalDateTime}
     * {@link DocumentType#INTERNAL}
     * {@link DocumentType#INCOMING}
     * {@link DocumentType#REQUEST}
     */
    public static final String EXECUTION_DATE_START = "executionDate.start";

    /**
     * Дата исполнения до {@link LocalDateTime} {@link LocalDate}
     * {@link DocumentType#INTERNAL}
     * {@link DocumentType#INCOMING}
     * {@link DocumentType#REQUEST}
     */
    public static final String EXECUTION_DATE_END = "executionDate.end";

    /**
     * Ответственный {@link User}
     * {@link DocumentType#INTERNAL}
     * {@link DocumentType#REQUEST}
     */
    public static final String RESPONSIBLE = "responsible";

    /**
     * Адресаты {@link Collection<User>}
     * {@link DocumentType#INTERNAL}
     * {@link DocumentType#INCOMING}
     * {@link DocumentType#REQUEST}
     */
    public static final String RECIPIENTS = "recipientUsers";

    /**
     * Исполнители {@link Collection<User>}
     * {@link DocumentType#INTERNAL}
     * {@link DocumentType#OUTGOING}
     */
    public static final String EXECUTORS = "executors";

    /**
     * Регистрация закрытого периода {@link Boolean}
     * {@link DocumentType#INTERNAL}
     */
    public static final String CLOSE_PERIOD_REGISTRATION_FLAG = "closePeriodRegistrationFlag";

    /**
     * Дата поступившего от {@link LocalDateTime}
     * {@link DocumentType#INCOMING}
     * {@link DocumentType#REQUEST}
     */
    public static final String DELIVERY_DATE_START = "deliveryDate.start";

    /**
     * Дата поступившего до {@link LocalDateTime} {@link LocalDate}
     * {@link DocumentType#INCOMING}
     * {@link DocumentType#REQUEST}
     */
    public static final String DELIVERY_DATE_END = "deliveryDate.end";

    /**
     * Дата доставки от {@link LocalDateTime}
     * {@link DocumentType#INCOMING}
     */
    public static final String RECEIVED_DATE_START = "receivedDocumentDate.start";

    /**
     * Дата доставки до {@link LocalDateTime} {@link LocalDate}
     * {@link DocumentType#INCOMING}
     */
    public static final String RECEIVED_DATE_END = "receivedDocumentDate.end";

    /**
     * Номер поступившего {@link String}
     * {@link DocumentType#INCOMING}
     */
    public static final String RECEIVED_DOCUMENT_NUMBER = "receivedDocumentNumber";

    /**
     * Тип доставки {@link DeliveryType}
     * {@link DocumentType#INCOMING}
     * {@link DocumentType#OUTGOING}
     * {@link DocumentType#REQUEST}
     */
    public static final String DELIVERY_TYPE = "deliveryType";

    /**
     * Контрагент {@link Contragent}
     * {@link DocumentType#INCOMING}
     * {@link DocumentType#OUTGOING}
     * {@link DocumentType#REQUEST}
     */
    public static final String CONTRAGENT = "contragent";

    /**
     * Том дела {@link OfficeKeepingVolume}
     * {@link DocumentType#INCOMING}
     */
    public static final String OFFICE_KEEPING_VOLUME = "officeKeepingVolume";

    /**
     * Имя отправителя {@link String}
     * {@link DocumentType#REQUEST}
     */
    public static final String SENDER_FIRST_NAME = "senderFirstName";

    /**
     * Фамилия отправителя {@link String}
     * {@link DocumentType#REQUEST}
     */
    public static final String SENDER_LAST_NAME = "senderLastName";

    /**
     * Отчество отправителя {@link String}
     * {@link DocumentType#REQUEST}
     */
    public static final String SENDER_PATR_NAME = "senderPatrName";

    /**
     * Документ-основание {@link String}
     * {@link DocumentType#TASK}
     */
    public static final String ROOT_DOCUMENT_ID = "rootDocumentId";

    /**
     * Документ-основание {@link String}
     * {@link DocumentType#TASK}
     */
    public static final String TASK_DOCUMENT_ID = "taskDocumentId";
}
