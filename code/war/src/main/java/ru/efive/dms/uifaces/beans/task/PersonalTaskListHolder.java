package ru.efive.dms.uifaces.beans.task;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.data.Task;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("personal_tasks")
@SessionScoped
public class PersonalTaskListHolder extends AbstractDocumentListHolderBean<Task> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate, taskNumber", false);
    }

    @Override
    protected int getTotalCount() {
        return new Long(sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).countDraftDocumentsByAuthor(
                sessionManagement.getLoggedUser(), false)).intValue();
    }

    @Override
    protected List<Task> loadDocuments() {
        return sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findDraftDocumentsByAuthor(filter, sessionManagement.getLoggedUser(),
                false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
    }

    @Override
    public List<Task> getDocuments() {
        return super.getDocuments();
    }

    public List<Task> getDocumentsByParent(String parentId) {
        getDocuments();
        List<Task> result = new ArrayList<Task>();
        try {
            if (parentId != null && !parentId.equals("")) {
                result = sessionManagement.getDAO(TaskDAOImpl.class, ru.efive.dms.util.ApplicationHelper.TASK_DAO).findResolutionsByParent(
                        sessionManagement.getLoggedUser().getId(), parentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    private String filter;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}