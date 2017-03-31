package ru.hitsl.sql.dao.interfaces.document;

import ru.entity.model.document.Task;
import ru.hitsl.sql.dao.interfaces.mapped.DocumentDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:34 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface TaskDao extends DocumentDao<Task> {
    boolean isAccessGrantedByAssociation(AuthorizationData auth, String docKey);

    List<Task> getTaskListByRootDocumentId(String rootId, boolean showDeleted);

    List<Task> getChildrenTaskByParentId(int parentId, boolean showDeleted);
}
