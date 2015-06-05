package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.entity.model.document.ReportTemplate;
import ru.hitsl.sql.dao.ReportDAOImpl;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 18:57 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForReportTemplate extends AbstractFilterableLazyDataModel<ReportTemplate>{

    private final ReportDAOImpl dao;

    public LazyDataModelForReportTemplate(final ReportDAOImpl dao) {
        this.dao = dao;
    }
    @Override
    public ReportTemplate getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<ReportTemplate> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,
            Object> filters) {
        setRowCount((int) dao.countDocument(false));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.findDocuments(false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }
}
