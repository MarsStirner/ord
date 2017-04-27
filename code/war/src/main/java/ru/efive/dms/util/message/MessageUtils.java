package ru.efive.dms.util.message;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.text.MessageFormat;

public class MessageUtils {
    public static void addMessage(FacesMessage message, Object... params) {
        MessageUtils.addMessage(new FacesMessage(message.getSeverity(), MessageFormat.format(message.getSummary(), params), null));
    }


    public static void addMessage(final FacesMessage message) {
MessageUtils.addMessage( message);
    }

    public static void addMessage(final String tag, final FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(tag, message);
    }
}
