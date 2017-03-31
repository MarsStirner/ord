package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.GroupType;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupTypeDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

@FacesConverter("GroupTypeConverter")
public class GroupTypeConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger("CONVERTER");

    @Autowired
    @Qualifier("groupTypeDao")
    private GroupTypeDao groupTypeDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {

            List<GroupType> list = groupTypeDao.getByValue(value);
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