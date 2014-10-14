package ru.efive.dms.uifaces.converters;

import ru.efive.dms.dao.OfficeKeepingRecordDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.document.OfficeKeepingRecord;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.OFFICE_KEEPING_RECORD_DAO;

@FacesConverter("OfficeKeepingRecordConverter")
public class OfficeKeepingRecordConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<OfficeKeepingRecord> list = sessionManagement.getDAO(OfficeKeepingRecordDAOImpl.class, OFFICE_KEEPING_RECORD_DAO).findDocuments(false);
            if (list.size() != 0) {
                for (OfficeKeepingRecord in_record : list) {
                    if (in_record != null) {
                        String in_recordCheckString = in_record.getRecordIndex() + " - " + in_record.getShortDescription();
                        if (in_recordCheckString.equals(value)) {
                            result = in_record;
                            break;
                        }
                    }
                    ;

                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
                System.out.println("Не найдена номенклатура дел");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            OfficeKeepingRecord in_record = (OfficeKeepingRecord) value;
            return in_record.getRecordIndex() + " - " + in_record.getShortDescription();
        } else {
            return "";
        }
    }
}

