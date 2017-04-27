package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.chemistry.opencmis.client.api.Document;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.bars_open.medvtr.ord.cmis.CmisDao;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.faces.event.PhaseId;

@ViewScopedController(value = "versionDialog")
public class VersionDialog {

    private static final Logger log = LoggerFactory.getLogger("CMIS");

    @Autowired
    @Qualifier("cmisDao")
    private CmisDao cmisDao;

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    private Document document;

    private String checkinComment;

    private boolean major = false;

    public void init(Document document) {
        log.debug("<init> Add Version Dialog Bean[@{}]", Integer.toHexString(hashCode()));
        this.document = document;
        clear();

    }

    private void clear() {
        checkinComment = null;
        major = false;
    }

    public String getName() {
        return document.getName();
    }

    public String getVersionLabel() {
        return document.getVersionLabel();
    }

    public String getCheckinComment() {
        return checkinComment;
    }

    public void setCheckinComment(String checkinComment) {
        this.checkinComment = checkinComment;
    }

    public boolean isMajor() {
        return major;
    }

    public void setMajor(boolean major) {
        this.major = major;
    }

    public void handleFileUpload(FileUploadEvent event) {
        //@see <a href="http://stackoverflow.com/a/11012475">Solution</a>
        if (!PhaseId.INVOKE_APPLICATION.equals(event.getPhaseId())) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
        } else {
            final UploadedFile file = event.getFile();
            log.info("Upload new file[{}] content-type={} size={}", file.getFileName(), file.getContentType(), file.getSize());
            final Document uploadResult = cmisDao.createVersion(
                    document, major, checkinComment, authData, file.getFileName(), file.getContentType(), file.getContents()
            );
            clear();
        }

    }
}
