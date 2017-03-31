package ru.entity.model.util;

import ru.entity.model.referenceBook.DocumentType;

import javax.persistence.*;

/**
 * Author: Upatov Egor <br>
 * Date: 08.07.2015, 18:30 <br>
 * Company: Korus Consulting IT <br>
 * Description: Справочник полей для типа документа (
 * TODO если добавить столбец "lang" + в PK, то можно получить i18n, ну или еще одна таблица  <поле - наименование - язык>
 * ) <br>
 */
@Entity
@Table(name = "rbDocumentTypeField")
@NamedQueries(
        @NamedQuery(name = "DocumentTypeField.getByDocumentTypeCode",
                query = "SELECT a FROM DocumentTypeField a JOIN FETCH a.documentType dt WHERE dt.code = :documentTypeCode"
        )
)
public class DocumentTypeField {
    @Id
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documentType_id")
    private DocumentType documentType;

    @Column(name = "fieldName")
    private String fieldName;

    @Column(name = "editable")
    private boolean editable;

    @Column(name = "fieldCode")
    private String fieldCode;

    public DocumentTypeField() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(final DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(final String fieldCode) {
        this.fieldCode = fieldCode;
    }
}
