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
}