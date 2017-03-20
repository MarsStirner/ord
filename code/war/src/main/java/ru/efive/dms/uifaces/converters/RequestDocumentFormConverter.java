package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.referenceBook.DocumentFormDAOImpl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.DOCUMENT_FORM_DAO;

@FacesConverter("RequestDocumentFormConverter")
public class RequestDocumentFormConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement =
                    context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
                            SessionManagementBean.class);
            List<DocumentForm> list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class, DOCUMENT_FORM_DAO).findByDocumentTypeCodeAndValue(
                    DocumentType.RB_CODE_REQUEST, value);
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
        if(value == null){
            return "";
        }
        return ((DocumentForm) value).getValue();
    }

}