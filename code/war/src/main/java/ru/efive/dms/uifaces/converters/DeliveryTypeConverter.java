package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DeliveryType;
import ru.hitsl.sql.dao.referenceBook.DeliveryTypeDAOImpl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.DELIVERY_TYPE_DAO;

@FacesConverter("DeliveryTypeConverter")
public class DeliveryTypeConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger("CONVERTER");

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
         try {
            SessionManagementBean sessionManagement =  context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<DeliveryType> list = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, DELIVERY_TYPE_DAO).getByValue(value);
            if (!list.isEmpty()) {
                return list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
               LOGGER.error("DELIVERY_TYPE: Не найден \'{}\'", value);
            }
        } catch (Exception e) {
            LOGGER.error("DELIVERY_TYPE", e);
        }
        return null;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((DeliveryType) value).getValue();
    }

}