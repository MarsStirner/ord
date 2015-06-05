package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.Nomenclature;
import ru.hitsl.sql.dao.referenceBook.NomenclatureDAOImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.RB_NOMENCLATURE_DAO;

@Named("NomenclatureConverter")
@ApplicationScoped
public class NomenclatureConverter extends DictionaryEntityConverter {

    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    private NomenclatureDAOImpl dao;

    @PostConstruct
    public void lookupDao(){
        logger.info("NomenclatureConverter: start initialization.");
        this.dao = (NomenclatureDAOImpl) indexManagement.getContext().getBean(RB_NOMENCLATURE_DAO);
        logger.info("NomenclatureConverter: initialized. SELF[{}] DAO[{}] ", this, dao);
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            final Nomenclature result = dao.getByCode(value);
            if(result != null){
                return result;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                logger.error("NomenclatureConverter: No item with code=\'{}\'", value);
                return null;
            }
        } catch (Exception e) {
            logger.error("NomenclatureConverter : unknown error", e);
            return null;
        }
    }
}