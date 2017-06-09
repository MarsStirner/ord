package ru.bars_open.medvtr.ord.cmis;


import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.chemistry.opencmis.commons.enums.Action.CAN_CHECK_OUT;
import static org.apache.chemistry.opencmis.commons.enums.BaseTypeId.CMIS_FOLDER;

public class CmisDao {
    private static final Logger log = LoggerFactory.getLogger(CmisSessionFactory.class);

    private CmisSessionFactory sessionFactory;

    public void setSessionFactory(CmisSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Document> getDocuments(final String name, final AuthorizationData authData) {
        log.info("start fetch CMIS-documents in Folder[{}] by {}", sessionFactory.getFullPath(name), authData.getLogString());
        try {
            final Folder documentFolder = getFolder(name);
            if (documentFolder != null) {
                return StreamSupport.stream(documentFolder.getChildren().spliterator(), false).map(Document.class::cast).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }catch (Exception e){
            log.error("Error while fetch Folder[{}] ", sessionFactory.getFullPath(name));
            return null;
        }
    }


    public ContentStream download(final Document document, final AuthorizationData authData) {
        log.info("start download Document[{}][name={}] by {}", document.getId(), document.getName(), authData.getLogString());
        try {
            return document.getContentStream();
        } catch (Exception e) {
            log.error("Error while download content for Document[{}][name={}] ", document.getId(), document.getName());
            return null;
        }
    }

    public void delete(final Document document, final AuthorizationData authData) {
        log.error("{} start delete Document[{}][name={}]", authData.getLogString(), document.getId(), document.getName());
        document.delete(true);
        log.error("{} successfully delete Document[{}][name={}]", authData.getLogString(), document.getId(), document.getName());
    }


    private Map<String, Object> createFileMetaData(String fileName, String contentType, int userId, String userFullName) {
        final Map<String, Object> result = new HashMap<>();
        result.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
        result.put(PropertyIds.NAME, fileName);
        result.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, contentType);
        result.put(PropertyIds.DESCRIPTION, userFullName);
        return result;
    }

    public Folder getOrCreateFolder(String path) {
        final Folder result = getFolder(path);
        if (result == null) {
            return createFolder(path);
        }
        return result;
    }

    private Folder createFolder(String path) {
        final Map<String, String> props = new HashMap<>(2);
        props.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
        props.put(PropertyIds.NAME, path);
        final Session session = sessionFactory.getSession();
        if(session !=null) {
            final Folder result = sessionFactory.getRootFolder(session).createFolder(props);
            log.info("Created new Folder['{}']{name='{}', id='{}'}", result.getPath(), result.getName(), result.getId());
            return result;
        } else {
            log.error("Could not create folder cause no session/connection available");

            return null;
        }
    }

    /**
     * Получить FOLDER по заданному пути или null
     *
     * @param path путь для поиска
     * @return Папка или NULL
     */
    public Folder getFolder(String path) {
        final String fullPath = sessionFactory.getFullPath(path);
        final Session session = sessionFactory.getSession();
        if (session == null) {
            log.error("Folder['{}'] NO SESSION", fullPath);
            return null;
        }
        try {
            final CmisObject result = session.getObjectByPath(fullPath);
            if (CMIS_FOLDER.equals(result.getBaseTypeId())) {
                log.info("Folder['{}'] id = {}", fullPath, result.getId());
                return (Folder) result;
            } else {
                log.warn("CmisObject['{}'] is not FOLDER. ActualType={}, BaseType={}", fullPath, result.getType(), result.getBaseTypeId());
                return null;
            }
        } catch (CmisObjectNotFoundException onfe) {
            log.debug("Folder['{}'] not exists", fullPath);
            return null;
        } catch (Exception e) {
            log.error("Folder['{}']: Error: {} ", fullPath,  e.getMessage());
            return null;
        }
    }

    // document, false, authData, file.getFileName(), file.getContentType(), file.getContents()
    public Document createVersion(
            final Document original,
            final boolean major,
            final String checkinComment,
            final AuthorizationData authData,
            final String fileName,
            final String contentType,
            final byte[] content) {
        if (original.getAllowableActions().getAllowableActions().contains(CAN_CHECK_OUT)) {
            original.refresh();
            Document pwc = (Document) sessionFactory.getObject(original.checkOut());
            final Map<String, Object> metadata = new HashMap<>();
            metadata.put(PropertyIds.DESCRIPTION, authData.getAuthorized().getDescription());
            metadata.put(PropertyIds.CONTENT_STREAM_FILE_NAME, fileName);
            metadata.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, contentType);
            final ContentStream contentStream = sessionFactory.createContentStream(fileName, content.length, contentType, new ByteArrayInputStream(content));
            pwc.checkIn(major, metadata, contentStream, checkinComment);
            return original;
        }
        return null;
    }

    public void createDocument(
            final String targetFolder,
            final String fileName,
            final String contentType,
            int size,
            final InputStream stream,
            final Integer userId,
            final String userFullName
    ) {
        log.debug("start upload File[{}] to Folder[{}] by User[{}][{}]", fileName, targetFolder, userId, userFullName);
        final Folder folder = getOrCreateFolder(targetFolder);
        if(folder == null){ return; }
        final Map<String, Object> metadata = createFileMetaData(fileName, contentType, userId, userFullName);
        final ContentStream contentStream = sessionFactory.createContentStream(fileName, size, contentType, stream);
        final Document document = folder.createDocument(metadata, contentStream, VersioningState.MAJOR);
        log.info("Created document[{}]: {}", document.getName(), document.getId());
    }
}
