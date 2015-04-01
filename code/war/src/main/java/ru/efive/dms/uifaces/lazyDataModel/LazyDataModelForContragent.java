package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.crm.dao.ContragentDAOHibernate;
import ru.entity.model.crm.Contragent;

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
        return dao.findDocuments(getFilter(), false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }

    @Override
    public int getRowCount() {
        return (((Long) dao.countDocument(getFilter(), false)).intValue());
    }

}
