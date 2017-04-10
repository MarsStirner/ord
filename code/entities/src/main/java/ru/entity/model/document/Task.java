package ru.entity.model.document;

import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.DocumentEntity;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.user.User;
import ru.external.ProcessedData;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Поручение
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_tasks")
public class Task extends DocumentEntity implements ProcessedData, Cloneable {

    private static final long serialVersionUID = -1414080814402194966L;

    /**
     * Контрольная дата исполнения
     */
    @Column(name = "controlDate")
    private LocalDateTime controlDate;

    /**
     * Дата создания
     */
    @Column(name = "creationDate")
    private LocalDateTime creationDate;


    /**
     * Инициатор
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = true)
    private User initiator;

    /**
     * Контролер
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id", nullable = true)
    private User controller;

    /**
     * Дата создания документа
     */
    @Column(name = "executionDate")
    private LocalDateTime executionDate;

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
    private LocalDateTime registrationDate;

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
    @ManyToMany(fetch = FetchType.LAZY)
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
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "dms_task_history",
            joinColumns = {@JoinColumn(name = "task_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    private Set<HistoryEntry> history;

    @Transient
    private String WFResultDescription;
    /**
     * Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     * TODO сделать класс-обертку
     */
    @Transient
    private String styleClass;

    public String getTaskNumber() {
        return taskNumber;
    }

    public void setTaskNumber(String taskNumber) {
        this.taskNumber = taskNumber;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
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

    public Task getParent() {
        return parent;
    }

    public void setParent(Task parent) {
        this.parent = parent;
    }

    @Transient
    public String getUniqueId() {
        return getId() == null ? "" : "task_" + getId();
    }

    public Set<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
    }

    @Transient
    public List<HistoryEntry> getHistoryList() {
        List<HistoryEntry> result = new ArrayList<>();
        if (history != null) {
            result.addAll(history);
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Добавление в историю еще одной записи, если история пуста, то она создается
     *
     * @param historyEntry Запись в истории, которую надо добавить
     * @return статус добавления (true - успех)
     */
    public boolean addToHistory(final HistoryEntry historyEntry) {
        if (history == null) {
            this.history = new HashSet<>(1);
        }
        return this.history.add(historyEntry);
    }

    public String getRootDocumentId() {
        return rootDocumentId;
    }

    public void setRootDocumentId(String rootDocumentId) {
        this.rootDocumentId = rootDocumentId;
    }

    public String getWFResultDescription() {
        return WFResultDescription;
    }

    public void setWFResultDescription(String wFResultDescription) {
        WFResultDescription = wFResultDescription;
    }

    public DocumentForm getForm() {
        return form;
    }

    public void setForm(DocumentForm form) {
        this.form = form;
    }

    public User getController() {
        return controller;
    }

    public void setController(User controller) {
        this.controller = controller;
    }

    public User getInitiator() {
        return initiator;
    }

    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        final Task clone = new Task();
        clone.setId(getId());
        clone.setAuthor(getAuthor());
        clone.setControlDate(controlDate);
        clone.setController(controller);
        clone.setCreationDate(creationDate);
        clone.setDeleted(deleted);
        clone.setDocumentStatus(getDocumentStatus());
        clone.setErpNumber(erpNumber);
        clone.setExecutionDate(executionDate);
        clone.setExecutors(new HashSet<>(executors));
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
        return new StringBuilder().append("Task[").append(getId())
                .append("]{ controlDate=").append(controlDate)
                .append(", creationDate=").append(creationDate)
                .append(", author=").append(getAuthor().getDescription())
                .append(", initiator=").append(initiator != null ? initiator.getDescription() : "null")
                .append(", controller=").append(controller != null ? controller.getDescription() : "null")
                .append(", parent=").append(parent != null ? parent.getId() : "null")
                .append(", registrationDate=").append(registrationDate)
                .append(", shortDescription='").append(shortDescription).append('\'')
                .append(", statusId=").append(statusId)
                .append(", taskNumber='").append(taskNumber).append('\'')
                .append(", rootDocumentId='").append(rootDocumentId).append('\'')
                .append('}').toString();
    }

    @Override
    public String getType() {
        return ru.entity.model.referenceBook.DocumentType.RB_CODE_TASK;
    }
}