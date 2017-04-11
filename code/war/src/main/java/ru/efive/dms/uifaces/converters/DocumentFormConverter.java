package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverter;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.interfaces.mapped.ReferenceBookDao;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@FacesConverter("DocumentFormConverter")
public class DocumentFormConverter extends AbstractReferenceBookConverter<DocumentForm> {

    @Autowired
    public DocumentFormConverter(@Qualifier("documentFormDao") final DocumentFormDao documentFormDao) {
        super(documentFormDao);
    }
}