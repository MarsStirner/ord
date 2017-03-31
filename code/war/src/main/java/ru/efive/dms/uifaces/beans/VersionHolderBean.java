package ru.efive.dms.uifaces.beans;


import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.net.URLClassLoader;
import java.util.Properties;


@Controller("versionHolderBean")
@ApplicationScoped
public class VersionHolderBean implements Serializable {

    private static final String BUILD_PROP_FILE = "/build.properties";
    private static final String VERSION = "application.version";
    private static final String BUILDTIME = "application.build.date";


    private String version;
    private String buildDate;


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
