package ru.efive.dms.uifaces.beans.user;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
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
public class LazyDataModelForUser extends LazyDataModel<User> implements SelectableDataModel<User> {
    private UserDAOHibernate dao;
    private String filter;

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public LazyDataModelForUser(UserDAOHibernate daoHibernate) {
        dao = daoHibernate;
    }

    @Override
    public Object getRowKey(User item) {
        return item.getId();
    }

    @Override
    public User getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<User> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        return dao.findUsers(
                filter,
                false, false,
                first, pageSize,
                sortField, sortOrder == SortOrder.ASCENDING
        );
    }

    @Override
    public int getRowCount(){
       return (((Long) dao.countUsers(filter, false, false)).intValue());
    }

}
