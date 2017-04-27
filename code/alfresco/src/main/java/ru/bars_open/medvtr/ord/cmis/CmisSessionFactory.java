package ru.bars_open.medvtr.ord.cmis;


import org.apache.chemistry.opencmis.client.api.*;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CmisSessionFactory {
    private static final Logger log = LoggerFactory.getLogger(CmisSessionFactory.class);

    private final SessionFactory sessionFactory;
    private final Map<String, String> connectionProperties;
    private final String ROOT_PATH;

    private long lastCheck = 0;
    private Session session;

    public CmisSessionFactory(Map<String, String> connectionProperties, String rootPath) {
        log.info("<init>: with properties\n{}",
                connectionProperties.entrySet().stream().map(Object::toString).collect(Collectors.joining("\n"))
        );
        this.connectionProperties = connectionProperties;
        this.ROOT_PATH = rootPath;
        this.sessionFactory = SessionFactoryImpl.newInstance();

    }


    public Session getSession() {
        if (session == null || !check()) {
            this.session = createNewSession();
        }
        return session;
    }

    private boolean check() {
        long now = System.nanoTime();
        if (lastCheck > now || ((now - lastCheck) > (60L * 1000 * 1000000))) {
            try {
                final Folder folder = getRootFolder(session);
                folder.refresh();
                log.info("ping [{} ms]", (System.nanoTime() - now) / 1000000);
                lastCheck = now;
                return true;
            } catch (Exception e) {
                log.error("ping [error]");
                return false;
            }
        }
        return true;
    }

    private Session createNewSession() throws CmisConnectionException {
        final Repository repository = getRepository();
        if (repository == null) {
            return null;
        }
        log.debug("Create new session");
        return repository.createSession();
    }

    private Repository getRepository() {
        try {
            final List<Repository> resultList = sessionFactory.getRepositories(connectionProperties);
            if (resultList == null || resultList.isEmpty()) {
                log.error("Not found any repositories!!!");
                return null;
            }
            for (Repository x : resultList) {
                log.info("Discovered repository[{}]{id={}, description={}}", x.getName(), x.getId(), x.getDescription());
                if (log.isTraceEnabled()) {
                    log.trace(x.toString());
                }
            }
            return resultList.iterator().next();
        } catch (CmisConnectionException e) {
            log.error("Exception while fetching repositories: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Exception while fetching repositories: ", e);
            return null;
        }
    }


    public Folder getRootFolder(final Session session) {
        final CmisObject result = session.getObjectByPath(ROOT_PATH);
        if (BaseTypeId.CMIS_FOLDER.equals(result.getBaseTypeId())) {
            return (Folder) result;
        } else {
            log.error("ROOT_FOLDER: CmisObject['{}'] is not FOLDER. ActualType={}, BaseType={}", ROOT_PATH, result.getType(), result.getBaseTypeId());
            return null;
        }
    }

    public ContentStream createContentStream(String fileName, int length, String contentType, InputStream byteArrayInputStream) {
        return session.getObjectFactory().createContentStream(fileName, length, contentType, byteArrayInputStream);
    }

    public String getFullPath(String path) {
        return ROOT_PATH + path;
    }

    public CmisObject getObject(ObjectId objectId) {
        return getSession().getObject(objectId);
    }
}
