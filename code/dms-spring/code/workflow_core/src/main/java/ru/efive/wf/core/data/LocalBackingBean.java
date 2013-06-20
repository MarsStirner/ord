package ru.efive.wf.core.data;

import java.util.List;

import ru.efive.wf.core.ActionResult;

public interface LocalBackingBean {
	
	public String getForm();
	public List<EditableProperty> getProperties();
	
	public ActionResult initialize();
}