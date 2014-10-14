package ru.efive.dms.uifaces.converters;

import ru.efive.dms.dao.DeliveryTypeDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.document.DeliveryType;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.DELIVERY_TYPE_DAO;

@FacesConverter("DeliveryTypeConverter")
public class DeliveryTypeConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<DeliveryType> list = sessionManagement.getDictionaryDAO(DeliveryTypeDAOImpl.class, DELIVERY_TYPE_DAO).findByValue(value);
            if (list.size() > 0) {
                result = list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
                System.out.println("Не найден вид документа");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((DeliveryType) value).getValue();
    }

}