package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.Nomenclature;
import ru.hitsl.sql.dao.interfaces.referencebook.NomenclatureDao;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("NomenclatureConverter")
@ApplicationScoped
public class NomenclatureConverter extends DictionaryEntityConverter {


    private NomenclatureDao dao;

    @PostConstruct
    public void lookupDao() {
        logger.info("NomenclatureConverter: initialized. SELF[{}] DAO[{}] ", this, dao);
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            final Nomenclature result = dao.getByCode(value);
            if (result != null) {
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