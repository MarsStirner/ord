package ru.efive.dms.uifaces.converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("PersonListConverter")
public class PersonListConverter implements Converter {
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
            List<User> in_users = new ArrayList<User>();
            List<String> values = Arrays.asList(value.split("<br \\>"));
            for (String e : values) {
                User in_user = ((UserDAOHibernate) sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO)).getByLogin(e);
                if (in_user != null) in_users.add(in_user);
            }
            result = in_users;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        StringBuffer mresult = new StringBuffer();
        Iterator iterator = ((List) value).iterator();
        while (iterator.hasNext()) {
            User in_user = (User) iterator.next();
            mresult.append(in_user.getLogin()).append("<br \\>");
        }
        return mresult.toString();
    }
}