package ru.entity.model.util;

import javax.persistence.*;

/**
 * Author: Upatov Egor <br>
 * Date: 08.07.2015, 18:41 <br>
 * Company: Korus Consulting IT <br>
 * Description: Матрица редактируемости полей в зависимости от статуса документа <br>
 */
@Entity
@Table(name = "utilEditableFieldMatrix")
@IdClass(EditableFieldMatrixPK.class)
@NamedQueries(
        @NamedQuery(name = "EditableFieldMatrix.getByDocumentTypeCode",
                query = "SELECT efm FROM EditableFieldMatrix efm " +
                        "INNER JOIN FETCH efm.field f " +
                        "INNER JOIN FETCH f.documentType dt " +
                        "WHERE dt.code = :documentTypeCode ORDER BY efm.statusId"))
public class EditableFieldMatrix {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "field_id")
    private DocumentTypeField field;

    @Id
    @Column(name = "status_id")
    private int statusId;

    @Column(name = "editable")
    private boolean editable;

    public EditableFieldMatrix() {
    }

    public DocumentTypeField getField() {
        return field;
    }

    public void setField(final DocumentTypeField field) {
        this.field = field;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(final int statusId) {
        this.statusId = statusId;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(final boolean editable) {
        this.editable = editable;
    }
}
