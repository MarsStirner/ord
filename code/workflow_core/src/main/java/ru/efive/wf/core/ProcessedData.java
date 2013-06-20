package ru.efive.wf.core;

import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.DocumentType;

import java.io.Serializable;

public interface ProcessedData extends Serializable {
    public int getId();

    public DocumentType getDocumentType();

    public DocumentStatus getDocumentStatus();

    public void setDocumentStatus(DocumentStatus status);

    public String getBeanName();

}