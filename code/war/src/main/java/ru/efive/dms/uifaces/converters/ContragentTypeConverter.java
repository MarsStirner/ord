package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.referenceBook.ContragentTypeDAOImpl;
import ru.hitsl.sql.dao.util.ApplicationDAONames;

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
            ContragentTypeDAOImpl service = sessionManagement.getDAO(ContragentTypeDAOImpl.class, ApplicationDAONames.RB_CONTRAGENT_TYPE_DAO);
            final List<ContragentType> resultList = service.getByValue(value);
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
