package ru.efive.dms.uifaces.converters;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentTypeDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

/**
 * Author: Upatov Egor <br>
 * Date: 11.02.2015, 4:42 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Named("ContragentTypeConverter")
public class ContragentTypeConverter implements Converter {
    @Autowired
    @Qualifier("contragentTypeDao")
    private ContragentTypeDao contragentTypeDao;

    @Override
    public Object getAsObject(FacesContext fc, UIComponent uiComponent, String value) {
        return StringUtils.isNotEmpty(value) ? contragentTypeDao.getByValue(value).stream().findFirst().orElse(null) : null;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
        return o != null && o instanceof ContragentType ? ((ContragentType) o).getValue() : null;
    }
}
