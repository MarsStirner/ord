package ru.efive.dms.uifaces.beans.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentableLazyDataModel;
import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.interfaces.ViewFactDao;
import ru.hitsl.sql.dao.interfaces.document.TaskDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 09.04.2015, 18:04 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("taskLDM")
public class TaskLazyDataModel extends AbstractDocumentableLazyDataModel<Task> {
    @Autowired
    public TaskLazyDataModel(
            @Qualifier("taskDao") TaskDao taskDao,
            @Qualifier("authData") AuthorizationData authData,
            @Qualifier("viewFactDao") ViewFactDao viewFactDao) {
        super(taskDao, authData, viewFactDao);
    }
}
