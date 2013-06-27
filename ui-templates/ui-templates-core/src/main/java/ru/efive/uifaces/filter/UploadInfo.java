package ru.efive.uifaces.filter;

import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The information bean about the upload event.
 * <br>This information includes the following proprties:
 * <ul>
 * <li>request - the request for that the upload event occurs;</li>
 * <li>response- the response for that the upload event occurs;</li>
 * <li>fieldName - the form's file filed name;</li>
 * <li>fileName - the uploading file name;</li>
 * <li>data - the live stream with file's data than acquired from the request.</li>
 * </ul>
 *
 * @author Pavel Porubov
 */
public class UploadInfo {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private String fieldName;
    private String fileName;
    private InputStream data;

    /**
     * Constructs uninitialized bean.
     */
    public UploadInfo() {
    }

    /**
     * Constructs the bean with specified information about upload event.
     * @param request the request for that the upload event occurs
     * @param response the response for that the upload event occurs
     * @param fieldName the form's file filed name
     * @param fileName the uploading file name
     * @param data the live stream with file's data than acquired from the request
     */
    public UploadInfo(HttpServletRequest request, HttpServletResponse response, String fieldName, String fileName, InputStream data) {
        this.request = request;
        this.response = response;
        this.fieldName = fieldName;
        this.fileName = fileName;
        this.data = data;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }
}
