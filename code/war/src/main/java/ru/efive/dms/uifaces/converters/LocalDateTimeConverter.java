package ru.efive.dms.uifaces.converters;

import org.apache.commons.lang3.StringUtils;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by EUpatov on 04.04.2017.
 */
@FacesConverter("LocalDateTimeConverter")
public class LocalDateTimeConverter implements Converter {
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (StringUtils.isNotEmpty(value)){
            try {
                return LocalDateTime.parse(value, getFormatter(component));
            } catch (Exception e){
                return LocalDate.parse(value, getFormatter(component)).atStartOfDay();
            }
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            final LocalDateTime dateValue = (LocalDateTime) value;
            return dateValue.format(getFormatter(component));
        } else {
            return null;
        }
    }

    private static DateTimeFormatter getFormatter(UIComponent component){
        Object pattern = component.getAttributes().get("pattern");
        if(pattern != null){
            return DateTimeFormatter.ofPattern((String) pattern);
        } else {
            return DateTimeFormatter.ISO_DATE_TIME;
        }
    }
}
