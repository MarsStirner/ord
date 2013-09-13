package ru.efive.dms.uifaces.beans;


import java.io.Serializable;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;


@Named("versionHolderBean")
@SessionScoped
public class VersionHolderBean implements Serializable {

    /** Serial UID */
    private static final long serialVersionUID = 1L;
    
    private static final String BUILD_PROP_FILE = "/build.properties";
    private static final String VERSION = "application.version";
    private static final String BUILDTIME = "application.build.date";
    
    
    private String version;
    private String buildDate;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void updateVersionInfo() {
        sessionManagement.registrateBeanName("versionHolderBean");
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream(BUILD_PROP_FILE));
            setVersion(properties.getProperty(VERSION));
            setBuildDate(properties.getProperty(BUILDTIME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = buildDate;
    }

}
