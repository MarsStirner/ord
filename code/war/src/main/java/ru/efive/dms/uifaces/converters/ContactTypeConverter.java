package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.entity.model.referenceBook.ContactInfoType;
import ru.hitsl.sql.dao.interfaces.referencebook.ContactInfoTypeDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 09.09.2014, 16:43 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverter("ContactTypeConverter")
public class ContactTypeConverter implements Converter {

    @Autowired
    @Qualifier("contactInfoTypeDao")
    private ContactInfoTypeDao contactInfoTypeDao;

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        if (value != null && value.trim().length() > 0) {
            final List<ContactInfoType> resultList = contactInfoTypeDao.getByValue(value);
            if (resultList.isEmpty()) {
                return null;
            }
            return resultList.get(0);
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null) {
            return o.toString();
        }
        return null;
    }
}
