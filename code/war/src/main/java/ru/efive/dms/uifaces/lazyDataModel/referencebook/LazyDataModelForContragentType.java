package ru.efive.dms.uifaces.lazyDataModel.referencebook;

import org.primefaces.model.SortOrder;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.referenceBook.ContragentTypeDAOImpl;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 16:00 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForContragentType extends AbstractFilterableLazyDataModel<ContragentType> {

    private ContragentTypeDAOImpl dao;

    public LazyDataModelForContragentType(final ContragentTypeDAOImpl daoHibernate) {
        dao = daoHibernate;
    }

    @Override
    public ContragentType getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<ContragentType> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object>
            filters) {
        setRowCount(dao.countItems(filter));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.getItems(filter, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
    }
}