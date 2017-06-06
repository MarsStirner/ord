package ru.efive.dms.uifaces.beans.abstractBean;

import org.apache.chemistry.opencmis.client.api.Document;

import java.util.List;

public interface AttachmentHolder {

    List<Document> getAttachments();

}
