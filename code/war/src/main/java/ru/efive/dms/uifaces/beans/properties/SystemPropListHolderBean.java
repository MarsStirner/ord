package ru.efive.dms.uifaces.beans.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.core.io.Resource;

import ru.efive.dms.util.ApplicationContextHelper;
import ru.efive.uifaces.bean.AbstractDocumentHolderBean.State;

/**
 * @author Kuleshov
 * 
 * Редактирование системных настроек( настройки базы данных и т.д.)
 */
@Named("systemProps")
@ConversationScoped
public class SystemPropListHolderBean implements Serializable {

    /** Serial UID */
    private static final long serialVersionUID = 1L;

    @Inject
    private transient Conversation conversation;

    private Map<String, String> managedPropValues;
    
    private Map<String, String> fileValues;

    private static final State EDIT_STATE = new State(false, "EDIT");

    private static final State STATE_VIEW = new State(false, "VIEW");

    public static final String ACTION_RESULT_EDIT = "edit";

    public static final String ACTION_RESULT_VIEW = "view";

    private State state;

    private Map<String, String> properties;

    private void initValuesMapping() {
        managedPropValues = new HashMap<String, String>();
        BasicDataSource datasource = ApplicationContextHelper.getBean("dataSource");
        if (datasource != null) {
            managedPropValues.put("jdbc.url", datasource.getUrl());
            managedPropValues.put("jdbc.username", datasource.getUsername());
            managedPropValues.put("jdbc.password", datasource.getPassword());
        }
    }

    @PostConstruct
    public void init() {
        initValuesMapping();
        refreshProperties();
        setState(STATE_VIEW);
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    public List<String> getKeys() {
        return new ArrayList<String>(getProperties().keySet());
    }

    private void setState(State state) {
        this.state = state;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String edit() {
        setState(EDIT_STATE);
        return ACTION_RESULT_EDIT;
    }

    public String view() {
        setState(STATE_VIEW);
        return ACTION_RESULT_VIEW;
    }

    public String cancel() {
        return view();
    }

    public String save() {
        Properties result = new Properties();
        result.putAll(getPropertiesToSave());
        updateProperties(result);
        return view();
    }

    public boolean isViewState() {
        return STATE_VIEW.equals(state);
    }

    public boolean isEditState() {
        return EDIT_STATE.equals(state);
    }

    public boolean isActual(String name) {
        return managedPropValues.get(name) != null && managedPropValues.get(name).equals(properties.get(name));
    }
    
    private Map<String, String> getPropertiesToSave(){
        for(String key: fileValues.keySet()){
            if(properties.get(key) != null){
                fileValues.put(key, properties.get(key));
            }
        }
        return fileValues;
    }

    private void refreshProperties() {
        File jdbcPropFile = getJDBCPropFile();
        Properties properties = new Properties();
        this.properties = new HashMap<String, String>();
        try {
            properties.load(new FileInputStream(jdbcPropFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileValues = new HashMap<String, String>((Map) properties);
        for (String key : managedPropValues.keySet()) {
            this.properties.put(key, fileValues.get(key));
        }
    }

    private void updateProperties(Properties properties) {
        File jdbcPropFile = getJDBCPropFile();
        if (jdbcPropFile != null) {
            try {
                properties.store(new FileOutputStream(jdbcPropFile), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getJDBCPropFile() {
        BeanDefinition def = ApplicationContextHelper.getBeanDefinition("propertyConfigurer");
        if (def != null) {
            List<TypedStringValue> values = (List<TypedStringValue>) ApplicationContextHelper.getpropertyValue(def,
                    "locations");
            if (values != null && values.size() > 0) {
                String location = (String) values.get(0).getValue();
                Resource resource = ApplicationContextHelper.getContext().getResource(location);
                try {
                    return resource.getFile();
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

}
