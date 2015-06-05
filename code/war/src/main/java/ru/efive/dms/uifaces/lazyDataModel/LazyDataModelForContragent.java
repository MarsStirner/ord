package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.entity.model.referenceBook.Contragent;
import ru.hitsl.sql.dao.ContragentDAOHibernate;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForContragent extends AbstractFilterableLazyDataModel<Contragent> {

    private ContragentDAOHibernate dao;

    public LazyDataModelForContragent(ContragentDAOHibernate daoHibernate) {
        dao = daoHibernate;
    }

    @Override
    public Contragent getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<Contragent> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object>
            filters) {
        setRowCount(dao.countItems(filter, false));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.findItems(filter, false, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
    }
}
