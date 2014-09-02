package ru.efive.dms.uifaces.converters;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.efive.sql.entity.user.Group;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("GroupConverter")
public class GroupConverter implements Converter {
    private static final Logger LOGGER =LoggerFactory.getLogger("CONVERTER");

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            //TODO не слишком ли жирно получать из контекста менеджера сессий?!
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
            final Group in_group = sessionManagement.getDAO(GroupDAOHibernate.class, ApplicationHelper.GROUP_DAO).findGroupByAlias(value);
            if (in_group != null) {
               LOGGER.debug("alias: " + in_group.getDescription());
               return in_group;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
                LOGGER.error("GroupConverter: FAIL to Object. String=" + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Group) {
            return ((Group) value).getDescription();
        } else {
            LOGGER.error("GroupConverter: FAIL to String. Object=" + value.toString());
            return value.toString();
        }
    }
}