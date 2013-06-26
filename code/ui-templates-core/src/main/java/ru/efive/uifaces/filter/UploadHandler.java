package ru.efive.uifaces.filter;

/**
 * The handler than shoul process uload events.
 *
 * @author Pavel Porubov
 */
public interface UploadHandler {

    /**
     * the handler that should process upload event
     * @param uploadInfo the information about upload event
     */
    void handleUpload(UploadInfo uploadInfo);
}
