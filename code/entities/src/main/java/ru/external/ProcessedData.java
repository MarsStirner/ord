package ru.external;

import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;

import java.io.Serializable;

public interface ProcessedData extends Serializable {
    public int getId();

    public DocumentType getDocumentType();

    public DocumentStatus getDocumentStatus();

    public void setDocumentStatus(DocumentStatus status);

    public String getBeanName();

}