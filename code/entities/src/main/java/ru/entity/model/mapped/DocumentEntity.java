package ru.entity.model.mapped;


import ru.entity.model.workflow.HistoryEntry;
import ru.entity.model.referenceBook.DocumentType;
import ru.entity.model.referenceBook.DocumentForm;
import ru.entity.model.user.User;
import ru.entity.model.workflow.Status;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Базовый класс - документ
 */
@MappedSuperclass
@Access(AccessType.FIELD)
public abstract class DocumentEntity extends DeletableEntity {
    /**
     * Дата создания документа
     */
    @Column(name = "creationDate", nullable = false)
    protected LocalDateTime creationDate;

    /**
     * Автор документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    protected User author;

    /**
     * Руководитель
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "controller_id", nullable = true)
    protected User controller;

    /**
     * Номер входящего
     */
    @Column(name = "registrationNumber")
    protected String registrationNumber;

    /**
     * Дата регистрации
     */
    @Column(name = "registrationDate")
    protected LocalDateTime registrationDate;

    /**
     * Краткое описание
     */
    @Column(name = "shortDescription", columnDefinition = "text")
    protected String shortDescription;

    /**
     * Текущий статус документа в процессе
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    protected Status status;

    /**
     * Вид документа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    protected DocumentForm form;

    /**
     * История
     * XXX: @AssociationOverride
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    protected Set<HistoryEntry> history;


    /**
     * Поле, в котором предполагается сохранять имя css - класса, для вывода в списках
     */
    @Transient
    protected String styleClass;

    public abstract DocumentType getType();

    public List<HistoryEntry> getHistoryList() {
        List<HistoryEntry> result = new ArrayList<>();
        if (history != null) {
            result.addAll(history);
        }
        Collections.sort(result);
        return result;
    }

    public String getUniqueId() {
        return getType().getCode() + "_" + id;
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

    @Override
    public String toString() {
        return getType().getCode() + "[" + getId() + "]";
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public Set<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(Set<HistoryEntry> history) {
        this.history = history;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public User getController() {
        return controller;
    }

    public void setController(User controller) {
        this.controller = controller;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public DocumentForm getForm() {
        return form;
    }

    public void setForm(DocumentForm form) {
        this.form = form;
    }

}