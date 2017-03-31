package ru.efive.dms.uifaces.beans.incoming;

import ru.entity.model.document.IncomingDocument;

/**
 * Author: Upatov Egor <br>
 * Date: 06.10.2014, 20:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class IncomingDocumentNode {

    private IncomingDocument document;
    private String header;
    private DOC_TYPE type;

    public IncomingDocumentNode(IncomingDocument doc, String header) {
        this.type = DOC_TYPE.DOCUMENT;
        this.document = doc;
        this.header = header;
    }

    public IncomingDocumentNode(DOC_TYPE type, String header) {
        this.header = header;
        this.type = type;
    }

    public IncomingDocument getDocument() {
        return document;
    }

    public void setDocument(IncomingDocument document) {
        this.document = document;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public DOC_TYPE getType() {
        return type;
    }

    public void setType(DOC_TYPE type) {
        this.type = type;
    }

    public enum DOC_TYPE {
        YEAR,
        MONTH,
        DAY,
        DOCUMENT
    }
}
