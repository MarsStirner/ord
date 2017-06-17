package ru.entity.model.workflow;

import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Запись истории workflow
 */
@Entity
@Table(name = "wf_history")
public class HistoryEntry extends IdentifiedEntity implements Comparable<HistoryEntry> {

    /**
     * Дата + время совершения действия
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * Произведенное над документом действие
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    /**
     * На каком статусе документа было выполнено действие
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_status_id", nullable = false)
    private Status fromStatus;


    /**
     * На какой статус был переведен документ во время выполнения действия
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_status_id", nullable = true)
    private Status toStatus;

    /**
     * Пользователь, выполнивший действие
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Комментарий
     */
    @Column(name = "commentary")
    private String commentary;


    /**
     * id документа в формате "type_id"
     */
    @Column(name = "document_id")
    private Integer documentId;

    /**
     * Тип документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id")
    private DocumentType documentType;

    /**
     * Было ли выполнено действие: 0 - нет, 1 - да
     */
    @Column(name = "processed")
    private boolean processed;

    public HistoryEntry() {
    }

    public <I extends DocumentEntity> HistoryEntry(I document) {
        this.documentId = document.getId();
        this.documentType = document.getType();
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Status getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(Status fromStatus) {
        this.fromStatus = fromStatus;
    }

    public Status getToStatus() {
        return toStatus;
    }

    public void setToStatus(Status toStatus) {
        this.toStatus = toStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Comparable
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(HistoryEntry o) {
        if (o == null) {
            return 1;
        }
        if (startDate == null) {
            return o.getStartDate() == null ? 0 : -1;
        } else {
            return o.getStartDate() == null ? 1 : startDate.compareTo(o.getStartDate());
        }
    }
}