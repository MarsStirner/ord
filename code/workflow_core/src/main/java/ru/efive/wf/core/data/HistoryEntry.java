package ru.efive.wf.core.data;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.user.User;

/**
 * Запись истории workflow
 */
@Entity
@Table(name = "wf_history")
public class HistoryEntry extends IdentifiedEntity {

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocType() {
        return docType;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public int getInitialStatusId() {
        return initialStatusId;
    }

    public void setInitialStatusId(int initialStatusId) {
        this.initialStatusId = initialStatusId;
    }

    @Transient
    public String getInitialStatusName() {
        return "";//fromStatusId == 0? "": ApplicationHelper.getStatusName(getDocType(), fromStatusId);
    }

    public int getDestionationStatusId() {
        return destionationStatusId;
    }

    public void setDestionationStatusId(int destionationStatusId) {
        this.destionationStatusId = destionationStatusId;
    }

    @Transient
    public String getDestionationStatusName() {
        return "";//toStatusId == 0? "": ApplicationHelper.getStatusName(getDocType(), toStatusId);
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    @Transient
    public String getActionName() {
        return "";//ApplicationHelper.getActionName(getDocType(), actionId);
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public String getCommentary() {
        return commentary;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }


    /**
     * Дата создания
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date created;

    /**
     * id документа в формате "type_id"
     */
    @Column(name = "parent_id")
    private int parentId;

    /**
     * Тип документа
     */
    private String docType;

    /**
     * Время начала
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date started;

    /**
     * Время завершения
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date finished;

    /**
     * Пользователь, выполнивший действие
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "wf_history_owners",
            joinColumns = {@JoinColumn(name = "history_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private User owner;

    /**
     * Идентификатор начального статуса
     */
    @Column(name = "initial_status_id")
    private int initialStatusId;

    /**
     * Идентификатор конечного статуса
     */
    @Column(name = "destination_status_id")
    private int destionationStatusId;

    /**
     * Идентификатор действия
     */
    @Column(name = "action_id")
    private int actionId;

    /**
     * Было ли выполнено действие
     */
    private boolean processed;

    /**
     * Комментарий
     */
    @Column(columnDefinition = "text")
    private String commentary;

    /**
     * Удален ли документ
     */
    private boolean deleted;

    private static final long serialVersionUID = -67429605560038386L;
}