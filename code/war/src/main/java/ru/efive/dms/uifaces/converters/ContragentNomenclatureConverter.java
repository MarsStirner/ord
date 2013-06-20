package ru.efive.dms.uifaces.converters;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import ru.efive.crm.dao.NomenclatureDAOImpl;
import ru.efive.crm.data.ContragentNomenclature;
import ru.efive.dms.uifaces.beans.SessionManagementBean;

@FacesConverter("ContragentNomenclatureConverter")
public class ContragentNomenclatureConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<ContragentNomenclature> list = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, "contragentNomenclatureDao").findDocuments();
            if (list != null) {
                for (ContragentNomenclature e : list) {
                    if (e.getDescription().equals(value)) {
                        return e;
                    }
                    ;
                }

            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
                System.out.println("Не найдена номенклатура для документа");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((ContragentNomenclature) value).getDescription();
    }

}