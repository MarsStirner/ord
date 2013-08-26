package ru.efive.dms.uifaces.beans.task;

import java.util.*;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import ru.efive.dms.dao.TaskDAOImpl;
import ru.efive.dms.data.Task;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.util.ApplicationHelper;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.efive.sql.entity.user.User;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;

@Named("petitions")
@SessionScoped
public class TasksEqPetitionHolder extends AbstractDocumentListHolderBean<Task> {

    /**
     *
     */
    private static final long serialVersionUID = 5710656604431628331L;

    protected List<Task> getHashDocuments(int fromIndex, int toIndex) {
        toIndex = (this.getHashDocuments().size() < fromIndex + toIndex) ? this.getHashDocuments().size() : fromIndex + toIndex;
        List<Task> result = new ArrayList<Task>(this.getHashDocuments().subList(fromIndex, toIndex));
        return result;
    }

    protected List<Task> getHashDocuments() {
        List<Task> result = new ArrayList<Task>();
        if (needRefresh) {
            try {
                User user = sessionManagement.getLoggedUser();
                user = sessionManagement.getDAO(UserDAOHibernate.class, ApplicationHelper.USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<Task>(new HashSet<Task>(sessionManagement.getDAO(TaskDAOImpl.class, ApplicationHelper.TASK_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(result, new Comparator<Task>() {
                    public int compare(Task o1, Task o2) {
                        int result = 0;
                        String colId = ApplicationHelper.getNotNull(getSorting().getColumnId());

                        if(colId.equalsIgnoreCase("registrationDate")) {
                            Date d1 = ApplicationHelper.getNotNull(o1.getRegistrationDate());
                            Calendar c1 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c1.setTime(d1);
                            c1.set(Calendar.HOUR_OF_DAY, 0);
                            c1.set(Calendar.MINUTE, 0);
                            c1.set(Calendar.SECOND, 0);
                            Date d2 = ApplicationHelper.getNotNull(o2.getRegistrationDate());
                            Calendar c2 = Calendar.getInstance(ApplicationHelper.getLocale());
                            c2.setTime(d2);
                            c2.set(Calendar.HOUR_OF_DAY, 0);
                            c2.set(Calendar.MINUTE, 0);
                            c2.set(Calendar.SECOND, 0);
                            result = c1.compareTo(c2);
                        }

                        if(getSorting().isAsc()) {
                            result *= -1;
                        }
                        return result;
                    }
                });

                this.hashDocuments = result;
                needRefresh = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = this.hashDocuments;
            //needRefresh=false;
        }
        return result;
    }

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate", true);
    }

    @Override
    protected int getTotalCount() {
        int result = 0;
        try {
            result = new Long(this.getHashDocuments().size()).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected List<Task> loadDocuments() {
        List<Task> result = new ArrayList<Task>();
        try {
            result = this.getHashDocuments(getPagination().getOffset(), getPagination().getPageSize());


        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void refresh() {
        this.needRefresh = true;
        super.refresh();
    }

    @Override
    public List<Task> getDocuments() {
        String key = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("key");
        if (key != null && !key.isEmpty()) {
            if (!filters.containsKey(key)) {
                this.needRefresh = true;
                markNeedRefresh();
                this.filters.clear();
                String value = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("value");
                this.filters.put(key, value);
            }
        }
        return super.getDocuments();
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.needRefresh = true;
        this.filter = filter;
    }

    private List<Task> hashDocuments;
    private boolean needRefresh = true;

    private String filter;
    static private Map<String, Object> filters = new HashMap<String, Object>();

    static {
        filters.put("exerciseTypeValue", "Заявление");
        filters.put("exerciseTypeCategory", "Задачи");
    }

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

}