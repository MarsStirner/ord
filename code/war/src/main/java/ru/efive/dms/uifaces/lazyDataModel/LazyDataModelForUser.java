package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.user.UserDAOHibernate;

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
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            return null;
        }
        return dao.getItemByIdForListView(identifier);
    }

    @Override
    public List<User> load(
            int first,
            int pageSize,
            String sortField,
            SortOrder sortOrder,
            Map<String, Object> filters
    ) {
        setRowCount(((Long) dao.countUsers(filter, false, showFired)).intValue());
        if(getRowCount() < first){
            first = 0;
        }
        return dao.findUsers(filter, false, showFired, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
    }

}
