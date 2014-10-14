package ru.efive.dms.uifaces.converters;

import ru.efive.dms.dao.NomenclatureDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.entity.model.document.Nomenclature;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.NOMENCLATURE_DAO;

@FacesConverter("NomenclatureConverter")
public class NomenclatureConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<Nomenclature> list = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, NOMENCLATURE_DAO).findDocuments();
            if (list != null) {
                for (Nomenclature e : list) {
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
        return ((Nomenclature) value).getDescription();
    }

}