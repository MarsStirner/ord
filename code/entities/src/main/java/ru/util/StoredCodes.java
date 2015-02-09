package ru.util;

/**
 * Author: Upatov Egor <br>
 * Date: 18.08.2014, 19:01 <br>
 * Company: Korus Consulting IT <br>
 * Description: Класс-хранилище кодов <br>
 */
public final class StoredCodes {
    /**
     * Типы контактных данных
     */
    public class ContactInfoType {
        /**
         * Мобильный телефон
         */
        public static final String MOBILE_PHONE = "mobilePhone";
        /**
         * Рабочий телефон
         */
        public static final String WORK_PHONE = "workPhone";
        /**
         * Номер телефона
         */
        public static final String PHONE = "phone";
        /**
         * Внутренний номер телефона
         */
        public static final String INTERNAL_PHONE = "internalPhone";
        /**
         * Адрес электронной почты
         */
        public static final String EMAIL = "email";
    }

    public class RoleType {
        public static final String ADMINISTRATOR = "ADMINISTRATOR";
        public static final String RECORDER = "RECORDER";
        public static final String OFFICE_MANAGER = "OFFICE_MANAGER";
        public static final String REQUEST_MANAGER = "REQUEST_MANAGER";
        public static final String EMPLOYER = "EMPLOYER";
        public static final String OUTER = "OUTER";
        public static final String HR = "HR";
        public static final String FILLING = "FILLING";
    }

    /**
     * Типы доставки
     */
    public class DeliveryType {
        /**
         * Почта
         */
        public static final String MAIL = "MAIL";
        /**
         * Факс
         */
        public static final String FAX = "FAX";
        /**
         * Электронная почта
         */
        public static final String EMAIL = "EMAIL";
        /**
         * Курьером
         */
        public static final String COURIER = "COURIER";
    }
}