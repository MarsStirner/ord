package ru.efive.dms.uifaces.converters;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import ru.efive.sql.dao.user.UserAccessLevelDAO;
import ru.efive.sql.entity.user.UserAccessLevel;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("UserAccessLevelConverter")
public class UserAccessLevelConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<UserAccessLevel> list = sessionManagement.getDictionaryDAO(UserAccessLevelDAO.class, ApplicationHelper.USER_ACCESS_LEVEL_DAO).findByValue(value);
            if (list.size() > 0) {
                result = list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
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