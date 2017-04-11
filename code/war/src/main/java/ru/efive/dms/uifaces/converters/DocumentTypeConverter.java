package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import ru.efive.dms.uifaces.beans.annotations.FacesConverterWithSpringSupport;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentTypeDao;

@FacesConverterWithSpringSupport("DocumentTypeConverter")
public class DocumentTypeConverter extends AbstractReferenceBookConverter<DocumentType> {

    @Autowired
    public DocumentTypeConverter(DocumentTypeDao documentTypeDao) {
        super(documentTypeDao);
    }
}