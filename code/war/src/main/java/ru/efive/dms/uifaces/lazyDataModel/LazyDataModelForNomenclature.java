package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.dms.dao.NomenclatureDAOImpl;
import ru.entity.model.document.Nomenclature;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 18:32 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForNomenclature extends AbstractFilterableLazyDataModel<Nomenclature> {
    private final NomenclatureDAOImpl dao;

    public LazyDataModelForNomenclature(final NomenclatureDAOImpl dao) {
        this.dao = dao;
    }

    @Override
    public Nomenclature getRowData(String rowKey) {
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            return null;
        }
        return dao.get(identifier);
    }

    @Override
    public List<Nomenclature> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        setRowCount((int) dao.countDocument(filter, false));
        if(getRowCount() < first){
            first = 0;
        }
        return dao.findDocuments(filter, false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }
}
