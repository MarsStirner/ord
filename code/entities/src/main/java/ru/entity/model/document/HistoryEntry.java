package ru.entity.model.document;

import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Запись истории workflow
 */
@Entity
@Table(name = "wf_history")
public class HistoryEntry extends IdentifiedEntity implements Comparable<HistoryEntry> {
    private static final long serialVersionUID = -67429605560038386L;

    /**
     * Идентификатор действия
     */
    @Column(name = "action_id")
    private Integer actionId;

    /**
     * Комментарий
     */
    @Column(name = "commentary", columnDefinition = "text")
    private String commentary;

    /**
     * Дата создания
     */
    @Column(name = "created")
    private LocalDateTime created;

    /**
     * Тип документа
     */
    @Column(name = "docType")
    private String docType;

    /**
     * Время завершения
     */
    @Column(name = "endDate")
    private LocalDateTime endDate;

    /**
     * Идентификатор начального статуса
     */
    @Column(name = "from_status_id")
    private Integer fromStatusId;


    /**
     * id документа в формате "type_id"
     */
    @Column(name = "parent_id")
    private Integer parentId;

    /**
     * Было ли выполнено действие: 0 - нет, 1 - да
     */
    @Column(name = "processed")
    private boolean processed;


    /**
     * Время начала
     */
    @Column(name = "startDate")
    private LocalDateTime startDate;

    /**
     * Идентификатор конечного статуса
     */
    @Column(name = "to_status_id")
    private Integer toStatusId;

    /**
     * Пользователь, выполнивший действие
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = true)
    private User owner;


    public HistoryEntry() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS & SETTERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Integer getActionId() {
        return actionId;
    }

    public void setActionId(Integer actionId) {
        this.actionId = actionId;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getFromStatusId() {
        return fromStatusId;
    }

    public void setFromStatusId(Integer fromStatusId) {
        this.fromStatusId = fromStatusId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public Integer getToStatusId() {
        return toStatusId;
    }

    public void setToStatusId(Integer toStatusId) {
        this.toStatusId = toStatusId;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Custom methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getFromStatusName() {
        return fromStatusId == null || fromStatusId == 0 ? "" : DocumentType.getStatusName(getDocType(), fromStatusId);
    }


    public String getToStatusName() {
        return toStatusId == 0 ? "" : DocumentType.getStatusName(getDocType(), toStatusId);
    }

    public String getActionName() {
        return DocumentType.getActionName(getDocType(), actionId);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface Comparable
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(HistoryEntry o) {
        if (o == null) {
            return 1;
        }
        if (created == null) {
            return o.getCreated() == null ? 0 : -1;
        } else {
            return o.getCreated() == null ? 1 : created.compareTo(o.getCreated());
        }
    }
}