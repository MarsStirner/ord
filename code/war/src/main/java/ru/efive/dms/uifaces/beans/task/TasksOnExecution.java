package ru.efive.dms.uifaces.beans.task;

import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.*;
import ru.entity.model.user.User;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static ru.efive.dms.util.ApplicationDAONames.*;

@Named("tasks_on_execution")
@SessionScoped
public class TasksOnExecution extends AbstractDocumentListHolderBean<Task> {

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 100);
    }

    @Override
    protected int getTotalCount() {
        User user = sessionManagement.getLoggedUser();
        //user = sessionManagement.getDAO(UserDAOHibernate.class,USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());

        int in_result;
        in_result = new Long(sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).countAllDocumentsByUser(filter, sessionManagement.getLoggedUser(), false, false)).intValue();

        return in_result;
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate, taskNumber", false);
    }

    @Override
    protected List<Task> loadDocuments() {
        List<Task> result = new ArrayList<Task>();
        try {
            User user = sessionManagement.getLoggedUser();
            //user = sessionManagement.getDAO(UserDAOHibernate.class,USER_DAO).findByLoginAndPassword(user.getLogin(), user.getPassword());

            List<Task> list = new ArrayList<Task>();
            list = new ArrayList<Task>(new HashSet<Task>(sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findAllDocumentsOnExecutionByUser(filter, sessionManagement.getLoggedUser(),
                    false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc())));

            /*SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
               SimpleDateFormat ydf=new SimpleDateFormat("yyyy");
               SimpleDateFormat mdf=new SimpleDateFormat("MM");
               SimpleDateFormat ddf=new SimpleDateFormat("dd");
               Date in_currentDate=sdf.parse(sdf.format(java.util.Calendar.getInstance ().getTime()));
               String category1=null,category2=null,category3 = null;
               for (Task document: list) {
                   Date in_date=document.getControlDate();
                   //if(in_date!=null&&(in_date.compareTo(in_currentDate)>=0)){
                   //Date in_date=in_currentDate;
                       Task year=new Task();
                       year.setControlStringDate(ydf.format(in_date));
                       if((category1==null)||(!ydf.format(in_date).equals(category1))){
                           category1=ydf.format(in_date);
                           year.setId(0);
                           year.setGrouping(0);
                           result.add(year);
                           category2=null;
                           category3 = null;
                       }
                       Task month=new Task();
                       if((category2==null)||(!mdf.format(in_date).equals(category2))){
                           category2=mdf.format(in_date);
                           month.setId(0);
                           month.setGrouping(1);
                           month.setControlStringDate(MONTHS[Integer.parseInt(mdf.format(in_date))-1]);
                           result.add(month);
                           category3 = null;
                       }
                       Task day=new Task();
                       if((category3==null)||(!ddf.format(in_date).equals(category3))){
                           category3=ddf.format(in_date);
                           day.setId(0);
                           day.setGrouping(2);
                           day.setControlStringDate(ddf.format(in_date));
                           result.add(day);
                       }
                       //document.setId(3);
                       document.setControlStringDate(sdf.format(in_date));
                       result.add(document);}
               //}*/
            result = list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Task> getDocuments() {
        return super.getDocuments();
    }

    public List<Task> getDocumentsByParent(int parentId) {
        getDocuments();
        List<Task> result = new ArrayList<Task>();
        try {
            if (parentId != 0) {
                result = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findResolutionsByParent(parentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Task> getDocumentsTreeByParent(int parentId) {
        List<Task> result = new ArrayList<Task>();
        try {
            if (parentId != 0) {
                TaskDAOImpl dao = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO);
                List<Task> descendants = loadChildTree(dao, parentId, 0);
                if (descendants.size() > 0) result.addAll(descendants);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<Task> loadChildTree(TaskDAOImpl dao, int parentId, int level) throws Exception {
        List<Task> result = new ArrayList<Task>();
        List<Task> list = dao.findResolutionsByParent(parentId);
        for (Task entry : list) {
                         List<Task> descendants = loadChildTree(dao, entry.getId(), level + 1);
            result.add(entry);
            if (descendants.size() > 0) {
                              result.addAll(descendants);
            }
        }
        return result;
    }

    public String getTopDocumentControllerByTaskDocument(Task task) {
        if (task != null) {
            String key = task.getRootDocumentId();
            if (key != null && !key.isEmpty()) {
                int pos = key.indexOf('_');
                if (pos != -1) {
                    String id = key.substring(pos + 1, key.length());
                    if (key.contains("incoming")) {
                        IncomingDocument in_doc = sessionManagement.getDAO(IncomingDocumentDAOImpl.class, INCOMING_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return in_doc.getController().getDescriptionShort();
                    } else if (key.contains("outgoing")) {
                        OutgoingDocument out_doc = sessionManagement.getDAO(OutgoingDocumentDAOImpl.class, OUTGOING_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return out_doc.getSigner().getDescriptionShort();
                    } else if (key.contains("internal")) {
                        InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return internal_doc.getSigner().getDescriptionShort();
                    } else if (key.contains("request")) {
                        RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return request_doc.getController().getDescriptionShort();
                    } else if (key.contains("task")) {
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
        this.filter = filter;
    }

    private String filter;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final long serialVersionUID = 8535420074467871583L;
}