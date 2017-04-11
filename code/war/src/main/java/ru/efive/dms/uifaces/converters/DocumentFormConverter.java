package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;

@FacesConverterWithSpringSupport("DocumentFormConverter")
public class DocumentFormConverter extends AbstractReferenceBookConverter<DocumentForm> {

    @Autowired
    public DocumentFormConverter(@Qualifier("documentFormDao") final DocumentFormDao documentFormDao) {
        super(documentFormDao);
    }
}