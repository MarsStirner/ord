package ru.efive.dms.uifaces.beans.dialogs;

import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 12.03.2015, 16:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class AbstractDialog<T> {

    protected static final Logger logger = LoggerFactory.getLogger("DIALOG");

    private static Map<String, Object> viewParams = new HashMap<String, Object>(3);
    static {
        viewParams.put("modal", true);
        viewParams.put("draggable", false);
        viewParams.put("resizable", false);
    }

    public static Map<String, Object> getViewParams(){
        return viewParams;
    }

    /**
     * Установить шапку по переданному ключу
     *
     * @param requestParameterMap карта get параметров
     * @return Строка с Заголовком
     */
    public String initializeTitle(Map<String, String> requestParameterMap) {
        return "Выберите элементы";
    }

    /**
     * Закрыть диалог с результатом
     *
     * @param withResult флаг указывающий передавать ли результат работы диалога
     */
    public void closeDialog(boolean withResult) {
        RequestContext.getCurrentInstance().closeDialog(withResult ? selected : null);
    }

    /**
     * Установить заранее заданные значения
     */
    public abstract void initializePreSelected();

    /**
     * Заголовок диалога
     */
    protected String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Выбранный элемент(-ы)
     */
    protected T selected;

    public T getSelected() {
        return selected;
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }
}
