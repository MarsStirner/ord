package ru.efive.dms.uifaces.converters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.referenceBook.DeliveryType;
import ru.hitsl.sql.dao.interfaces.referencebook.DeliveryTypeDao;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named("DeliveryTypeConverter")
@ApplicationScoped
public class DeliveryTypeConverter extends DictionaryEntityConverter {

    @Autowired
    @Qualifier("deliveryTypeDao")
    private DeliveryTypeDao deliveryTypeDao;

    @PostConstruct
    public void lookupDao() {
        logger.info("DeliveryTypeConverter: initialized. SELF[{}] DAO[{}] ", this, deliveryTypeDao);
    }

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        try {
            final DeliveryType result = deliveryTypeDao.getByCode(value);
            if (result != null) {
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