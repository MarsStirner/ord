package ru.efive.dao.alfresco;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.chemistry.opencmis.commons.SessionParameter.*;

/**
 * Created by EUpatov on 18.04.2017.
 */
public class AlfrescoImpl {
    private static final Logger log = LoggerFactory.getLogger(AlfrescoImpl.class);

    private String url;
    private String login;
    private String password;
    private String rootFolder;
    private Session session;

    public Session createSession(){
        if(session == null) {
            init();
        }
        return session;
    }

    public boolean init(){
        final Map<String, String> parameters = new HashMap<>();
        parameters.put(BINDING_TYPE, BindingType.ATOMPUB.value());
        parameters.put(ATOMPUB_URL, getUrl());
        parameters.put(USER, getLogin());
        parameters.put(PASSWORD, getPassword());
        parameters.put(AUTH_HTTP_BASIC, "true");
        parameters.put(COOKIES, "true");
        parameters.put(LOCALE_ISO639_LANGUAGE, "ru");
        parameters.put(AUTH_SOAP_USERNAMETOKEN, "true");
        parameters.put(CLIENT_COMPRESSION, "false");
        parameters.put(COMPRESSION, "true");
        parameters.put(CONNECT_TIMEOUT, "5000");
        parameters.put(READ_TIMEOUT, "60000");

        final SessionFactoryImpl sessionFactory = SessionFactoryImpl.newInstance();
        final List<Repository> repositories = sessionFactory.getRepositories(parameters);
        for (Iterator<Repository> iterator = repositories.iterator(); iterator.hasNext(); ) {
            Repository repository = iterator.next();
            log.info("SessionFactory initialized. One of available Repository: {} '{}'", repository.getId(), repository.getDescription());
            this.session = repository.createSession();
            Folder parentFolder = getParentFolder(session);
            if(parentFolder == null){
                createParentFolder(session);
            }
            return true;
        }
        return false;
    }

    private void createParentFolder(Session session) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.NAME, getRootFolder());
        session.getRootFolder().createFolder(properties);
    }

    public Folder getParentFolder(Session cmisSession) {
        return (Folder) cmisSession.getObjectByPath(getRootFolder());
    }


    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    public String getRootFolder() {
        return rootFolder;
    }

    public boolean isConnected() {
        return session != null;
    }
}
