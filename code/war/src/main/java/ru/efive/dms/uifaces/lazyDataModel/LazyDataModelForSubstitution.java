package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.dms.dao.ejb.SubstitutionDaoImpl;
import ru.entity.model.user.Substitution;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 17.11.2014, 19:56 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForSubstitution extends AbstractFilterableLazyDataModel<Substitution> {
    private SubstitutionDaoImpl dao;

    public LazyDataModelForSubstitution(SubstitutionDaoImpl dao) {
        this.dao = dao;
    }

    @Override
    public Substitution getRowData(String rowKey) {
        return dao.get(Integer.valueOf(rowKey));
    }

    @Override
    public List<Substitution> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,
            Object> filters) {
        setRowCount(dao.getDocumentsCount(getFilter(), false));
        if(getRowCount() < first){
            first = 0;
        }
        final List<Substitution> result = dao.getDocuments(getFilter(), false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
        return result;
    }
}
