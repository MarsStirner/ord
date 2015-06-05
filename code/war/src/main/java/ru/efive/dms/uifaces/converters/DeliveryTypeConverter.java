package ru.efive.dms.uifaces.converters;

import ru.efive.dms.uifaces.beans.IndexManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DeliveryType;
import ru.hitsl.sql.dao.referenceBook.DeliveryTypeDAOImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.RB_DELIVERY_TYPE_DAO;

@Named("DeliveryTypeConverter")
@ApplicationScoped
public class DeliveryTypeConverter extends DictionaryEntityConverter {

    @Inject
    @Named("indexManagement")
    private IndexManagementBean indexManagement;

    private DeliveryTypeDAOImpl dao;

    @PostConstruct
    public void lookupDao(){
        logger.info("DeliveryTypeConverter: start initialization.");
        this.dao = (DeliveryTypeDAOImpl) indexManagement.getContext().getBean(RB_DELIVERY_TYPE_DAO);
        logger.info("DeliveryTypeConverter: initialized. SELF[{}] DAO[{}] ", this, dao);
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            final DeliveryType result = dao.getByCode(value);
            if(result != null){
                return result;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                logger.error("DeliveryTypeConverter: No item with code=\'{}\'", value);
                return null;
            }
        } catch (Exception e) {
            logger.error("DeliveryTypeConverter : unknown error", e);
            return null;
        }
    }

}