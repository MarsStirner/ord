package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.SenderTypeDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.document.SenderType;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.SENDER_TYPE_DAO;

@FacesConverter("SenderTypeConverter")
public class SenderTypeConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger("CONVERTER");

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context,
                    "#{sessionManagement}", SessionManagementBean.class);
            List<SenderType> list = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, SENDER_TYPE_DAO)
                    .findByValue(value);
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