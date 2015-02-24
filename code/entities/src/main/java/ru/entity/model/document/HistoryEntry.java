package ru.entity.model.document;

import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.Date;

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
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    /**
     * Тип документа
     */
    @Column(name = "docType")
    private String docType;

    /**
     * Время завершения
     */
    @Column(name = "endDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date endDate;

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
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date startDate;

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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
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