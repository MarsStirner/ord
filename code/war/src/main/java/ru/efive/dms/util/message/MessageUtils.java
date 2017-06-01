package ru.efive.dms.util.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.text.MessageFormat;

public class MessageUtils {
    private static final Logger log = LoggerFactory.getLogger(MessageUtils.class);

    public static void addMessage(FacesMessage message, Object... params) {
        addMessage(new FacesMessage(message.getSeverity(), MessageFormat.format(message.getSummary(), params), null));
    }


    public static void addMessage(final FacesMessage message) {
        addMessage(null, message);
    }

    public static void addMessage(final String tag, final FacesMessage message) {
        FacesContext.getCurrentInstance().addMessage(tag, message);
        log.info("[{}][tag={}] {} ({})",message.getSeverity(), tag, message.getSummary(), message.getDetail());
    }
}
