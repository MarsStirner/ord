package ru.efive.dms.uifaces.converters;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 13:29 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@FacesConverter("SubstitutionTypeConverter")
public class SubstitutionTypeConverter implements Converter {
    private static final String helper = "Помощник"; //1
    private static final String io = "Исполняющий обязанности"; //2

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
        if (helper.equalsIgnoreCase(s)) {
            return 1;
        } else if (io.equalsIgnoreCase(s)) {
            return 2;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        if (o != null) {
            try{
                final Integer item = (Integer) o;
                switch (item){
                    case 1:{
                        return helper;
                    }
                    case 2: {
                        return io;
                    }
                    default:{
                        return "";
                    }
                }
            } catch (NumberFormatException e){
                return "";
            }
        }
        return "";
    }
}
