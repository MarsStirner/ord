package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.interfaces.referencebook.DocumentFormDao;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("DocumentFormConverter")
@ApplicationScoped
public class DocumentFormConverter extends DictionaryEntityConverter {

    @Autowired
    @Qualifier("documentFormDao")
    private DocumentFormDao documentFormDao;

    @PostConstruct
    public void init() {
        logger.info("DocumentFormConverter: initialized. SELF[{}] DAO[{}] ", this, documentFormDao);
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            final DocumentForm result = documentFormDao.getByCode(value);
            if (result == null) {
                logger.error("DocumentFormConverter: Not found by code[{}]", value);
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
            }
            return result;
        } catch (Exception e) {
            logger.error("DocumentFormConverter: exception", e);
            FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
        }
        return null;
    }

}