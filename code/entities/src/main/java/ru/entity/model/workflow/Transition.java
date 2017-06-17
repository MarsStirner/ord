package ru.entity.model.workflow;

import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Список переходов и действий над документом
 */
@Entity
@Table(name = "wf_transition")
public class Transition extends DeletableEntity{

    /**
     * Тип документа (NULL - любой)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_type_id", nullable = true)
    private DocumentType documentType;

    /**
     * С какого статуса переход (NULL - любой)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_status_id", nullable = true)
    private Status fromStatus;

    /**
     * Какое действие будет выполнено при переходе
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    /**
     * На какой статус переход (NULL - переход не меняет статус)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_status_id", nullable = true)
    private Status toStatus;

    /**
     * Нужно ли создавать запись в истории документа: 0 - нет, 1 - да
     */
    @Column(name = "write_history")
    private boolean writeHistory;

    /**
     * Нужно ли запрашивать у пользователя комментарий при совершении действия: 0 - нет, 1 - да
     */
    @Column(name = "need_comment")
    private boolean needComment;

    public Transition() {
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public Status getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(Status fromStatus) {
        this.fromStatus = fromStatus;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Status getToStatus() {
        return toStatus;
    }

    public void setToStatus(Status toStatus) {
        this.toStatus = toStatus;
    }

    public boolean isWriteHistory() {
        return writeHistory;
    }

    public void setWriteHistory(boolean writeHistory) {
        this.writeHistory = writeHistory;
    }

    public boolean isNeedComment() {
        return needComment;
    }

    public void setNeedComment(boolean needComment) {
        this.needComment = needComment;
    }
}