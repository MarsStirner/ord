package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.entity.model.user.Substitution;
import ru.hitsl.sql.dao.SubstitutionDaoImpl;

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
        setRowCount(dao.getDocumentsCount(filter, false));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.getDocuments(filter, false, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
    }
}
