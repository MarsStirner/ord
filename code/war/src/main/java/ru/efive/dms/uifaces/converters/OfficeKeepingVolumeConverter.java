package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.hitsl.sql.dao.OfficeKeepingVolumeDAOImpl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.OFFICE_KEEPING_VOLUME_DAO;

@FacesConverter("OfficeKeepingVolumeConverter")
public class OfficeKeepingVolumeConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            Map<String, Object> in_filters = new HashMap<String, Object>();
            List<OfficeKeepingVolume> list = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, OFFICE_KEEPING_VOLUME_DAO).findDocuments(in_filters, "", false);
            if (list.size() != 0) {
                for (OfficeKeepingVolume in_record : list) {
                    if (in_record != null) {
                        String in_recordCheckString = in_record.getVolumeIndex() + " - " + in_record.getShortDescription();
                        if (in_recordCheckString.equals(value)) {
                            result = in_record;
                            break;
                        }
                    }
                    ;

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

