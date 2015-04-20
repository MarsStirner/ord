package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.sql.dao.user.RoleDAOHibernate;
import ru.entity.model.user.Role;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 17:16 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForRole extends AbstractFilterableLazyDataModel<Role> {

    private final RoleDAOHibernate dao;

    public LazyDataModelForRole(final RoleDAOHibernate dao) {
        this.dao = dao;
    }

    @Override
    public List<Role> load(
            int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters
    ) {
        setRowCount((int) dao.countRoles());
        return dao.findRoles(first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
    }

    @Override
    public Role getRowData(String rowKey) {
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            return null;
        }
        return dao.get(identifier);
    }

}
