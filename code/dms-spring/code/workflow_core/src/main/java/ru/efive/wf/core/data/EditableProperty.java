package ru.efive.wf.core.data;

import ru.efive.wf.core.activity.enums.EditablePropertyScope;

public class EditableProperty {
	
	public EditableProperty() {
		
	}
	
	public EditableProperty(String name, Object value, EditablePropertyScope scope) {
		this.name = name;
		this.value = value;
		this.scope = scope;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public EditablePropertyScope getScope() {
		return scope;
	}
	
	public void setScope(EditablePropertyScope scope) {
		this.scope = scope;
	}
	
	
	private String name;
	private Object value;
	private EditablePropertyScope scope = EditablePropertyScope.GLOBAL;
}