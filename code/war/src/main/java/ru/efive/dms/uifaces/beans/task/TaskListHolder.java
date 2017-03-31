package ru.efive.dms.uifaces.beans.task;

import com.github.javaplugs.jsf.SpringScopeView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentLazyDataModelBean;
import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import org.springframework.stereotype.Controller;
import java.util.*;

import static ru.hitsl.sql.dao.util.DocumentSearchMapKeys.STATUS_LIST_KEY;

@Controller("tasks")
@SpringScopeView
public class TaskListHolder extends AbstractDocumentLazyDataModelBean<Task> {

    private static final Logger logger = LoggerFactory.getLogger("TASK");
    @Autowired
    @Qualifier("taskDao")
    private TaskDao taskDao;
    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;
    private Map<String, Object> filters = new HashMap<>();

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
                                final ArrayList<Integer> ints = new ArrayList<>(strings.size());
                                for (String string : strings) {
                                    ints.add(Integer.valueOf(string));
                                }
                                filters.put(entry.getKey(), ints);
                            } else {
                                filters.put(entry.getKey(), new ArrayList<>(strings));
                            }
                        }
                    } else {
                        //Одиночное значение
                        filters.put(entry.getKey(), value);
                    }
                }
            }
        }


        //TODO
    }
}
