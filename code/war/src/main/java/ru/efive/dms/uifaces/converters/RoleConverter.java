package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.user.Role;
import ru.hitsl.sql.dao.user.RoleDAOHibernate;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.ROLE_DAO;

@FacesConverter("RoleConverter")
public class RoleConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<Role> list = sessionManagement.getDAO(RoleDAOHibernate.class, ROLE_DAO).findByValue(value);
            if (list.size() > 0) {
                result = list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                System.out.println("Не найдены значения");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Role) value).getName();
    }

}