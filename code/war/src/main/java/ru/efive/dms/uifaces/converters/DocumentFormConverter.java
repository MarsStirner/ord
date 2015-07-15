package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentForm;
import ru.hitsl.sql.dao.referenceBook.DocumentFormDAOImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.DOCUMENT_FORM_DAO;

@Named("DocumentFormConverter")
@ApplicationScoped
public class DocumentFormConverter extends DictionaryEntityConverter {
    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    private DocumentFormDAOImpl dao;

    @PostConstruct
    public void lookupDao(){
        logger.info("DocumentFormConverter: start initialization.");
        this.dao = (DocumentFormDAOImpl) indexManagement.getContext().getBean(DOCUMENT_FORM_DAO);
        logger.info("DocumentFormConverter: initialized. SELF[{}] DAO[{}] ", this, dao);
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            final DocumentForm result = dao.getByCode(value);
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