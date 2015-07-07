package ru.efive.dms.uifaces.beans;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;

//TODO remove Startup & Singleton after auto init Spring config
@Singleton(name ="indexManagement")
@Startup

@Named("indexManagement")
@ApplicationScoped
public class IndexManagementBean implements Serializable {

    @PostConstruct
    public void initializeIndex() {
       context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    public ApplicationContext getContext() {
        return context;
    }

    private ApplicationContext context;

    private static final long serialVersionUID = 2489903807452724602L;
}