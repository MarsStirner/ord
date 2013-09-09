package ru.efive.dms.uifaces.beans;


import org.hibernate.SQLQuery;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate3.HibernateTemplate;
import ru.efive.dms.dao.VersionDAOImpl;
import ru.efive.dms.data.Version;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.entity.AbstractEntity;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;


@Named("versionHolderBean")
@SessionScoped
public class VersionHolderBean implements Serializable {

    private String version;
    private String buildDate;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void updateVersionInfo() {
        sessionManagement.registrateBeanName("versionHolderBean");
        VersionDAOImpl versionDao = sessionManagement.getDAO(VersionDAOImpl.class, ApplicationHelper.VERSION_DAO);
        List<Version> versionList = versionDao.findDocuments();
        if(versionList.size() > 0) {
            setVersion(String.valueOf(versionList.get(versionList.size() - 1).getVersion()));
        }
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/build.properties"));
            setBuildDate(properties.getProperty("application.build.date"));
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
