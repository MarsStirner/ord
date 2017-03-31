package ru.entity.model.referenceBook;

import ru.entity.model.mapped.ReferenceBookEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Типы доставки документов
 */
@Entity
@Table(name = "rbDeliveryType")
public class DeliveryType extends ReferenceBookEntity {
    /**
     * Предопределенные коды для справочника типов доставки
     */
    /**
     * Почта
     */
    public static final String RB_CODE_MAIL = "MAIL";
    /**
     * Факс
     */
    public static final String RB_CODE_FAX = "FAX";
    /**
     * Электронная почта
     */
    public static final String RB_CODE_EMAIL = "EMAIL";
    /**
     * Курьером
     */
    public static final String RB_CODE_COURIER = "COURIER";

}