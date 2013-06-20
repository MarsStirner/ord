package ru.efive.dms.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import ru.efive.sql.entity.IdentifiedEntity;
import ru.efive.sql.entity.enums.DocumentStatus;
import ru.efive.sql.entity.enums.DocumentType;
import ru.efive.sql.entity.user.User;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.wf.core.ProcessedData;

/**
 * Поручение
 *
 * @author Alexey Vagizov
 */
@Entity
@Table(name = "dms_tasks")
public class Task extends IdentifiedEntity implements ProcessedData {

    private static final long serialVersionUID = -1414080814402194966L;

    /**
     * Номер поручения
     */
    private String taskNumber;

    /**
     * Дата создания
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date creationDate;

    /**
     * Дата регистрации
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date registrationDate;

    /**
     * Контрольная дата исполнения
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date controlDate;

    /**
     * Дата создания документа
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date executionDate;

    /**
     * Автор поручения
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_tasks_authors")
    private User author;


    /**
     * Инициатор
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_tasks_initiators")
    private User initiator;

    /**
     * Контролер
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_tasks_controllers")
    private User controller;

    /**
     * Исполнитель поручения
     */
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinTable(name = "dms_tasks_executors")
    private User executor;

    /**
     * Краткое описание
     */
    @Column(columnDefinition = "text")
    private String shortDescription;

    /**
     * Зарегистрировано
     */
    private boolean registered;

    /**
     * На контроле
     */
    private boolean control;

    /**
     * Текущий статус документа в процессе
     */
    @Column(name = "status_id")
    private int statusId;

    /**
     * id родительского документа
     */
    private String parentId;

    /**
     * id корневого документа
     */
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
     * Значение для группировки
     */
    @Transient
    private int grouping = 100;

    /**
     * Есть ли документы - потомки
     */
    @Transient
    private boolean parent = false;

    /**
     * Удален ли документ
     */
    private boolean deleted;


    /**
     * История
     */
    @OneToMany
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    @JoinTable(name = "dms_task_history",
            joinColumns = {@JoinColumn(name = "task_id")},
            inverseJoinColumns = {@JoinColumn(name = "history_entry_id")})
    @LazyCollection(LazyCollectionOption.FALSE)
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

    public User getExecutor() {
        return executor;
    }

    public void setExecutor(User executor) {
        this.executor = executor;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isControl() {
        return control;
    }

    public void setControl(boolean control) {
        this.control = control;
    }

    public int getGrouping() {
        return grouping;
    }

    public void setGrouping(int grouping) {
        this.grouping = grouping;
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

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentId() {
        return parentId;
    }


    @Transient
    public String getUniqueId() {
        return getId() == 0 ? "" : "task_" + getId();
    }

    public void setParent(boolean parent) {
        this.parent = parent;
    }

    public boolean isParent() {
        return parent;
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

    public void setControlStringDate(String date) {
        this.date = date;
    }

    public String getControlStringDate() {
        return this.date;
    }

    @Transient
    private String date;

    @Transient
    public List<HistoryEntry> getHistoryList() {
        List<HistoryEntry> result = new ArrayList<HistoryEntry>();
        if (history != null) {
            result.addAll(history);
        }
        Collections.sort(result, new Comparator<HistoryEntry>() {
            public int compare(HistoryEntry o1, HistoryEntry o2) {
                Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                c1.setTime(o1.getCreated());
                Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                c2.setTime(o2.getCreated());
                return c1.compareTo(c2);
            }
        });
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

}