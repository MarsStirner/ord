package ru.efive.dms.uifaces.beans.abstractBean;

import org.apache.chemistry.opencmis.client.api.Document;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.StreamedContent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface AttachmentHolder {

    List<Document> getAttachments();

    boolean delete(final Document document);

    StreamedContent download(final Document document) throws UnsupportedEncodingException;

    void handleFileUpload(final FileUploadEvent event) throws IOException;

    void setCurrent(final Document document);

    Document getCurrent();

    String getCheckinComment();

    void setCheckinComment(String checkinComment);

    boolean isMajor();

    void setMajor(boolean major);

    void handleVersionUpload(FileUploadEvent event);

    void refresh();
}
