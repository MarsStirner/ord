package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.entity.model.referenceBook.Region;
import ru.hitsl.sql.dao.referenceBook.RegionDAOImpl;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 28.04.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForRegion extends AbstractFilterableLazyDataModel<Region>{

    private final RegionDAOImpl dao;

    public LazyDataModelForRegion(final RegionDAOImpl dao) {
        this.dao = dao;
    }
    @Override
    public Region getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<Region> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        setRowCount(dao.countItems(filter));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.getItems(filter, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
    }
}
