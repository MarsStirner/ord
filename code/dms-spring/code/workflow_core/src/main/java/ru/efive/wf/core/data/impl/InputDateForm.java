package ru.efive.wf.core.data.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.efive.wf.core.ActionResult;
import ru.efive.wf.core.activity.enums.EditablePropertyScope;
import ru.efive.wf.core.data.EditableProperty;
import ru.efive.wf.core.data.LocalBackingBean;
import ru.efive.wf.core.util.EngineHelper;

public class InputDateForm implements LocalBackingBean {	

	@Override
	public String getForm() {
		return "<?xml version='1.0' encoding='UTF-8' ?>\n"
		+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
		+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:h=\"http://java.sun.com/jsf/html\" "
		+ "xmlns:f=\"http://java.sun.com/jsf/core\" " 
		+ "xmlns:e5ui=\"http://efive.ru/uitemplates\" " 
		+ "xmlns:p=\"http://primefaces.org/ui\" "
		+ "xmlns:e5ui-comp=\"http://efive.ru/uitemplates/composite\">\n"   
		+ "<h:outputScript>"
		+ "PrimeFaces.locales ['ru'] = {"
		+ "closeText: 'Закрыть',"
		+ "prevText: 'Назад',"
		+ "nextText: 'Вперёд',"
		+ "monthNames: ['Январь', 'Февраль' , 'Март' , 'Апрель' , 'Май' , 'Июнь' , 'Июль' , 'Август' , 'Сентябрь','Октябрь','Ноябрь','Декабрь' ],"
		+ "monthNamesShort: ['Янв', 'Фев', 'Мар', 'Апр', 'Май', 'Июн', 'Июл', 'Авг', 'Сен', 'Окт', 'Ноя', 'Дек' ],"
		+ "dayNames: ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Субота'],"
		+ "dayNamesShort: ['Воск','Пон' , 'Вт' , 'Ср' , 'Четв' , 'Пят' , 'Суб'],"
		+ "dayNamesMin: ['В', 'П', 'Вт', 'С ', 'Ч', 'П ', 'Сб'],"
		+ "weekHeader: 'Неделя',"
		+ "firstDay: 1,"
		+ "isRTL: false,"
		+ "showMonthAfterYear: false,"
		+ "yearSuffix:'',"
		+ "timeOnlyTitle: 'Только время',"
		+ "timeText: 'Время',"
		+ "hourText: 'Час',"
		+ "minuteText: 'Минута',"
		+ "secondText: 'Секунда',"
		+ "currentText: 'Сегодня',"
		+ "ampm: false,"
		+ "month: 'Месяц',"
		+ "week: 'неделя',"
		+ "day: 'День',"
		+ "allDayText: 'Весь день'"
		+ "};"
		+ "</h:outputScript>"
		+ "<div id=\"title\">Дата</div>\n"
		+ "<div style=\"margin-top:0;\">\n"
		+ " <span class=\"title\">Дата:</span>\n"
		+ " <p:calendar value=\"#{" + getBeanName() 
		+ ".processorModal.processedActivity.document.actionDate}\" locale=\"ru\" immediate=\"true\" showOn=\"button\"/>"       
		+ "</div>\n"
		+ "</html>";
	}


  

	public void setActionDate(Date actionDate) {		
		this.actionDate = actionDate;
	}

	public Date getActionDate() {
		return actionDate;
	}

	public void setScope(EditablePropertyScope scope) {
		this.scope = scope;
	}

	public EditablePropertyScope getScope() {
		return scope;
	}

	public void setActionDateField(String actionDateField) {
		this.actionDateField = actionDateField;
	}

	public String getActionDateField() {
		return actionDateField;
	}

	@Override
	public List<EditableProperty> getProperties() {
		return properties == null? new ArrayList<EditableProperty>(): properties;
	}

	@Override
	public ActionResult initialize() {
		ActionResult result = new ActionResult();
		try {
			if (actionDate == null) {				
				result.setProcessed(false);
				result.setDescription("Не указана дата");
			}
			else {				
				properties = new ArrayList<EditableProperty>();
				properties.add(new EditableProperty(getActionDateField(), getActionDate(), getScope()));				
				properties.add(new EditableProperty(getActionCommentaryField(), getActionCommentary(), getScope()));				
				result.setProcessed(true);
			}
		}
		catch (Exception e) {
			result.setProcessed(false);
			result.setDescription("Exception in initialize");
			e.printStackTrace();
		}
		return result;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanName() {
		return beanName;
	}	


	public void setActionCommentary(String actionCommentary) {
		this.actionCommentary = actionCommentary;
	}




	public String getActionCommentary() {
		return actionCommentary;
	}


	public void setActionCommentaryField(String actionCommentaryField) {
		this.actionCommentaryField = actionCommentaryField;
	}




	public String getActionCommentaryField() {
		return actionCommentaryField;
	}


	private List<EditableProperty> properties;

	private String beanName;
	
	private Date actionDate;
	private String actionCommentary;
	private String actionCommentaryField;
	private EditablePropertyScope scope;
	private String actionDateField;
}