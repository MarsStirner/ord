package ru.entity.model.wf;

import ru.entity.model.mapped.Document;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * Задача-согласование
 */
@Entity
@Table(name = "wf_tasks")
public class HumanTask extends Document {

    public void setExecutor(User executor) {
        this.executor = executor;
    }

    public User getExecutor() {
        return executor;
    }

    public void setExecuted(Date executed) {
        this.executed = executed;
    }

    public Date getExecuted() {
        return executed;
    }

    public void setParentNode(HumanTaskTreeNode parentNode) {
        this.parentNode = parentNode;
    }

    public HumanTaskTreeNode getParentNode() {
        return parentNode;
    }

    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public String getCommentary() {
        return commentary;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }


    @Override
    public int hashCode() {
        return ((executor == null) ? 0 : executor.getId()) + statusId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        HumanTask other = (HumanTask) obj;
        if (executor == null) {
            if (other.executor != null)
                return false;
        } else if (!Objects.equals(executor.getId(), other.executor.getId()))
            return false;
        return statusId == other.statusId;
    }


    /**
     * Исполнитель
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private User executor;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date executed;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    private HumanTaskTreeNode parentNode;

    /**
     * Комментарий
     */
    @Column(columnDefinition = "text")
    private String commentary;

    /**
     * Статус
     */
    private int statusId;


    private static final long serialVersionUID = 6459711483730948995L;
}