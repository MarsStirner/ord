package ru.entity.model.document;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.IdentifiedEntity;
import ru.entity.model.user.User;
import ru.external.ProcessedData;
import ru.util.ApplicationHelper;

import javax.persistence.*;
import java.util.*;

/**
 * Поручение
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_tasks")
public class Task extends IdentifiedEntity implements ProcessedData, Cloneable {

    private static final long serialVersionUID = -1414080814402194966L;

    /**
     * На контроле
     */
    @Column(name = "control", nullable = false)
    private boolean control;

    /**
     * Контрольная дата исполнения
     */
    @Column(name = "controlDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date controlDate;

    /**
     * Дата создания
     */
    @Column(name = "creationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Автор поручения
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;


    /**
     * Инициатор
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "initiator_id")
    private User initiator;

    /**
     * Контролер
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "controller_id")
    private User controller;

    /**
     * Удален ли документ
     */
    @Column(name = "deleted")
    private boolean deleted;

    /**
     * Дата создания документа
     */
    @Column(name = "executionDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date executionDate;

    /**
     * id родительского поручения
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parentId", nullable = true)
    private Task parent;

    /**
     * Дата регистрации
     */
    @Column(name = "registrationDate")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date registrationDate;

    /**
     * Краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text")
    private String shortDescription;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    /**
     * Номер поручения
     */
    @Column(name = "taskNumber")
    private String taskNumber;

    /**
     * Исполнитель поручения
     */
    @ManyToMany(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_tasks_executors",
            joinColumns = {@JoinColumn(name = "id")},
            inverseJoinColumns = {@JoinColumn(name = "executor_id")}
    )
    private Set<User> executors;

    /**
     * id корневого документа
     */
    @Column(name = "rootDocumentId")
    private String rootDocumentId;

    /**
     * Вид задачи
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "exerciseType_id")
    private DocumentForm exerciseType;

    /**
     * Вид документа
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "form_id")
    private DocumentForm form;

    /**
     * Номер ERP
     */
    @Column(name = "erpNumber")
    private String erpNumber;

    /**
     * История
     */
    @OneToMany (cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_task_history",
            joinColumns = {@JoinColumn(name = "task_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    private Set<HistoryEntry> history;

    @Transient
    private String WFResultDescription;


    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public String getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(String erpNumber) {
        this.erpNumber = erpNumber;
    }


    public void setControlDate(Date controlDate) {
        this.controlDate = controlDate;
    }

    public Date getControlDate() {
        return controlDate;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<User> getExecutors() {
        if(executors != null) {
            return new ArrayList<User>(executors);
        } else {
            return new ArrayList<User>(0);
        }
    }

    public void setExecutors(Set<User> executors) {
        this.executors = executors;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }


    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }

    @Transient
    public DocumentType getDocumentType() {
        return DocumentType.Task;
    }

    @Transient
    public DocumentStatus getDocumentStatus() {
        return DocumentType.getStatus(getDocumentType().getName(), this.statusId);
    }

    @Transient
    public void setDocumentStatus(DocumentStatus status) {
        this.statusId = status.getId();
    }

    @Override
    public String getBeanName() {
        return "task";
    }

    public void setParent(Task parent) {
        this.parent = parent;
    }

    public Task getParent() {
        return parent;
    }


    @Transient
    public String getUniqueId() {
        return getId() == 0 ? "" : "task_" + getId();
    }



    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
    }

    public Set<HistoryEntry> getHistory() {
        return history;
    }

    @Transient
    public List<HistoryEntry> getHistoryList() {
        List<HistoryEntry> result = new ArrayList<HistoryEntry>();
        if (history != null) {
            result.addAll(history);
        }
        Collections.sort(result);
        return result;
    }

    public void setRootDocumentId(String rootDocumentId) {
        this.rootDocumentId = rootDocumentId;
    }

    public String getRootDocumentId() {
        return rootDocumentId;
    }

    public void setWFResultDescription(String wFResultDescription) {
        WFResultDescription = wFResultDescription;
    }

    public String getWFResultDescription() {
        return WFResultDescription;
    }

    public void setForm(DocumentForm form) {
        this.form = form;
    }

    public DocumentForm getForm() {
        return form;
    }

    public void setExerciseType(DocumentForm exerciseType) {
        this.exerciseType = exerciseType;
    }

    public DocumentForm getExerciseType() {
        return exerciseType;
    }

    public void setController(User controller) {
        this.controller = controller;
    }

    public User getController() {
        return controller;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    public User getInitiator() {
        return initiator;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        final Task clone = new Task();
        clone.setId(getId());
        clone.setAuthor(author);
        clone.setControl(control);
        clone.setControlDate(controlDate);
        clone.setController(controller);
        clone.setCreationDate(creationDate);
        clone.setDeleted(deleted);
        clone.setDocumentStatus(getDocumentStatus());
        clone.setErpNumber(erpNumber);
        clone.setExecutionDate(executionDate);
        clone.setExecutors(executors);
        clone.setExerciseType(exerciseType);
        clone.setForm(form);
        clone.setHistory(history);
        clone.setInitiator(initiator);
        clone.setParent(parent);
        clone.setRegistrationDate(registrationDate);
        clone.setRootDocumentId(rootDocumentId);
        clone.setShortDescription(shortDescription);
        clone.setTaskNumber(taskNumber);
        return clone;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Task[");
        sb.append(getId()).append("]:{");
        sb.append("controlDate=").append(ApplicationHelper.formatDate(controlDate));
        sb.append(", creationDate=").append(ApplicationHelper.formatDate(creationDate));
        sb.append(", author=").append(author.getFullName());
        sb.append(", initiator=").append(initiator != null ? initiator.getFullName() : "null");
        sb.append(", controller=").append(controller != null ? controller.getFullName() : "null");
        sb.append(", parent=").append(parent != null ? parent.getId() : "null");
        sb.append(", registrationDate=").append(ApplicationHelper.formatDate(registrationDate));
        sb.append(", shortDescription='").append(shortDescription).append('\'');
        sb.append(", statusId=").append(statusId);
        sb.append(", taskNumber='").append(taskNumber).append('\'');
        sb.append(", rootDocumentId='").append(rootDocumentId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}