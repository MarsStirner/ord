package ru.efive.dms.uifaces.lazyDataModel.tasks;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.impl.document.TaskDaoImpl;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 09.04.2015, 18:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@Component("personalTaskLDM")
@SpringScopeView
public class LazyDataModelForPersonalDraftsTask extends AbstractFilterableLazyDataModel<Task> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_TASK");

    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    /**
     * Создает модель для заданного пользователя
     *
     * @param taskDao доступ к БД
     */
    @Autowired
    public LazyDataModelForPersonalDraftsTask(
            @Qualifier("taskDao") final TaskDaoImpl taskDao) {
        super(taskDao);
    }

    @Override
    public List<Task> load(
            int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {
        TaskDao dao = (TaskDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(dao.countPersonalDraftDocumentListByFilters(authData, getFilter()));
            if (getRowCount() < first) {
                first = 0;
            }
            return dao.getPersonalDraftDocumentListByFilters(
                    authData, getFilter(), sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize
            );
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }

}