package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.SortOrder;
import ru.efive.dms.dao.NumeratorDAOImpl;
import ru.entity.model.document.Numerator;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 18:05 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public class LazyDataModelForNumerator extends AbstractFilterableLazyDataModel<Numerator> {
    private final NumeratorDAOImpl dao;

    public LazyDataModelForNumerator(final NumeratorDAOImpl dao) {
        this.dao = dao;
    }

    @Override
    public Numerator getRowData(String rowKey) {
        final Integer identifier;
        try {
            identifier = Integer.valueOf(rowKey);
        } catch (NumberFormatException e) {
            return null;
        }
        return dao.get(identifier);
    }

    @Override
    public List<Numerator> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        setRowCount((int) dao.countDocument(false));
        return dao.findDocuments(false, first, pageSize, sortField, sortOrder == SortOrder.ASCENDING);
    }
}
