package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.sql.dao.user.UserAccessLevelDAO;
import ru.entity.model.user.UserAccessLevel;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.USER_ACCESS_LEVEL_DAO;

@FacesConverter("UserAccessLevelConverter")
public class UserAccessLevelConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context,
                    "#{sessionManagement}", SessionManagementBean.class);
            List<UserAccessLevel> list = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class,
                    USER_ACCESS_LEVEL_DAO).findByValue(value);
            if (list.size() > 0) {
                result = list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                System.out.println("Не найден уровень доступа");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((UserAccessLevel) value).getValue();
    }

}