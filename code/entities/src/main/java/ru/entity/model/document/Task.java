package ru.entity.model.document;

import ru.entity.model.enums.DocumentStatus;
import ru.entity.model.enums.DocumentType;
import ru.entity.model.mapped.DeletableEntity;
import ru.entity.model.referenceBook.DocumentForm;
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
public class Task extends DeletableEntity implements ProcessedData, Cloneable {

    private static final long serialVersionUID = -1414080814402194966L;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;


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
    @OneToMany (cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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

    public List<User> getExecutorsList() {
        if(executors != null) {
            return new ArrayList<>(executors);
        } else {
            return new ArrayList<>(0);
        }
    }

    public Set<User> getExecutors(){
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

    public void setParent(Task parent) {
        this.parent = parent;
    }

    public Task getParent() {
        return parent;
    }


    @Transient
    public String getUniqueId() {
        return getId() == null ? "" : "task_" + getId();
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
    }

    public Set<HistoryEntry> getHistory() {
        return history;
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


    /**
     *  Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     *  TODO сделать класс-обертку
     */
    @Transient
    private String styleClass;

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
        clone.setAuthor(author);
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
        String sb = "Task[" + getId() + "]:{" +
                "controlDate=" + ApplicationHelper.formatDate(controlDate) +
                ", creationDate=" + ApplicationHelper.formatDate(creationDate) +
                ", author=" + author.getDescription() +
                ", initiator=" + (initiator != null ? initiator.getDescription() : "null") +
                ", controller=" + (controller != null ? controller.getDescription() : "null") +
                ", parent=" + (parent != null ? parent.getId() : "null") +
                ", registrationDate=" + ApplicationHelper.formatDate(registrationDate) +
                ", shortDescription='" + shortDescription + '\'' +
                ", statusId=" + statusId +
                ", taskNumber='" + taskNumber + '\'' +
                ", rootDocumentId='" + rootDocumentId + '\'' +
                '}';
        return sb;
    }
}