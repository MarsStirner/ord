package ru.efive.dms.uifaces.converters;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import ru.efive.dms.dao.SenderTypeDAOImpl;
import ru.efive.dms.data.SenderType;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("SenderTypeConverter")
public class SenderTypeConverter implements Converter {

	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Object result = null;
		try {
			SessionManagementBean sessionManagement = 
				(SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
						SessionManagementBean.class);
			List<SenderType> list = sessionManagement.getDictionaryDAO(SenderTypeDAOImpl.class, ApplicationHelper.SENDER_TYPE_DAO).findByValue(value);
			if (list.size() > 0) {
				result = list.get(0);
			}
			else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
    					FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
				System.out.println("Не найден вид документа");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return ((SenderType) value).getValue();
	}
	
}