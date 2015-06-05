package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DocumentType;
import ru.hitsl.sql.dao.referenceBook.DocumentTypeDAOImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.RB_DOCUMENT_TYPE_DAO;

@Named("DocumentTypeConverter")
@ApplicationScoped
public class DocumentTypeConverter extends DictionaryEntityConverter {

    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    private DocumentTypeDAOImpl dao;

    @PostConstruct
    public void lookupDao(){
        logger.info("DocumentTypeConverter: start initialization.");
        this.dao = (DocumentTypeDAOImpl) indexManagement.getContext().getBean(RB_DOCUMENT_TYPE_DAO);
        logger.info("DocumentTypeConverter: initialized. SELF[{}] DAO[{}] ", this, dao);
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            final DocumentType result = dao.getByCode(value);
            if(result != null){
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