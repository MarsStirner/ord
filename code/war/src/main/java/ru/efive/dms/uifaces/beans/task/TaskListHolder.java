package ru.efive.dms.uifaces.beans.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.efive.dms.uifaces.beans.SessionManagementBean;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.efive.dms.uifaces.lazyDataModel.tasks.LazyDataModelForTask;
import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.TaskDAOImpl;
import ru.hitsl.sql.dao.ViewFactDaoImpl;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static ru.hitsl.sql.dao.util.ApplicationDAONames.TASK_DAO;
import static ru.hitsl.sql.dao.util.ApplicationDAONames.VIEW_FACT_DAO;
import static ru.hitsl.sql.dao.util.DocumentSearchMapKeys.STATUS_LIST_KEY;

@Named("tasks")
@ViewScoped
public class TaskListHolder extends AbstractDocumentLazyDataModelBean<Task> {

    private TaskDAOImpl dao;
    private ViewFactDaoImpl viewFactDao;
    @Inject
    @Named("sessionManagement")
    private transient SessionManagementBean sessionManagement;

    private static final Logger logger = LoggerFactory.getLogger("TASK");
    private Map<String, Object> filters = new HashMap<String, Object>();

    @PostConstruct
    public void init() {
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
                                if (STATUS_LIST_KEY.equals(entry.getKey())) {
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
        dao = sessionManagement.getDAO(TaskDAOImpl.class, TASK_DAO);
        viewFactDao = sessionManagement.getDAO(ViewFactDaoImpl.class, VIEW_FACT_DAO);

        final LazyDataModelForTask lazyDataModel = new LazyDataModelForTask(dao, viewFactDao, sessionManagement.getAuthData());
        lazyDataModel.setFilters(filters);
        setLazyModel(lazyDataModel);
    }
}
