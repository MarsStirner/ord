package ru.efive.dms.uifaces.converters;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.efive.crm.data.Contragent;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("ContragentConverter")
public class ContragentConverter implements Converter {

    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Object result = null;
        try {
            SessionManagementBean sessionManagement = (SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}", SessionManagementBean.class);
            Contragent in_contragent = ((ContragentDAOHibernate) sessionManagement.getDAO(ContragentDAOHibernate.class, ApplicationHelper.CONTRAGENT_DAO)).getByFullName(value);
            if (in_contragent != null) {
                result = in_contragent;
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
                System.out.println("Не найден контрагент");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return ((Contragent) value).getFullName();
    }

}