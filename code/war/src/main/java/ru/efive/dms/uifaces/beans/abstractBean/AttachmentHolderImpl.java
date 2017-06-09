package ru.efive.dms.uifaces.beans.abstractBean;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.bars_open.medvtr.ord.cmis.CmisDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.event.PhaseId;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class AttachmentHolderImpl implements AttachmentHolder {

    private final Logger log = LoggerFactory.getLogger(AttachmentHolderImpl.class);

    private final CmisDao cmisDao;
    private final AuthorizationData authData;
    private final String uniqueId;

    /**
     * Документы - вложения (CMIS / alfresco)
     */
    private List<Document> attachments;
    private Document current;
    private String checkinComment;
    private boolean major = false;


    public AttachmentHolderImpl(final CmisDao cmisDao, final AuthorizationData authData, final String uniqueId) {
        log.info("<init>: with {}", uniqueId);
        this.cmisDao = cmisDao;
        this.authData = authData;
        this.uniqueId = uniqueId;
    }

    @Override
    public List<Document> getAttachments() {
        return attachments;
    }

    @Override
    public boolean delete(Document document) {
        cmisDao.delete(document, authData);
        refresh();
        return true;
    }

    @Override
    public StreamedContent download(Document document) throws UnsupportedEncodingException {
        final ContentStream x = cmisDao.download(document, authData);
        return new DefaultStreamedContent(x.getStream(), x.getMimeType(),
                URLEncoder.encode(x.getFileName(), "UTF-8").replaceAll( "\\+", "%20"));
    }

    @Override
    public void handleFileUpload(FileUploadEvent event) throws IOException {
        final UploadedFile file = event.getFile();
        if (file == null) {
            log.error("[handleFileUpload] called without file!");
            return;
        }
        cmisDao.createDocument(
                uniqueId,
                file.getFileName(),
                file.getContentType(),
                Math.toIntExact(file.getSize()),
                file.getInputstream(),
                authData.getAuthorized().getId(),
                authData.getAuthorized().getDescription()
        );
        refresh();
    }

    @Override
    public Document getCurrent() {
        return current;
    }

    @Override
    public void setCurrent(Document document) {
        current = document;
        checkinComment = null;
        major = false;
    }

    @Override
    public String getCheckinComment() {
        return checkinComment;
    }

    @Override
    public void setCheckinComment(String checkinComment) {
        this.checkinComment = checkinComment;
    }

    @Override
    public boolean isMajor() {
        return major;
    }

    @Override
    public void setMajor(boolean major) {
        this.major = major;
    }

    @Override
    public void handleVersionUpload(FileUploadEvent event) {
        //@see <a href="http://stackoverflow.com/a/11012475">Solution</a>
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            final UploadedFile file = event.getFile();
            log.info("Upload new file[{}] content-type={} size={}", file.getFileName(), file.getContentType(), file.getSize());
            final Document uploadResult = cmisDao.createVersion(
                    current, major, checkinComment, authData, file.getFileName(), file.getContentType(), file.getContents()
            );
            checkinComment = null;
            major = false;
        }
        refresh();
    }

    @Override
    public void refresh() {
        attachments = cmisDao.getDocuments(uniqueId, authData);
    }

}
