package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.SenderType;
import ru.hitsl.sql.dao.interfaces.referencebook.SenderTypeDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

@FacesConverter("SenderTypeConverter")
public class SenderTypeConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger("CONVERTER");

    @Autowired
    @Qualifier("senderTypeDao")
    private SenderTypeDao senderTypeDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {

            List<SenderType> list = senderTypeDao.getByValue(value);
            if (!list.isEmpty()) {
                return list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                LOGGER.error("SENDER_TYPE: Не найден вид документа \"{}\"", value);
            }
        } catch (Exception e) {
            LOGGER.error("SENDER_TYPE", e);
        }
        return null;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((SenderType) value).getValue();
    }

}