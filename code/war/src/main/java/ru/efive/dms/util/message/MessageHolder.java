package ru.efive.dms.util.message;

import javax.faces.application.FacesMessage;

import static javax.faces.application.FacesMessage.*;

/**
 * Author: Upatov Egor <br>
 * Date: 09.09.2014, 13:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: Хранилище сообщений для FACES MESSAGE<br>
 */
public class MessageHolder {

    public static final FacesMessage MSG_CANT_DELETE = new FacesMessage(SEVERITY_ERROR, "Невозможно удалить документ", "");
    public static final FacesMessage MSG_CANT_SAVE = new FacesMessage(SEVERITY_ERROR, "Документ не может быть сохранен. Попробуйте повторить позже.", "");

    public static final FacesMessage MSG_ERROR_ON_DELETE = new FacesMessage(SEVERITY_ERROR, "Внутренняя ошибка при удалении.", "");
    public static final FacesMessage MSG_ERROR_ON_INITIALIZE = new FacesMessage(SEVERITY_ERROR, "Внутренняя ошибка при инициализации.", "");
    public static final FacesMessage MSG_ERROR_ON_SAVE = new FacesMessage(SEVERITY_ERROR, "Внутренняя ошибка при сохранении.", "");
    public static final FacesMessage MSG_ERROR_ON_SAVE_NEW = new FacesMessage(SEVERITY_ERROR, "Внутренняя ошибка при сохранении нового документа.", "");
    public static final FacesMessage MSG_ERROR_ON_ATTACH = new FacesMessage(SEVERITY_ERROR, "Внутренняя ошибка при вложении файла.", "");

    public static final FacesMessage MSG_CONTROLLER_NOT_SET = new FacesMessage(SEVERITY_ERROR, "Необходимо выбрать Руководителя", "");
    public static final FacesMessage MSG_CONTRAGENT_NOT_SET = new FacesMessage(SEVERITY_ERROR, "Необходимо выбрать Корреспондента", "");
    public static final FacesMessage MSG_RECIPIENTS_NOT_SET = new FacesMessage(SEVERITY_ERROR, "Необходимо выбрать Адресатов", "");
    public static final FacesMessage MSG_SHORT_DESCRIPTION_NOT_SET = new FacesMessage(SEVERITY_ERROR, "Необходимо заполнить Краткое содержание", "");
    public static final FacesMessage MSG_EXECUTOR_NOT_SET = new FacesMessage(SEVERITY_ERROR, "Необходимо выбрать Ответственного исполнителя", "");


    public static final FacesMessage MSG_CANT_DELETE_EXISTS_LINK_WITH_OTHER_DOCUMENT  = new FacesMessage(
            SEVERITY_ERROR, "Невозможно удалить документ, так как он связан с ''{0}'' ", "");

    public static final FacesMessage MSG_CANT_DELETE_CONTRAGENT_DOCUMENTS_EXISTS = new FacesMessage(SEVERITY_ERROR, "Невозможно удалить контрагента, так как к нему привязаны документы.", "");
    public static final FacesMessage MSG_RB_CONTRAGENT_TYPE_IS_USED_BY_SOME_CONTRAGENTS = new FacesMessage(SEVERITY_ERROR, "Невозможно удалить тип контрагента, так как к нему привязаны контрагенты.", "");


    public static final FacesMessage MSG_NO_NUMERATOR_FOUND = new FacesMessage(SEVERITY_ERROR, "Не найдено подходящего нумератора", "");
    public static final FacesMessage MSG_NUMERATOR_ALREADY_IN_USE = new FacesMessage(SEVERITY_ERROR, "Документ уже использует нумерацию", "");;

    //Сообщения об ошибках авторизации
    public final static FacesMessage MSG_AUTH_FIRED = new FacesMessage(SEVERITY_ERROR, "Невозможно выполнить вход, потому что сотрудник уволен", "");
    public final static FacesMessage MSG_AUTH_DELETED = new FacesMessage(SEVERITY_ERROR, "Невозможно выполнить вход, потому что сотрудник удален", "");
    public final static FacesMessage MSG_AUTH_ERROR = new FacesMessage(SEVERITY_ERROR, "Ошибка при входе в систему. Попробуйте повторить позже.", "");
    public final static FacesMessage MSG_AUTH_NOT_FOUND = new FacesMessage(SEVERITY_ERROR, "Введены неверные данные.", "");
    public final static FacesMessage MSG_AUTH_NO_ROLE = new FacesMessage(SEVERITY_ERROR, "Обратитесь к администратору системы для получения доступа", "");

    //ORD-42 факты просмотров документов
    public static final FacesMessage MSG_VIEW_FACT_REGISTERED = new FacesMessage(SEVERITY_INFO, "Факт просмотра документа сохранен", "");
    public static final FacesMessage MSG_VIEW_FACT_REGISTRATION_ERROR = new FacesMessage(SEVERITY_WARN, "Ошибка при регистрации факта просмотра документа", "");

    // Неизветсная внутренняя ошибка
    public static final FacesMessage MSG_INTERNAL_ERROR = new FacesMessage(SEVERITY_ERROR, "Внутренняя ошибка", "");


    //Отчеты
    public static final FacesMessage MSG_ERROR_ON_REPORT_CREATION = new FacesMessage(SEVERITY_ERROR, "Ошибка при формировании отчета", "");
    public static final FacesMessage MSG_ERROR_ON_REPORT_COMPILATION =
            new FacesMessage(SEVERITY_ERROR, "Отчет ''{0}'' не может быть сформирован из-за ошибки в шаблоне [{1}]. Текст ошибки: {2}", "");
    public static final FacesMessage MSG_ERROR_ON_REPORT_EXECUTION =
            new FacesMessage(SEVERITY_ERROR, "Отчет ''{0}'' не может быть сформирован из-за ошибки во время выполнения. Текст ошибки: {1}", "");
    public static final FacesMessage MSG_ERROR_ON_REPORT_EXPORT =
            new FacesMessage(SEVERITY_ERROR, "Отчет ''{0}'' сформирован, но не может быть экспортирован в выбранный формат. Текст ошибки: {1}", "");
    // Расширенные поиски
    public static final FacesMessage MSG_CANT_DO_SEARCH = new FacesMessage(SEVERITY_ERROR, "Невозможно осуществить поиск", "");


    public static final FacesMessage MSG_CONVERTER_ERROR = new FacesMessage(SEVERITY_ERROR, "Ошибка при конвертации", "");


    // Ошибки валидации страницы замещений
    public static final FacesMessage MSG_SUBSTITUTION_DATE_MISMATCH = new FacesMessage("Ошибка валидации", "Дата начала периода замещения позже чем его дата окончания");
    public static final FacesMessage MSG_SUBSTITUTION_SUBSTITUTOR_NOT_SET = new FacesMessage("Ошибка валидации", "Не выбран заместитель");
    public static final FacesMessage MSG_SUBSTITUTION_PERSON_NOT_SET = new FacesMessage("Ошибка валидации", "Не выбрано замещаемое лицо");
    public static final FacesMessage MSG_SUBSTITUTION_PERSON_DUPLICATE = new FacesMessage("Ошибка валидации", "Замещаемое лицо и заместитель один и тот-же человек.");

    //Сообщения для документа и holder-бина
    public static final FacesMessage MSG_CANT_CREATE = new FacesMessage(SEVERITY_ERROR, "У вас нет прав на создание нового документа", "");

    public static final FacesMessage MSG_NOT_ALLOW_TO_VIEW = new FacesMessage(SEVERITY_ERROR, "У вас нет прав на просмотр и работу с этим типом документов", "");
    public static final FacesMessage MSG_NOT_ALLOW_TO_VIEW_ITEM = new FacesMessage(SEVERITY_ERROR, "У вас нет прав на просмотр и работу с этим документом", "");

    public static final FacesMessage MSG_CANT_EDIT_WITHOUT_ID = new FacesMessage(SEVERITY_ERROR, "Невозможно проводить редактирование не указав документ", "");

    public static final FacesMessage MSG_UNKNOWN_ACTION = new FacesMessage(SEVERITY_WARN, "Неизвестное действие при открытии документа", "");
    ;
    public static final FacesMessage MSG_NO_DOC_ID = new FacesMessage(SEVERITY_ERROR, "Не указан идентификатор документа", "");
    public static final FacesMessage MSG_DOC_ID_CONVERSION_ERROR = new FacesMessage(SEVERITY_ERROR, "Некорректное значение идентифкатора", "");
    public static final FacesMessage MSG_DOCUMENT_IS_DELETED = new FacesMessage(SEVERITY_ERROR, "Запрошенный документ помечен как удаленный", "");
    public static final FacesMessage MSG_DOCUMENT_NOT_FOUND = new FacesMessage(SEVERITY_ERROR, "Запрошенный документ не найден", "");
    public static final FacesMessage MSG_TRY_TO_EDIT_WITHOUT_PERMISSION = new FacesMessage(SEVERITY_ERROR, "У вас нет прав на редактирование документа", "");
    public static final FacesMessage MSG_TRY_TO_VIEW_WITHOUT_PERMISSION = new FacesMessage(SEVERITY_ERROR, "У вас нет прав на просмотр документа", "");
    public static final FacesMessage MSG_TRY_TO_VIEW_WITH_LESSER_ACCESS_LEVEL = new FacesMessage(SEVERITY_WARN, "Уровень доступа к документу \'%s\' выше чем ваш текущий уровень доступа \'%s\'", "");

    // Ошибки валидации
    public static final FacesMessage MSG_REQUIRED_PARAMETER_NOT_SET = new FacesMessage(SEVERITY_ERROR, "Обязательный параметр ''{0}'' не указан", "");



}
