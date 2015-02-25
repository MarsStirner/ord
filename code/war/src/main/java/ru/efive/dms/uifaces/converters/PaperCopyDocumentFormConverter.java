package ru.efive.dms.uifaces.converters;

import ru.efive.dms.dao.DocumentFormDAOImpl;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.document.DocumentForm;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.DOCUMENT_FORM_DAO;

@FacesConverter("PaperCopyDocumentFormConverter")
public class PaperCopyDocumentFormConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<DocumentForm> list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByCategoryAndValue("Бумажные экземпляры", value);
            if (list.size() > 0) {
                result = list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                System.out.println("Не найден вид документа");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((DocumentForm) value).getValue();
    }

}