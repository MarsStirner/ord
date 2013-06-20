package ru.efive.dms.uifaces.beans;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.springframework.context.support.ClassPathXmlApplicationContext;

@Singleton
@Startup
@Named("indexManagement")
@ApplicationScoped
public class IndexManagementBean implements Serializable {
	
	@PostConstruct
	public void initializeIndex() {
		context = new ClassPathXmlApplicationContext("application-context.xml");
	}
	
	@PreDestroy
	public void dispose() {
		context.close();
	}
	
	public ClassPathXmlApplicationContext getContext() {
		return context;
	}
	
	private ClassPathXmlApplicationContext context;
	
	private static final long serialVersionUID = 2489903807452724602L;
}