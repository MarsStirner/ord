package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.efive.sql.dao.user.GroupTypeDAO;
import ru.entity.model.enums.GroupType;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.GROUP_TYPE_DAO;

@FacesConverter("GroupTypeConverter")
public class GroupTypeConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger("CONVERTER");

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context,
                    "#{sessionManagement}", SessionManagementBean.class);
            List<GroupType> list = sessionManagement.getDictionaryDAO(GroupTypeDAO.class, GROUP_TYPE_DAO).findByValue
                    (value);
            if (!list.isEmpty()) {
                return list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                LOGGER.error("GROUP_TYPE: Не найден тип группы \'{}\'", value);
            }
        } catch (Exception e) {
            LOGGER.error("GROUP_TYPE", e);
        }
        return null;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((GroupType) value).getValue();
    }

}