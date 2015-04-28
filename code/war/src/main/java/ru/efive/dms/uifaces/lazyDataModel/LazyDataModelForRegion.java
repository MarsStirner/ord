package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.dms.dao.RegionDAOImpl;
import ru.entity.model.document.Region;

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
        setRowCount((int) dao.countDocument(getFilter(), false));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.findDocuments(getFilter(), false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }
}
