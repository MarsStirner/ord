package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationDAONames;
import ru.efive.sql.dao.RbContragentTypeDAOImpl;
import ru.entity.model.crm.ContragentType;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 11.02.2015, 4:42 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverter("ContragentTypeConverter")
public class ContragentTypeConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        if (value != null && value.trim().length() > 0) {
            SessionManagementBean sessionManagement = fc.getApplication().evaluateExpressionGet(fc, "#{sessionManagement}", SessionManagementBean.class);
            RbContragentTypeDAOImpl service = sessionManagement.getDAO(RbContragentTypeDAOImpl.class, ApplicationDAONames.RB_CONTRAGENT_TYPE_DAO);
            final List<ContragentType> resultList = service.findByValue(value);
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
        if (o != null && o instanceof ContragentType) {
            return ((ContragentType)o).getValue();
        }
        return null;
    }
}
