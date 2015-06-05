package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.referenceBook.DocumentFormDAOImpl;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.List;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.DOCUMENT_FORM_DAO;

@FacesConverter("DocumentFormConverter")
public class DocumentFormConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger("CONVERTER");

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context,
                    "#{sessionManagement}", SessionManagementBean.class);
            List<DocumentForm> list = sessionManagement.getDictionaryDAO(DocumentFormDAOImpl.class,
                    DOCUMENT_FORM_DAO).getByValue(value);
            if (!list.isEmpty()) {
                return list.get(0);
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                LOGGER.error("DOCUMENT_FORM: Не найден вид документа \'{}\'", value);
            }
        } catch (Exception e) {
            LOGGER.error("DOCUMENT_FORM", e);
        }

        return null;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((DocumentForm) value).getValue();
    }

}