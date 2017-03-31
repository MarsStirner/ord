package ru.efive.dms.uifaces.beans.dialogs;

import com.github.javaplugs.jsf.SpringScopeView;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import ru.efive.dao.alfresco.Attachment;
import ru.efive.dms.uifaces.beans.FileManagementBean;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 02.07.2015, 18:58 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Controller("attachmentVersionDialog")
@SpringScopeView
public class AttachmentVersionDialogHolder extends AbstractDialog<Attachment> {
    public static final String DIALOG_SESSION_KEY = "DIALOG_ATTACHMENT_VERSION";
    public static final String DIALOG_DOCUMENT_KEY = "documentId";

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    @Qualifier("fileManagement")
    private FileManagementBean fileManagement;
    
    private String documentId;

    /**
     * Установить заранее заданные значения
     */
    @Override
    public void initializePreSelected() {
        this.selected = (Attachment) getFromExternalContext(DIALOG_SESSION_KEY);
    }

    @PostConstruct
    public void init() {
        logger.info("Initialize new AttachmentVersionDialog");
        final Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        logger.debug("With requestParams = {}", requestParameterMap);
        initializeDocumentKey(requestParameterMap);
        initializePreSelected();
    }

    public String initializeDocumentKey(Map<String, String> requestParameterMap) {
        final String value = requestParameterMap.get(DIALOG_DOCUMENT_KEY);
        if (StringUtils.isNotEmpty(value)) {
            this.documentId = value;
        }
        return null;
    }


    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        logger.info("Upload new file[{}] content-type={} size={}", file.getFileName(), file.getContentType(), file.getSize());
        final boolean uploadResult = fileManagement.createVersion(
                selected, file.getContents(), false, file.getFileName(), authData.getAuthorized()
        );
        if (uploadResult) {
            final DialogResult result = new DialogResult(
                    Button.CONFIRM, new FacesMessage(
                    "Successful! " + file.getFileName() + " is uploaded. Size " + file.getSize()
            )
            );
            logger.debug("DIALOG_BTN_CONFIRM:  {}", result);
            RequestContext.getCurrentInstance().closeDialog(result);
        } else {
            final DialogResult result = new DialogResult(
                    Button.CONFIRM, new FacesMessage(
                    "Error! " + file.getFileName() + " is not uploaded."
            )
            );
            logger.debug("DIALOG_BTN_CONFIRM:  {}", result);
            RequestContext.getCurrentInstance().closeDialog(result);
        }

    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(final String documentId) {
        this.documentId = documentId;
    }
}
