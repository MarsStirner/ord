package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.entity.model.user.Group;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import static ru.efive.dms.util.ApplicationDAONames.GROUP_DAO;

@FacesConverter("GroupConverter")
public class GroupConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger("CONVERTER");

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            //TODO не слишком ли жирно получать из контекста менеджера сессий?!
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context,
                    "#{sessionManagement}", SessionManagementBean.class);
            final Group in_group = sessionManagement.getDAO(GroupDAOHibernate.class, GROUP_DAO).findGroupByAlias(value);
            if (in_group != null) {
                LOGGER.debug("GROUP: alias=\'{}\'", in_group.getDescription());
                return in_group;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                LOGGER.error("GROUP: FAIL to Object. String=\'{}\'", value);
            }
        } catch (Exception e) {
            LOGGER.error("GROUP", e);
        }
        return null;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof Group) {
            return ((Group) value).getDescription();
        } else {
            LOGGER.error("GROUP: FAIL to String. Object=\'{}\'", value.toString());
            return value.toString();
        }
    }
}