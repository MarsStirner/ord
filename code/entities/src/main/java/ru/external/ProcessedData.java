package ru.external;

import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;

import java.io.Serializable;

public interface ProcessedData extends Serializable {
    Integer getId();

    DocumentType getDocumentType();

    DocumentStatus getDocumentStatus();

    void setDocumentStatus(DocumentStatus status);

    String getBeanName();

    String getWFResultDescription();
}