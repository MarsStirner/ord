package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.entity.model.user.Group;
import ru.entity.model.user.User;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForUserInDialogs extends AbstractFilterableLazyDataModel<User> {
    private UserDAOHibernate dao;
    private Group filterGroup;
    private boolean showFired = false;

    public boolean isShowFired() {
        return showFired;
    }

    public void setShowFired(final boolean showFired) {
        this.showFired = showFired;
    }

    public Group getFilterGroup() {
        return filterGroup;
    }

    public void setFilterGroup(Group filterGroup) {
        this.filterGroup = filterGroup;
    }

    public LazyDataModelForUserInDialogs(UserDAOHibernate daoHibernate) {
        dao = daoHibernate;
    }

    @Override
    public User getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<User> load(
            int first,
            int pageSize,
            String sortField,
            SortOrder sortOrder,
            Map<String, Object> filters
    ) {
        if (filterGroup == null) {
            setRowCount(((Long) dao.countUsersForDialog(getFilter(), false, showFired)).intValue());
            return dao.findUsersForDialog(getFilter(), false, showFired, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
        } else {
            setRowCount(((Long) dao.countUsersForDialogByGroup(getFilter(), false, showFired, filterGroup)).intValue());
            return dao.findUsersForDialogByGroup(getFilter(), false, showFired, filterGroup, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
        }
    }


}
