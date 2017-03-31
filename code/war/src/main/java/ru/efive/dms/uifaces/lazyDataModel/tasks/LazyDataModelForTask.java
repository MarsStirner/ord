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
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 09.04.2015, 18:04 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Component("taskLDM")
@SpringScopeView
public class LazyDataModelForTask extends AbstractFilterableLazyDataModel<Task> {
    private static final Logger logger = LoggerFactory.getLogger("LAZY_DM_TASK");

    @Autowired
    @Qualifier("viewFactDao")
    private ViewFactDao viewFactDao;
    //Авторизационные данные пользователя
    @Autowired
    @Qualifier("authData")
    private AuthorizationData authData;

    @Autowired
    public LazyDataModelForTask(
            @Qualifier("taskDao") TaskDao taskDao) {
        super(taskDao);
    }

    @Override
    public List<Task> load(
            int first, final int pageSize, final String sortField, final SortOrder sortOrder, final Map<String, Object> filters
    ) {
        final TaskDao taskDao = (TaskDao) this.dao;
        //Используются фильтры извне, а не из параметров
        if (authData != null) {
            setRowCount(taskDao.countItems(authData, filter, getFilters(), false, false));
            if (getRowCount() < first) {
                first = 0;
            }
            final List<Task> resultList = taskDao.getItems(
                    authData, filter, getFilters(), sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize, false, false
            );
            //Проверка и выставленние классов просмотра документов пользователем
            if (!resultList.isEmpty()) {
                viewFactDao.applyViewFlagsOnTaskDocumentList(resultList, authData.getAuthorized());
            }
            return resultList;
        } else {
            logger.error("NO AUTH DATA");
            return null;
        }
    }

    @Override
    public Task getRowData(String rowKey) {
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            logger.error("Try to get Item by nonInteger identifier \'{}\'. Return NULL", rowKey);
            return null;
        }
        return dao.getItemByListCriteria(identifier);
    }

}
