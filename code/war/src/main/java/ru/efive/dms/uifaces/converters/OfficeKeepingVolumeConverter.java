package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverter;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingVolumeDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import java.util.List;

@FacesConverter("OfficeKeepingVolumeConverter")
public class OfficeKeepingVolumeConverter implements Converter {

    @Autowired
    @Qualifier("officeKeepingVolumeDao")
    private OfficeKeepingVolumeDao officeKeepingVolumeDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            List<OfficeKeepingVolume> list = officeKeepingVolumeDao.getItems();
            if (list.size() != 0) {
                for (OfficeKeepingVolume in_record : list) {
                    if (in_record != null) {
                        String in_recordCheckString = in_record.getVolumeIndex() + " - " + in_record.getShortDescription();
                        if (in_recordCheckString.equals(value)) {
                            result = in_record;
                            break;
                        }
                    }

                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                System.out.println("Не найдена номенклатура дел");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            OfficeKeepingVolume in_record = (OfficeKeepingVolume) value;
            return in_record.getVolumeIndex() + " - " + in_record.getShortDescription();
        } else {
            return "";
        }
    }
}

