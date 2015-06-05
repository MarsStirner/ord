package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.referenceBook.ContactInfoType;
import ru.hitsl.sql.dao.referenceBook.ContactInfoTypeDAOImpl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.RB_CONTACT_TYPE_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 09.09.2014, 16:43 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverter("ContactTypeConverter")
public class ContactTypeConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        if (value != null && value.trim().length() > 0) {
            SessionManagementBean sessionManagement = fc.getApplication().evaluateExpressionGet(fc, "#{sessionManagement}", SessionManagementBean.class);
            ContactInfoTypeDAOImpl service = sessionManagement.getDAO(ContactInfoTypeDAOImpl.class, RB_CONTACT_TYPE_DAO);
            final List<ContactInfoType> resultList = service.getByValue(value);
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
