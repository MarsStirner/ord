package ru.efive.dms.uifaces.beans.dialogs;

import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 12.03.2015, 16:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class AbstractDialog<T> implements Serializable {

    protected static final Logger logger = LoggerFactory.getLogger("DIALOG");
    private static Map<String, Object> viewOptions;

    static {
        viewOptions = new HashMap<>();
        viewOptions.put("modal", true);
        viewOptions.put("draggable", true);
        viewOptions.put("width", "50%");
        viewOptions.put("contentWidth", "100%");
        viewOptions.put("contentHeight", "98%"); //Не 100%, т.к. на 100 появляется скролл
        viewOptions.put("resizable", false);
        viewOptions.put("height", "80vh");
        viewOptions.put("position", "top");
    }

    /**
     * Заголовок диалога
     */
    protected String title;
    /**
     * Выбранный элемент(-ы)
     */
    protected T selected;

    public static Map<String, Object> getViewOptions() {
        return viewOptions;
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
     * Очистить выделение
     */
    public void clearSelected() {
        logger.debug("DIALOG_BTN_CLEAR:  pressed");
        selected = null;
    }

    /**
     * Закрыть диалог по кнопке "Выбрать"
     */
    public void confirmSelection() {
        final DialogResult result = new DialogResult(Button.CONFIRM, selected);
        logger.debug("DIALOG_BTN_CONFIRM:  {}", result);
        RequestContext.getCurrentInstance().closeDialog(result);
    }

    /**
     * Закрыть диалог по кнопке "Закрыть"
     */
    public void closeDialog() {
        final DialogResult result = new DialogResult(Button.CLOSE, null);
        logger.debug("DIALOG_BTN_CLOSE:  {}", result);
        RequestContext.getCurrentInstance().closeDialog(result);
    }


    /**
     * Установить заранее заданные значения
     */
    public abstract void initializePreSelected();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public T getSelected() {
        return selected;
    }

    public void setSelected(T selected) {
        this.selected = selected;
    }

    @SuppressWarnings("unchecked")
    public void onRowSelect(final SelectEvent event) {
        if (selected instanceof Collection) {
            ((Collection) selected).add(event.getObject());
        } else {
            selected = (T) event.getObject();
        }
    }

    @SuppressWarnings("unchecked")
    public void onRowUnSelect(final UnselectEvent event) {
        if (selected instanceof Collection) {
            ((Collection) selected).remove(event.getObject());
        } else {
            selected = null;
        }
    }

    protected Object getFromExternalContext(final String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove(key);
    }

    public enum Button {
        CONFIRM,
        CLOSE,
        CLEAR
    }

    public class DialogResult implements Serializable {
        private Button button;
        private Object result;

        public DialogResult(Button button, Object result) {
            this.button = button;
            this.result = result;
        }

        public Button getButton() {
            return button;
        }

        public void setButton(final Button button) {
            this.button = button;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(final Object result) {
            this.result = result;
        }

        @Override
        public String toString() {
            return "DialogResult{" + "button=" + button + ", result=" + result + '}';
        }
    }
}
