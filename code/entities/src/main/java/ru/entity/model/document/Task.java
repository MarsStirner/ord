package ru.entity.model.document;

import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.user.User;
import ru.external.ProcessedData;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Поручение
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "task")
@Access(AccessType.FIELD)
@AssociationOverrides({
        @AssociationOverride(name = "history", joinTable = @JoinTable(name = "task_history",
                joinColumns = {@JoinColumn(name = "document_id")},
                inverseJoinColumns = {@JoinColumn(name = "entry_id")}
        ))
})
public class Task extends DocumentEntity implements ProcessedData, Cloneable {

    /**
     * Контрольная дата исполнения
     */
    @Column(name = "controlDate")
    private LocalDateTime controlDate;


    /**
     * Инициатор
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = true)
    private User initiator;

    /**
     * Дата создания документа
     */
    @Column(name = "executionDate")
    private LocalDateTime executionDate;

    /**
     * id родительского поручения
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", nullable = true)
    private Task parent;

    /**
     * Исполнитель поручения
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "task_executor",
            joinColumns = {@JoinColumn(name = "document_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<User> executors;

    /**
     * id корневого документа
     */
    @Column(name = "rootDocumentId")
    private String rootDocumentId;


    /**
     * Номер ERP
     */
    @Column(name = "erpNumber")
    private String erpNumber;

    public String getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(String erpNumber) {
        this.erpNumber = erpNumber;
    }

    public LocalDateTime getControlDate() {
        return controlDate;
    }

    public void setControlDate(LocalDateTime controlDate) {
        this.controlDate = controlDate;
    }

    public LocalDateTime getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDateTime executionDate) {
        this.executionDate = executionDate;
    }

    public List<User> getExecutorsList() {
        if (executors != null) {
            return new ArrayList<>(executors);
        } else {
            return new ArrayList<>(0);
        }
    }

    public Set<User> getExecutors() {
        return executors;
    }

    public void setExecutors(Set<User> executors) {
        this.executors = executors;
    }

    public DocumentType getType() {
        return DocumentType.Task;
    }


    public Task getParent() {
        return parent;
    }

    public void setParent(Task parent) {
        this.parent = parent;
    }

    public String getRootDocumentId() {
        return rootDocumentId;
    }

    public void setRootDocumentId(String rootDocumentId) {
        this.rootDocumentId = rootDocumentId;
    }

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        final Task clone = new Task();
        clone.setId(getId());
        clone.setAuthor(getAuthor());
        clone.setControlDate(controlDate);
        clone.setController(getController());
        clone.setCreationDate(getCreationDate());
        clone.setDeleted(deleted);
        clone.setStatus(getDocumentStatus());
        clone.setErpNumber(erpNumber);
        clone.setExecutionDate(executionDate);
        clone.setExecutors(new HashSet<>(executors));
        clone.setForm(getForm());
        clone.setHistory(getHistory());
        clone.setInitiator(initiator);
        clone.setParent(parent);
        clone.setRegistrationDate(getRegistrationDate());
        clone.setRegistrationNumber(getRegistrationNumber());
        clone.setRootDocumentId(rootDocumentId);
        clone.setShortDescription(getShortDescription());
        return clone;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("Task[").append(getId())
                .append("]{ controlDate=").append(controlDate)
                .append(", creationDate=").append(getCreationDate())
                .append(", author=").append(getAuthor().getDescription())
                .append(", initiator=").append(initiator != null ? initiator.getDescription() : "null")
                .append(", controller=").append(getController() != null ? getController().getDescription() : "null")
                .append(", parent=").append(parent != null ? parent.getId() : "null")
                .append(", registrationDate=").append(getRegistrationDate())
                .append(", shortDescription='").append(getShortDescription()).append('\'')
                .append(", statusId=").append(getDocumentStatus())
                .append(", registrationNumber='").append(getRegistrationNumber()).append('\'')
                .append(", rootDocumentId='").append(rootDocumentId).append('\'')
                .append('}').toString();
    }
}