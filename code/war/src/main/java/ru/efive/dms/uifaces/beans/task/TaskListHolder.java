package ru.efive.dms.uifaces.beans.task;

import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.entity.model.document.*;
import ru.entity.model.user.User;
import ru.util.ApplicationHelper;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.*;

@Named("tasks")
@SessionScoped
public class TaskListHolder extends AbstractDocumentListHolderBean<Task> {

    private static final long serialVersionUID = 4130764164049044408L;

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
                //user = sessionManagement.getDAO(UserDAOHibernate.class,USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());
                result = new ArrayList<Task>(new HashSet<Task>(sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findAllDocumentsByUser(filters, filter, user, false, false)));

                Collections.sort(result, new Comparator<Task>() {
                    public int compare(Task o1, Task o2) {
                        int result = 0;
                        String colId = getSorting().getColumnId();

                        if (colId.equalsIgnoreCase("task_number")) {
                            try {
                                Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getTaskNumber()));
                                Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getTaskNumber()));
                                result = i1.compareTo(i2);
                            } catch (NumberFormatException e) {
                                result = ApplicationHelper.getNotNull(o1.getTaskNumber()).compareTo(ApplicationHelper.getNotNull(o2.getTaskNumber()));
                            }
                        } else if (colId.equalsIgnoreCase("registration_date")) {
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
                            if (c1.equals(c2)) {
                                try {
                                    Integer i1 = Integer.parseInt(ApplicationHelper.getNotNull(o1.getTaskNumber()));
                                    Integer i2 = Integer.parseInt(ApplicationHelper.getNotNull(o2.getTaskNumber()));
                                    result = i1.compareTo(i2);
                                } catch (NumberFormatException e) {
                                    result = ApplicationHelper.getNotNull(o1.getTaskNumber()).compareTo(ApplicationHelper.getNotNull(o2.getTaskNumber()));
                                }
                            } else {
                                result = c1.compareTo(c2);
                            }
                        }

                        if (getSorting().isAsc()) {
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
        return new Sorting("registration_date", true);
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
            this.needRefresh = true;
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

    public List<Task> getDocumentsByParent(String parentId) {
        getDocuments();
        List<Task> result = new ArrayList<Task>();
        try {
            if (parentId != null && !parentId.equals("")) {
                result = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findResolutionsByParent(parentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Task> getDocumentsTreeByParent(String parentId) {
        List<Task> result = new ArrayList<Task>();
        try {
            if (parentId != null && !parentId.equals("")) {
                TaskDAOImpl dao = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO);
                List<Task> descendants = loadChildTree(dao, parentId, 0);
                if (descendants.size() > 0) result.addAll(descendants);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<Task> loadChildTree(TaskDAOImpl dao, String parentId, int level) throws Exception {
        List<Task> result = new ArrayList<Task>();
        List<Task> list = dao.findResolutionsByParent(parentId);
        for (Task entry : list) {
            entry.setGrouping(level);
            List<Task> descendants = loadChildTree(dao, entry.getUniqueId(), level + 1);
            result.add(entry);
            if (descendants.size() > 0) {
                entry.setParent(true);
                result.addAll(descendants);
            }
        }
        return result;
    }

    public String getTopDocumentControllerByTaskDocument(Task task) {
        if (task != null) {
            String key = task.getParentId();
            if (key != null && !key.isEmpty()) {
                int pos = key.indexOf('_');
                if (pos != -1) {
                    String id = key.substring(pos + 1, key.length());
                    StringBuffer in_description = new StringBuffer("");

                    if (key.indexOf("incoming") != -1) {
                        IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return in_doc.getController().getDescriptionShort();
                    } else if (key.indexOf("outgoing") != -1) {
                        OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return out_doc.getSigner().getDescriptionShort();
                    } else if (key.indexOf("internal") != -1) {
                        InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return internal_doc.getSigner().getDescriptionShort();
                    } else if (key.indexOf("request") != -1) {
                        RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return request_doc.getController().getDescriptionShort();
                    } else if (key.indexOf("task") != -1) {
                        Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findDocumentById(id);
                        return getTopDocumentControllerByTaskDocument(task_doc);
                    } else {
                        return "";
                    }
                }
            } else {
                //return task.getAuthor().getDescriptionShort();
                return "";
            }
            //return task.getAuthor().getDescriptionShort();
            return "";
        } else {
            return "";
        }
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
    private Map<String, Object> filters = new HashMap<String, Object>();

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;


}