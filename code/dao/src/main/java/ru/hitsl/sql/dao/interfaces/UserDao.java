package ru.hitsl.sql.dao.interfaces;

import org.hibernate.criterion.DetachedCriteria;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:31 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface UserDao extends CommonDao<User> {
    void applyFilterForDialogs(DetachedCriteria criteria, String filter);

    User findByLoginAndPassword(String login, String password);

    User getByLogin(String login);

    List<User> getItems(
            String filter,
            boolean showDeleted,
            boolean showFired,
            int first,
            int pageSize,
            String orderBy,
            boolean orderAsc
    );

    int countItems(String filter, boolean showDeleted, boolean showFired);

    List<User> getItemsForDialog(
            String filter,
            boolean showDeleted,
            boolean showFired,
            int first,
            int pageSize,
            String orderBy,
            boolean orderAsc
    );

    int countItemsForDialog(String filter, boolean showDeleted, boolean showFired);

    List<User> getItemsForDialogByGroup(
            String filter,
            boolean showDeleted,
            boolean showFired,
            Group group,
            int first,
            int pageSize,
            String orderBy,
            boolean orderAsc
    );

    int countItemsForDialogByGroup(String filter, boolean showDeleted, boolean showFired, Group group);
}
