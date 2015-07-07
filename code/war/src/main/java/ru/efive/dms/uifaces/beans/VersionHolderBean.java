package ru.efive.dms.uifaces.beans;


import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.net.URLClassLoader;
import java.util.Properties;


@Named("versionHolderBean")
@ApplicationScoped
public class VersionHolderBean implements Serializable {

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

    public String getClasspathInfo() {
        return StringUtils.join((((URLClassLoader) Thread.currentThread().getContextClassLoader())).getURLs(), "\n");
    }
}
