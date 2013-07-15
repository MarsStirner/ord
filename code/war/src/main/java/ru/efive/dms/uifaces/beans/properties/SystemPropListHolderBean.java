package ru.efive.dms.uifaces.beans.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.dbcp.BasicDataSource;

import ru.efive.dms.uifaces.beans.IndexManagementBean;

@Named("systemProps")
@SessionScoped
public class SystemPropListHolderBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
    @Named("indexManagement")
    private transient IndexManagementBean indexManagement;
	
	private Map<String, String> properties;
	
	public List<String> getKeys(){
		return new ArrayList<String>(getProperties().keySet());
	}
    
    public Map<String, String> getProperties(){    	
    	
//    	indexManagement.getContext().get
    	
    	BasicDataSource dataSource =  (BasicDataSource)indexManagement.getContext().getBean("dataSource");
    	Map<String, String> properties = new HashMap<String, String>();
    	properties.put("Database username",dataSource.getUsername());
    	properties.put("Database password",dataSource.getPassword());
    	properties.put("Database URL",dataSource.getUrl());
    	properties.put("Database driver class name",dataSource.getDriverClassName());
    	this.properties = properties;
    	return properties;
    }
	
}
