package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.RbContactTypeDAO;
import ru.efive.sql.entity.user.RbContactInfoType;

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
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        if(value != null && value.trim().length() > 0) {
            SessionManagementBean sessionManagement = fc.getApplication().evaluateExpressionGet(fc, "#{sessionManagement}", SessionManagementBean.class);
           RbContactTypeDAO service = sessionManagement.getDAO(RbContactTypeDAO.class, ApplicationHelper.RB_CONTACT_TYPE_DAO);
            final List<RbContactInfoType> resultList = service.findByValue(value);
            if(resultList.isEmpty()){
                return null;
            }
            return resultList.get(0);
        }
        else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if(o != null) {
            return o.toString();
        }
        return null;
    }
}
