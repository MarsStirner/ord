package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.hitsl.sql.dao.OfficeKeepingVolumeDAOImpl;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 12.03.2015, 17:28 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForOfficeKeepingVolume extends AbstractFilterableLazyDataModel<OfficeKeepingVolume> {
    private OfficeKeepingVolumeDAOImpl dao;

    public LazyDataModelForOfficeKeepingVolume(OfficeKeepingVolumeDAOImpl daoHibernate) {
        dao = daoHibernate;
    }

    @Override
    public OfficeKeepingVolume getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<OfficeKeepingVolume> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,
            Object> filters) {
        setRowCount((((Long) dao.countDocument(filter, false)).intValue()));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.findDocuments(filter, false, first, pageSize, sortField,SortOrder.ASCENDING.equals(sortOrder));
    }
}
