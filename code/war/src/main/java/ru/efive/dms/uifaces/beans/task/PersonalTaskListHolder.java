package ru.efive.dms.uifaces.beans.task;

import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.tasks.LazyDataModelForPersonalDraftsTask;
import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.TaskDAOImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.TASK_DAO;

@Named("personal_tasks")
@SessionScoped
public class PersonalTaskListHolder extends AbstractDocumentLazyDataModelBean<Task> implements Serializable {

    private static final long serialVersionUID = 853542007446781235L;
    private TaskDAOImpl dao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    @PostConstruct
    public void init() {
        dao = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO);
        setLazyModel(new LazyDataModelForPersonalDraftsTask(dao, sessionManagement.getAuthData()));
    }
}
