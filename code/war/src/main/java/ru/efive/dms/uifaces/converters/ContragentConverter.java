package ru.efive.dms.uifaces.converters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.utils.MessageHolder;
import ru.entity.model.crm.Contragent;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import static ru.efive.dms.util.ApplicationDAONames.CONTRAGENT_DAO;

@FacesConverter("ContragentConverter")
public class ContragentConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger("CONVERTER");

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement = context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
            Contragent in_contragent = sessionManagement.getDAO(ContragentDAOHibernate.class, CONTRAGENT_DAO).getByFullName(value);
            if (in_contragent != null) {
                result = in_contragent;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, MessageHolder.MSG_CONVERTER_ERROR);
                LOGGER.error("CONTRAGENT: Не найден контрагент \'{}\'", value);
            }
        } catch (Exception e) {
            LOGGER.error("CONTRAGENT", e);
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Contragent) value).getFullName();
    }

}