package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentTypeDao;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("DocumentTypeConverter")
@ApplicationScoped
public class DocumentTypeConverter extends DictionaryEntityConverter {

    private DocumentTypeDao dao;

    @PostConstruct
    public void lookupDao() {
        logger.info("DocumentTypeConverter: initialized. SELF[{}] DAO[{}] ", this, dao);
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            final DocumentType result = dao.getByCode(value);
            if (result != null) {
                return result;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                logger.error("DocumentTypeConverter: No item with code=\'{}\'", value);
                return null;
            }
        } catch (Exception e) {
            logger.error("DocumentTypeConverter : unknown error", e);
            return null;
        }
    }
}