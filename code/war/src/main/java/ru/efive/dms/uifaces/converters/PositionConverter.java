package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.referenceBook.PositionDAOImpl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.POSITION_DAO;

/**
 * Author: Upatov Egor <br>
 * Date: 08.09.2014, 17:45 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverter("positionConverter")
public class PositionConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        if (value != null && value.trim().length() > 0) {
            SessionManagementBean sessionManagement = fc.getApplication().evaluateExpressionGet(fc, "#{sessionManagement}", SessionManagementBean.class);
            PositionDAOImpl service = sessionManagement.getDAO(PositionDAOImpl.class, POSITION_DAO);
            final List<Position> resultList = service.getByValue(value);
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
