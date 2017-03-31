package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

@FacesConverter("IncomingDocumentFormConverter")
public class IncomingDocumentFormConverter implements Converter {

    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            List<DocumentForm> list = documentFormDao.findByDocumentTypeCodeAndValue(DocumentType.RB_CODE_INCOMING, value);
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
        if (value == null) {
            return "";
        }
        return ((DocumentForm) value).getValue();
    }

}