package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.Region;
import ru.hitsl.sql.dao.interfaces.referencebook.RegionDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;


@FacesConverterWithSpringSupport("RegionConverter")
public class RegionConverter extends AbstractReferenceBookConverter<Region> {

    @Autowired
    public RegionConverter(@Qualifier("regionDao") final RegionDao regionDao) {
        super(regionDao);
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