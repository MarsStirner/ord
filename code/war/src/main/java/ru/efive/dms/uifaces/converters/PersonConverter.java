package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.user.UserDAOHibernate;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.USER_DAO;

@FacesConverter("PersonConverter")
public class PersonConverter implements Converter {
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);

            User in_user = (sessionManagement.getDAO(UserDAOHibernate.class, USER_DAO)).getByLogin(value);
            if (in_user != null) {
                result = in_user;
                System.out.println("login: " + in_user.getDescription());
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);

                System.out.println("Не найден пользователь по логину");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            if (value instanceof User) {
                return ((User) value).getDescriptionShort();
            } else {
                return value.toString();
            }
        }
        return "";
    }

}