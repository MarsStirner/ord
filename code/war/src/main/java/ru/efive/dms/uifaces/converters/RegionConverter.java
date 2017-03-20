package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.Region;
import ru.hitsl.sql.dao.referenceBook.RegionDAOImpl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.REGION_DAO;

@FacesConverter("RegionConverter")
public class RegionConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<Region> list = sessionManagement.getDictionaryDAO(RegionDAOImpl.class, REGION_DAO).getByValue(value);
            if (list.size() > 0) {
                result = list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                System.out.println("Не найден вид документа");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Region in_region = (Region) value;
        if (in_region != null) {
            String in_category = in_region.getCategory().toLowerCase();
            if (in_category.equals("республика") || in_category.equals("город")) {
                return in_category + " " + in_region.getValue();
            } else {
                return in_region.getValue() + " " + in_category;
            }
        } else {
            return "";
        }
    }

}