package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.UserAccessLevel;
import ru.hitsl.sql.dao.interfaces.referencebook.UserAccessLevelDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

@FacesConverter("UserAccessLevelConverter")
public class UserAccessLevelConverter implements Converter {

    @Autowired
    @Qualifier("userAccessLevelDao")
    private UserAccessLevelDao userAccessLevelDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            List<UserAccessLevel> list = userAccessLevelDao.getByValue(value);
            if (list.size() > 0) {
                result = list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
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