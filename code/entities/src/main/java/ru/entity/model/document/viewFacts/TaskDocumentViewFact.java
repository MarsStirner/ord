package ru.entity.model.document.viewFacts;

import ru.entity.model.document.Task;
import ru.entity.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_documents_views")
public class TaskDocumentViewFact extends DocumentViewFact<Task>{

    public TaskDocumentViewFact(Task document, User user, LocalDateTime date) {
        super(document, user, date);
    }
}