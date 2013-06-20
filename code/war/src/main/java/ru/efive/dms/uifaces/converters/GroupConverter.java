package ru.efive.dms.uifaces.converters;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;


import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.efive.sql.entity.user.Group;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("GroupConverter")
public class GroupConverter implements Converter {
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);

            Group in_group = ((GroupDAOHibernate) sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO)).findGroupByAlias(value);
            if (in_group != null) {
                result = in_group;
                System.out.println("alias: " + in_group.getDescription());
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
        System.out.println("group get as string" + value.toString());
        return ((Group) value).getDescription();
    }
}