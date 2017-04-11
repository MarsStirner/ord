package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import ru.efive.dms.uifaces.beans.annotations.FacesConverter;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentTypeDao;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@FacesConverter("DocumentTypeConverter")
public class DocumentTypeConverter extends AbstractReferenceBookConverter<DocumentType> {

    @Autowired
    public DocumentTypeConverter(DocumentTypeDao documentTypeDao) {
        super(documentTypeDao);
    }
}