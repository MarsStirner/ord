package ru.entity.model.document;

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
    @Column(name = "parent_id")
    private Integer parentId;
    

    /**
     * Тип документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type_id")
    private ru.entity.model.referenceBook.DocumentType docType;

    /**
     * Время завершения
     */
    @Column(name = "endDate")
    private LocalDateTime endDate;






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