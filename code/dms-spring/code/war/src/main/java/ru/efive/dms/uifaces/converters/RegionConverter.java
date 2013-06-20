package ru.efive.dms.uifaces.converters;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import ru.efive.dms.dao.DeliveryTypeDAOImpl;
import ru.efive.dms.dao.RegionDAOImpl;
import ru.efive.dms.data.DeliveryType;
import ru.efive.dms.data.Region;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("RegionConverter")
public class RegionConverter implements Converter {

	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Object result = null;
		try {
			SessionManagementBean sessionManagement = 
				(SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
						SessionManagementBean.class);
			List<Region> list = sessionManagement.getDictionaryDAO(RegionDAOImpl.class, ApplicationHelper.REGION_DAO).findByValue(value);
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
		Region in_region=(Region) value;
		if(in_region!=null){
			String in_category=in_region.getCategory().toLowerCase();
			if(in_category.equals("республика")||in_category.equals("город")){
				return in_category+" "+in_region.getValue();
			}else{
				return in_region.getValue()+" "+in_category;
			}
		}else{
			return "";
		}		
	}
	
}