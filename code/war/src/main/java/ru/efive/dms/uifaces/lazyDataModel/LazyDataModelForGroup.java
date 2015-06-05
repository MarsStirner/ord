package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.entity.model.user.Group;
import ru.hitsl.sql.dao.user.GroupDAOHibernate;

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
        if(getRowCount() < first){
            first = 0;
        }
        return dao.findItems(getFilter(), false, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
    }
}