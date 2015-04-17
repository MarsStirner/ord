package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.sql.dao.user.UserDAOHibernate;
import ru.entity.model.user.User;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForUser extends AbstractFilterableLazyDataModel<User> {
    private UserDAOHibernate dao;
    private boolean showFired = false;

    public boolean isShowFired() {
        return showFired;
    }

    public void setShowFired(final boolean showFired) {
        this.showFired = showFired;
    }

    public LazyDataModelForUser(UserDAOHibernate daoHibernate) {
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
        setRowCount(((Long) dao.countUsers(getFilter(), false, showFired)).intValue());
        return dao.findUsers(getFilter(), false, showFired, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }

}
