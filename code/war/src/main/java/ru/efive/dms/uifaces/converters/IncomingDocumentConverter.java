package ru.efive.dms.uifaces.converters;

import ru.entity.model.document.IncomingDocument;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("IncomingDocumentConverter")
public class IncomingDocumentConverter implements Converter {

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        IncomingDocument in_doc = ((IncomingDocument) value);
        return "Входящий документ № " + in_doc.getRegistrationNumber();
    }

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        return null;
    }
}