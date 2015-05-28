package ru.efive.dms.uifaces.beans.dialogs;

import com.google.common.collect.ImmutableMap;
import org.primefaces.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 12.03.2015, 16:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class AbstractDialog<T> implements Serializable {

    public enum Button{
        CONFIRM,
        CLOSE,
        CLEAR
    }

    public class DialogResult{
        private Button button;
        private Object result;

        public DialogResult(Button button, Object result){
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
            final StringBuilder sb = new StringBuilder("DialogResult{");
            sb.append("button=").append(button);
            sb.append(", result=").append(result);
            sb.append('}');
            return sb.toString();
        }
    }


    protected static final Logger logger = LoggerFactory.getLogger("DIALOG");

    private static Map<String, Object> viewParams = ImmutableMap.of("modal", (Object)true, "draggable", false, "resizable", false);

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
     * Очистить выделение
     */
    public void clearSelected(){
        logger.debug("DIALOG_BTN_CLEAR:  pressed");
        selected = null;
    }

    /**
     * Закрыть диалог по кнопке "Выбрать"
     */
    public void confirmSelection(){
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
