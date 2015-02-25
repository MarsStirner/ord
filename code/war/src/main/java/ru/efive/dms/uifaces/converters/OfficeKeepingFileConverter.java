package ru.efive.dms.uifaces.converters;

import ru.efive.dms.dao.OfficeKeepingFileDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.document.OfficeKeepingFile;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.OFFICE_KEEPING_FILE_DAO;


@FacesConverter("OfficeKeepingFileConverter")
public class OfficeKeepingFileConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<OfficeKeepingFile> list = sessionManagement.getDAO(OfficeKeepingFileDAOImpl.class, OFFICE_KEEPING_FILE_DAO).findDocuments(false);
            if (list.size() != 0) {
                for (OfficeKeepingFile in_record : list) {
                    if (in_record != null) {
                        String in_recordCheckString = in_record.getFileIndex() + " - " + in_record.getShortDescription();
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
            OfficeKeepingFile in_record = (OfficeKeepingFile) value;
            return in_record.getFileIndex() + " - " + in_record.getShortDescription();
        } else {
            return "";
        }
    }
}

