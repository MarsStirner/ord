package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.sql.dao.user.GroupDAOHibernate;
import ru.entity.model.user.Group;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 17.04.2015, 17:37 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForGroup extends AbstractFilterableLazyDataModel<Group> {

    private GroupDAOHibernate dao;

    public LazyDataModelForGroup(GroupDAOHibernate daoHibernate) {
        dao = daoHibernate;
    }

    @Override
    public Group getRowData(String rowKey) {
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            return null;
        }
        return dao.getItemByIdForListView(identifier);
    }

    @Override
    public List<Group> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        setRowCount(dao.countItems(getFilter(), false));
        return dao.findItems(getFilter(), false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }
}