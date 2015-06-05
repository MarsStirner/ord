package ru.efive.dms.uifaces.converters;

import ru.entity.model.mapped.DictionaryEntity;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 * Author: Upatov Egor <br>
 * Date: 03.06.2015, 20:11 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class DictionaryEntityConverter extends CustomConverter {

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if(value == null){
            logger.error("Convert null value is not possible. Return empty string");
            return "";
        }
        try {
            return ((DictionaryEntity) value).getCode();
        } catch (ClassCastException e){
            logger.error("Convert non-DictionaryEntity value is not possible. Return empty string. Object={}", value);
            return "";
        }
    }

}
