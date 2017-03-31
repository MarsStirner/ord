package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.Region;
import ru.hitsl.sql.dao.interfaces.referencebook.RegionDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

@FacesConverter("RegionConverter")
public class RegionConverter implements Converter {

    @Autowired
    @Qualifier("regionDao")
    private RegionDao regionDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {

            List<Region> list = regionDao.getByValue(value);
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