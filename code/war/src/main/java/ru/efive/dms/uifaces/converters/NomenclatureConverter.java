package ru.efive.dms.uifaces.converters;

import ru.efive.dms.dao.NomenclatureDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.document.Nomenclature;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.RB_NOMENCLATURE_DAO;

@FacesConverter("NomenclatureConverter")
public class NomenclatureConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<Nomenclature> list = sessionManagement.getDictionaryDAO(NomenclatureDAOImpl.class, RB_NOMENCLATURE_DAO).findDocuments();
            if (list != null) {
                for (Nomenclature e : list) {
                    if (e.getCode().equals(value)) {
                        return e;
                    }
                    ;
                }

            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                System.out.println("Не найдена номенклатура для документа");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return value != null ? ((Nomenclature) value).getCode() : "";
    }

}