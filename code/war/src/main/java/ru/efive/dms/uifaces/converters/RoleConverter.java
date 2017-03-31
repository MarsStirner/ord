package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.Role;
import ru.hitsl.sql.dao.interfaces.referencebook.RoleDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

@FacesConverter("RoleConverter")
public class RoleConverter implements Converter {

    @Autowired
    @Qualifier("roleDao")
    private RoleDao roleDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            List<Role> list = roleDao.getByValue(value);
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
        return value != null ? ((Role) value).getCode() : null;
    }

}