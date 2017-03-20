package ru.efive.dms.uifaces.beans;

import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dao.alfresco.AlfrescoDAO;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dao.alfresco.Revision;
import ru.entity.model.user.User;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named("fileManagement")
@SessionScoped
public class FileManagementBean implements java.io.Serializable {
    private static final Logger logger = LoggerFactory.getLogger("ALFRESCO");
    private static final String CONTENT_TYPE = "application/octet-stream;charset=UTF-8";


    // file operations

    public synchronized Attachment getFileById(String id) {
        Attachment result = null;
        AlfrescoDAO<Attachment> dao = null;
        try {
            dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
            if (dao == null) {
                return null;
            }
            result = dao.getDataById(id);
        } catch (Exception e) {
            result = null;
            logger.error("", e);
        } finally {
            if (dao != null) dao.disconnect();
        }
        return result;
    }

    public synchronized List<Attachment> getFilesByParentId(String id) {
        List<Attachment> result = Collections.synchronizedList(new ArrayList<Attachment>());
        AlfrescoDAO<Attachment> dao = null;
        try {
            dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
            if (dao == null) {
                return result;
            }
            result = dao.getDataList(
                    "+TYPE:\"" + new Attachment().getNamedNodeType() + "\"" +
                            " +@e5-dms\\:parentId:\"" + id + "\""
            );
            //" +ALL:\""+ id+ "\"");
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (dao != null) dao.disconnect();
        }
        return result;
    }

    public boolean createFile(Attachment file, byte[] bytes) {
        boolean result = false;
        AlfrescoDAO<Attachment> dao = null;
        try {
            dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
            if (dao == null) {
                return false;
            }
            result = dao.createContent(file, bytes);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (dao != null) dao.disconnect();
        }
        return result;
    }

    public synchronized boolean deleteFile(Attachment file) {
        boolean result = false;
        AlfrescoDAO<Attachment> dao = null;
        try {
            dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
            if (dao == null) {
                return false;
            }
            result = dao.deleteData(file);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (dao != null) dao.disconnect();
        }
        return result;
    }

    public StreamedContent download(String id){
        final byte[] result;
        AlfrescoDAO<Attachment> dao = null;
        if (StringUtils.isNotEmpty(id)) {
            try {
                dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
                if (dao == null) {
                    logger.error("DAO IS NULL");
                    return null;
                }
                final Attachment attachment = dao.getDataById(id);
                result = dao.getContent(attachment);
                final String fileName=StringUtils.defaultIfEmpty(attachment.getCurrentRevision().getFileName(), attachment.getFileName());
                return new DefaultStreamedContent(new ByteArrayInputStream(result), CONTENT_TYPE, URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replaceAll(
                        "\\+", "%20"));
            } catch (Exception e) {
                logger.error("", e);
                return null;
            } finally {
                if (dao != null) dao.disconnect();
            }
        } else {
            return null;
        }
    }

    public StreamedContent download(String id, String version){
        byte[] result = {};
        AlfrescoDAO<Attachment> dao = null;
        if (StringUtils.isNotEmpty(id)) {
            if(StringUtils.isNotEmpty(version)) {
                try {
                    dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
                    if (dao == null) {
                        logger.error("DAO IS NULL");
                        return null;
                    }
                    final Attachment attachment = dao.getDataById(id);
                    String downloadFileName=null;
                    if (attachment.isVersionable()) {
                        for (Revision revision : attachment.getRevisions()) {
                            if (revision.getVersion().equals(version)) {
                                downloadFileName = revision.getFileName();
                                break;
                            }
                        }
                    }
                    if (StringUtils.isEmpty(downloadFileName)) {
                        downloadFileName = attachment.getFileName();
                    }
                    result = dao.getContent(attachment, version);
                    return new DefaultStreamedContent(
                            new ByteArrayInputStream(result), CONTENT_TYPE,
                            URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8.name()).replaceAll(
                            "\\+", "%20"
                    )
                    );
                } catch (Exception e) {
                    logger.error("", e);
                    return null;
                } finally {
                    if (dao != null) dao.disconnect();
                }
            } else {
                logger.warn("Version param is not set. download via ID only");
                return download(id);
            }
        } else {
            return null;
        }
    }

    public synchronized boolean createVersion(Attachment file, byte[] bytes, boolean majorVersion, String fileName, User owner) {
        boolean result = false;
        AlfrescoDAO<Attachment> dao = null;
        try {
            dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
            if (dao == null) {
                return false;
            }
            result = dao.createVersion(file, bytes, owner.getId(), fileName, majorVersion);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (dao != null) dao.disconnect();
        }
        return result;
    }



    public synchronized boolean createVersion(Attachment file, byte[] bytes, boolean majorVersion, String fileName) {
        boolean result = false;
        AlfrescoDAO<Attachment> dao = null;
        try {
            dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
            if (dao == null) {
                return false;
            }
            result = dao.createVersion(file, bytes, sessionManagementBean.getLoggedUser().getId(), fileName, majorVersion);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (dao != null) dao.disconnect();
        }
        return result;
    }

    public synchronized List<Revision> getVersionHistory(Attachment file) {
        List<Revision> result = new ArrayList<>();
        AlfrescoDAO<Attachment> dao = null;
        try {
            dao = sessionManagementBean.getAlfrescoDAO(Attachment.class);
            if (dao == null) {
                return result;
            }
            result = dao.getRevisions(file);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            if (dao != null) dao.disconnect();
        }
        return result;
    }


    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagementBean;

    private static final long serialVersionUID = -4470091321891731494L;
}