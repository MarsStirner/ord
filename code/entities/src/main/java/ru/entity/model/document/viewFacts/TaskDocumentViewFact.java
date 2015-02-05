package ru.entity.model.document.viewFacts;

import ru.entity.model.document.Task;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "dms_task_documents_views")
@IdClass(TaskDocumentViewFactPrimaryKey.class)
public class TaskDocumentViewFact {

    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    private Task document;

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewDateTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date viewDateTime;

    public TaskDocumentViewFact() {
    }

    public TaskDocumentViewFact(Task document, User user, Date viewDateTime) {
        this.document = document;
        this.user = user;
        this.viewDateTime = viewDateTime;
    }

    public Task getDocument() {
        return document;
    }

    public void setDocument(Task document) {
        this.document = document;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getViewDateTime() {
        return viewDateTime;
    }

    public void setViewDateTime(Date viewDateTime) {
        this.viewDateTime = viewDateTime;
    }

}