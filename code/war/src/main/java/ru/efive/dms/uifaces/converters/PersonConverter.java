package ru.efive.dms.uifaces.converters;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("PersonConverter")
public class PersonConverter implements Converter {
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);

            User in_user = ((UserDAOHibernate) sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO)).getByLogin(value);
            if (in_user != null) {
                result = in_user;
                System.out.println("login: " + in_user.getDescription());
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));

                System.out.println("Не найден пользователь по логину");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value != null ? ((User) value).getDescriptionShort() : "";
    }
}