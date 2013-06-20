package ru.efive.wf.core;

import java.util.ResourceBundle;

public final class Settings {
	
	static {
		rb = ResourceBundle.getBundle("workflow");
	}
	
	public static String getMessagingService() {
		return rb.getString("MessagingServiceJNDI");
	}
	
	public static String getWorkFlowService() {
		return rb.getString("WorkFlowJNDI");
	}
	
	
	private static ResourceBundle rb;
}