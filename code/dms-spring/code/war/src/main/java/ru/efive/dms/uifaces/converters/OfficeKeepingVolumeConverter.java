package ru.efive.dms.uifaces.converters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import ru.efive.dms.dao.OfficeKeepingVolumeDAOImpl;
import ru.efive.dms.data.OfficeKeepingVolume;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;

@FacesConverter("OfficeKeepingVolumeConverter")
public class OfficeKeepingVolumeConverter implements Converter {

	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Object result = null;
		try {
			SessionManagementBean sessionManagement = 
				(SessionManagementBean) context.getApplication().evaluateExpressionGet(context, "#{sessionManagement}",
						SessionManagementBean.class);
			Map<String,Object> in_filters=new HashMap<String, Object>();
			List<OfficeKeepingVolume> list = sessionManagement.getDAO(OfficeKeepingVolumeDAOImpl.class, ApplicationHelper.OFFICE_KEEPING_VOLUME_DAO).findDocuments(in_filters,"",false);
			if(list.size()!=0){
				for (OfficeKeepingVolume in_record : list)
				{				
					if(in_record!=null){
						String in_recordCheckString=in_record.getVolumeIndex()+" - "+in_record.getShortDescription();
						if(in_recordCheckString.equals(value)){
							result=in_record;
							break;
						}
					};

				}
			}else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
						FacesMessage.SEVERITY_ERROR, "Внутренняя ошибка.", ""));
				System.out.println("Не найдена номенклатура дел");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getAsString(FacesContext context, UIComponent component, Object value) {	
		if(value!=null){
			OfficeKeepingVolume in_record=(OfficeKeepingVolume)value;
			return in_record.getVolumeIndex()+" - "+in_record.getShortDescription();
		}else{
			return "";
		}
	}
}

