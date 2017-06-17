package ru.efive.dms.uifaces.beans.abstractBean;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import ru.entity.model.mapped.DeletableEntity;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 31.03.2015, 16:46 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class AbstractFilterableLazyDataModel<T extends DeletableEntity> extends LazyDataModel<T> implements Serializable {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final CommonDao<T> dao;
    protected String filter;
    protected Map<String, Object> filters;

    public AbstractFilterableLazyDataModel(final CommonDao<T> dao) {
        this.dao = dao;
    }

    public void clearFilters() {
        filter = null;
        filters = null;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    public void setFilters(final Map<String, Object> filters) {
        this.filters = filters;
    }


    public void addFilter(final String key, final Object value) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        if (value instanceof String && ((String) value).startsWith("{") && ((String) value).endsWith("}")) {
            // Список
            final List<String> strings = Arrays.asList(((String) value).substring(1, ((String) value).length() - 1).split("\\s*,\\s*"));
            filters.put(key, strings);
        } else {
            //Одиночное значение
            filters.put(key, value);
        }
    }

    @Override
    @Transactional("ordTransactionManager")
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> unusedPrimefacesFilters) {
        setRowCount(dao.countItems(filter, filters, false));
        if (getRowCount() < first) {
            first = 0;
        }
        return dao.getItems(filter, filters, sortField, SortOrder.ASCENDING.equals(sortOrder), first, pageSize, false);
    }

    @Override
    @Transactional(value = "ordTransactionManager", readOnly = true)
    public T getRowData(String rowKey) {
        try {
            return dao.getItemByListCriteria(Integer.valueOf(rowKey));
        } catch (NumberFormatException e) {
            log.error("Try to get Item by non-integer identifier \'{}\'. Return NULL", rowKey);
            return null;
        }
    }

    @Override
    public Object getRowKey(T item) {
        return item.getId();
    }
}
