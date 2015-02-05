package ru.efive.dms.uifaces.beans.utils;

import javax.faces.application.FacesMessage;

/**
 * Author: Upatov Egor <br>
 * Date: 09.09.2014, 13:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: Хранилище сообщений для FACES MESSAGE<br>
 */
public class MessageHolder {
    public static final FacesMessage MSG_CANT_DELETE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Невозможно удалить документ", "");
    public static final FacesMessage MSG_CANT_SAVE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Документ не может быть сохранен. Попробуйте повторить позже.", "");

    public static final FacesMessage MSG_ERROR_ON_DELETE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка при удалении.", "");
    public static final FacesMessage MSG_ERROR_ON_INITIALIZE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка при инициализации.", "");
    public static final FacesMessage MSG_ERROR_ON_SAVE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка при сохранении.", "");
    public static final FacesMessage MSG_ERROR_ON_SAVE_NEW = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка при сохранении нового документа.", "");
    public static final FacesMessage MSG_ERROR_ON_ATTACH = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Внутренняя ошибка при вложении файла.", "");

    public static final FacesMessage MSG_CONTROLLER_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Руководителя", "");
    public static final FacesMessage MSG_CONTRAGENT_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Корреспондента", "");
    public static final FacesMessage MSG_RECIPIENTS_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Адресатов", "");
    public static final FacesMessage MSG_SHORT_DESCRIPTION_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо заполнить Краткое содержание", "");
    public static final FacesMessage MSG_EXECUTOR_NOT_SET = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Необходимо выбрать Ответственного исполнителя", "");

    //Сообщения об ошибках авторизации
    public final static FacesMessage MSG_AUTH_FIRED = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Невозможно выполнить вход, потому что сотрудник уволен", "");
    public final static FacesMessage MSG_AUTH_DELETED = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Невозможно выполнить вход, потому что сотрудник удален", "");
    public final static FacesMessage MSG_AUTH_ERROR = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка при входе в систему. Попробуйте повторить позже.", "");
    public final static FacesMessage MSG_AUTH_NOT_FOUND = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Введены неверные данные.", "");
    public final static FacesMessage MSG_AUTH_NO_ROLE = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Обратитесь к администратору системы для получения доступа", "");
    public final static FacesMessage MSG_AUTH_NO_MAX_ACCESS_LEVEL = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Обратитесь к администратору системы для назначения корректного уровня допуска", "");


    //ORD-42 факты просмотров документов
    public static final FacesMessage MSG_VIEW_FACT_REGISTERED = new FacesMessage(FacesMessage.SEVERITY_INFO, "Факт просмотра документа сохранен", "");
}
