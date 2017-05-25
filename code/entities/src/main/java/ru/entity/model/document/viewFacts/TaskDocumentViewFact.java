package ru.entity.model.document.viewFacts;

import ru.entity.model.document.OutgoingDocument;
import ru.entity.model.document.Task;
import ru.entity.model.document.viewFacts.composite_pk.TaskDocumentViewFactPrimaryKey;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_views")
@IdClass(TaskDocumentViewFactPrimaryKey.class)
public class TaskDocumentViewFact implements DocumentViewFact<Task>{
    @Id
    @ManyToOne
    @JoinColumn(name = "document_id")
    private Task document;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewDateTime")
    private LocalDateTime viewDateTime;

    @Override
    public Task getDocument() {
        return document;
    }

    @Override
    public void setDocument(Task document) {
        this.document = document;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public LocalDateTime getViewDateTime() {
        return viewDateTime;
    }

    @Override
    public void setViewDateTime(LocalDateTime viewDateTime) {
        this.viewDateTime = viewDateTime;
    }
}
