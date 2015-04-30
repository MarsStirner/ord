package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.dms.dao.OfficeKeepingFileDAOImpl;
import ru.efive.dms.util.security.AuthorizationData;
import ru.entity.model.document.OfficeKeepingFile;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 29.04.2015, 19:58 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForOfficeKeepingFile extends AbstractFilterableLazyDataModel<OfficeKeepingFile>{
    private OfficeKeepingFileDAOImpl dao;
    private AuthorizationData authData;

    public LazyDataModelForOfficeKeepingFile(OfficeKeepingFileDAOImpl daoHibernate, AuthorizationData authData) {
        dao = daoHibernate;
        this.authData = authData;
    }

    @Override
    public OfficeKeepingFile getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<OfficeKeepingFile> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        setRowCount((((Long) dao.countDocument(getFilter(), false)).intValue()));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.findDocuments(getFilter(), false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }
}
