package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import ru.efive.dms.dao.OfficeKeepingVolumeDAOImpl;
import ru.entity.model.document.OfficeKeepingVolume;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 12.03.2015, 17:28 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForOfficeKeepingVolume extends LazyDataModel<OfficeKeepingVolume> {
    private OfficeKeepingVolumeDAOImpl dao;
    private String filter;

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    public LazyDataModelForOfficeKeepingVolume(OfficeKeepingVolumeDAOImpl daoHibernate) {
        dao = daoHibernate;
    }

    @Override
    public Object getRowKey(OfficeKeepingVolume item) {
        return item.getId();
    }

    @Override
    public OfficeKeepingVolume getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<OfficeKeepingVolume> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,
            Object> filters) {
        return dao.findDocuments(filter, false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }

    @Override
    public int getRowCount() {
        return (((Long) dao.countDocument(filter, false)).intValue());
    }
}
