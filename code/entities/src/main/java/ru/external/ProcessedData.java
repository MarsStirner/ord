package ru.external;

import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;

import java.io.Serializable;

public interface ProcessedData extends Serializable {
    Integer getId();

    DocumentType getType();

    DocumentStatus getDocumentStatus();

    void setStatus(DocumentStatus status);

    String getWFResultDescription();
}