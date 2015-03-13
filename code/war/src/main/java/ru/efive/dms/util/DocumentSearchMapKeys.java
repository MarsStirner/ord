package ru.efive.dms.util;

/**
 * Author: Upatov Egor <br>
 * Date: 05.03.2015, 16:43 <br>
 * Company: Korus Consulting IT <br>
 * Description: Список ключей для карты, используемой в поиске по критериям <br>
 */
public final class DocumentSearchMapKeys {
    /**
     * Статус документа
     */
    public static final String STATUS_KEY = "statusId";

    /**
     * Автор документа
     */
    public static final String AUTHOR_KEY = "author";

    /**
     * Руководитель документа
     */
    public static final String CONTROLLER_KEY = "controller";

    /**
     * Вид документа
     */
    public static final String FORM_KEY = "form";

    /**
     * регистрационный номер
     */
    public static final String REGISTRATION_NUMBER_KEY = "registrationNumber";

    /**
     * Номер поступившего
     */
    public static final String RECEIVED_DOCUMENT_NUMBER_KEY = "receivedDocumentNumber";

    /**
     * Дата создания от .. до
     */
    public static final String START_CREATION_DATE_KEY = "startCreationDate";
    public static final String END_CREATION_DATE_KEY = "endCreationDate";

    /**
     * Дата регистрации от .. до
     */
    public static final String START_REGISTRATION_DATE_KEY = "startRegistrationDate";
    public static final String END_REGISTRATION_DATE_KEY = "endRegistrationDate";

    /**
     * Дата подписания от .. до   (OUTGOING)
     */
    public static final String START_SIGNATURE_DATE_KEY = "startSignatureDate";
    public static final String END_SIGNATURE_DATE_KEY = "endSignatureDate";

    /**
     * @deprecated  не используется, но возможно нужно
     * Дата отправки от .. до   (OUTGOING)
     */
    @Deprecated
    public static final String START_SENDING_DATE_KEY = "startSendingDate";
    @Deprecated
    public static final String END_SENDING_DATE_KEY = "endSendingDate";

    /**
     * Дата доставки от .. до
     */
    public static final String START_DELIVERY_DATE_KEY = "startDeliveryDate";
    public static final String END_DELIVERY_DATE_KEY = "endDeliveryDate";

    /**
     * Срок исполнения от .. до
     */
    public static final String START_EXECUTION_DATE_KEY = "startExecutionDate";
    public static final String END_EXECUTION_DATE_KEY = "endExecutionDate";

    /**
     * Дата поступившего от .. до
     */
    public static final String START_RECEIVED_DATE_KEY = "startReceivedDocumentDate";
    public static final String END_RECEIVED_DATE_KEY = "endReceivedDocumentDate";

    /**
     * Тип доставки
     */
    public static final String DELIVERY_TYPE_KEY = "deliveryType";

    /**
     * Контрагент
     */
    public static final String CONTRAGENT_KEY = "contragent";

    /**
     * Исполнители (LIST<USER>)
     */
    public static final String EXECUTORS_KEY = "executors";

    /**
     * Адресаты (LIST<USER>)
     */
    public static final String RECIPIENTS_KEY ="recipientUsers";

    /**
     * Краткое содержание
     */
    public static final String SHORT_DESCRIPTION_KEY = "shortDescription";


    /**
     * Том дела
     */
    public static final String OFFICE_KEEPING_VOLUME_KEY = "officeKeepingVolume";

}
