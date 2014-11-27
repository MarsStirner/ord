package ru.efive.dms.uifaces.beans.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.dao.*;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.uifaces.bean.AbstractDocumentListHolderBean;
import ru.efive.uifaces.bean.Pagination;
import ru.entity.model.document.*;
import ru.entity.model.user.User;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.efive.dms.util.ApplicationDAONames.*;

@ManagedBean(name = "tasks")
@ViewScoped
public class TaskListHolder extends AbstractDocumentListHolderBean<Task> {

    private static final long serialVersionUID = 4130764164049044408L;


    private static final Logger logger = LoggerFactory.getLogger("TASK");
    private Map<String, Object> filters = new HashMap<String, Object>();
    private String filter;

    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;


    @PostConstruct
    /**
     * При каждом запросе страницы (нового view) инициализировать список фильтров
     */
    public void initTaskDocumentList() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            final Map<String, String> parameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            if (!parameterMap.isEmpty()) {
                logger.info("List initialize with {} params", parameterMap.size());
                filters.clear();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    final String value = entry.getValue();
                    logger.info("{} = {}", entry.getKey(), value);
                    if (value.startsWith("{") && value.endsWith("}")) {
                        // Список
                        final List<String> strings = Arrays.asList(value.substring(1, value.length() - 1).split("\\s*,\\s*"));
                        if (!strings.isEmpty()) {
                            //Для некоторых парметров надо приводить типы
                            if ("statusesId".equals(entry.getKey())) {
                                final ArrayList<Integer> ints = new ArrayList<Integer>(strings.size());
                                for (String string : strings) {
                                    ints.add(Integer.valueOf(string));
                                }
                                filters.put(entry.getKey(), ints);
                            } else {
                                filters.put(entry.getKey(), new ArrayList<String>(strings));
                            }
                        }
                    } else {
                        //Одиночное значение
                        filters.put(entry.getKey(), value);
                    }
                }
            }
        }
    }

    @Override
    protected Pagination initPagination() {
        return new Pagination(0, getTotalCount(), 50);
        //Ранжирование по страницам по-умолчанию  (50 на страницу, начиная с нуля)
    }

    @Override
    protected Sorting initSorting() {
        return new Sorting("registrationDate", true);
        //Сортировка по-умолчанию (дата регистрации документа)
    }

    @Override
    protected int getTotalCount() {
        if (!sessionManagement.isSubstitution()) {
            //Без замещения
            return new Long(
                    sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO)
                            .countAllDocumentsByUser(filters, filter, sessionManagement.getLoggedUser(), false, false)
            ).intValue();
        } else {
            // С замещением
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            return new Long(
                    sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO)
                            .countAllDocumentsByUserList(filters, filter, userList, false, false)
            ).intValue();
        }
    }


    @Override
    protected List<Task> loadDocuments() {
        if (!sessionManagement.isSubstitution()) {
            // Без замещения
            return sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO)
                    .findAllDocumentsByUser(filters, filter, sessionManagement.getLoggedUser(), false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        } else {
            // С замещением
            final List<User> userList = new ArrayList<User>(sessionManagement.getSubstitutedUsers());
            userList.add(sessionManagement.getLoggedUser());
            return sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO)
                    .findAllDocumentsByUserList(filters, filter, userList, false, false, getPagination().getOffset(), getPagination().getPageSize(), getSorting().getColumnId(), getSorting().isAsc());
        }
    }


    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
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
                        return out_doc.getController().getDescriptionShort();
                    } else if (key.contains("internal")) {
                        InternalDocument internal_doc = sessionManagement.getDAO(InternalDocumentDAOImpl.class, INTERNAL_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return internal_doc.getSigner().getDescriptionShort();
                    } else if (key.contains("request")) {
                        RequestDocument request_doc = sessionManagement.getDAO(RequestDocumentDAOImpl.class, REQUEST_DOCUMENT_FORM_DAO).findDocumentById(id);
                        return request_doc.getController().getDescriptionShort();
                    } else if (key.contains("task")) {
                        Task task_doc = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO).findDocumentById(id);
                        return getTopDocumentControllerByTaskDocument(task_doc);
                    }
                }
            }
        }
        return "";
    }
}