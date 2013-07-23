package ru.efive.dms.util;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Utility class for support application context handling.
 * 
 * @author Kuleshov
 * 
 */
public class ApplicationContextHelper {

	/**
	 * Application context.Should be setted once during application lifecycle. 
	 */
	private static ClassPathXmlApplicationContext context;

	/**
	 * Gets application contex.
	 * 
	 * @return application context.
	 */
	public static ClassPathXmlApplicationContext getContext() {
		return context;
	}

	/**
	 * Sets application context.
	 * 
	 * @param context application context.
	 */
	public static void setContext(ClassPathXmlApplicationContext context) {
		ApplicationContextHelper.context = context;
	}

	/**
	 * Gets bean definition from application context.
	 * 
	 * @param qName bean name.
	 * @return bean definition object.
	 */
	public static BeanDefinition getBeanDefinition(String qName) {
		return context.getBeanFactory().getBeanDefinition(qName);
	}

	/**
	 * Returns property value object from bean defined in application context. 
	 * 
	 * @param def bean definition object.
	 * @param propname name of the property.
	 * @return property value object.
	 */
	public static Object getpropertyValue(BeanDefinition def,
			String propname) {
		return def.getPropertyValues().getPropertyValue(propname).getValue();
	}
	
	/**
	 * @param qName
	 * @return
	 */
	public static <T> T getBean(String qName){
		return (T)context.getBean(qName);
	}

}
