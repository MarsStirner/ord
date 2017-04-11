package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;


@FacesConverterWithSpringSupport("PersonConverter")
public class PersonConverter implements Converter {

    @Autowired
    @Qualifier("userDao")
    private UserDao userDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            User in_user = userDao.getByLogin(value);
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