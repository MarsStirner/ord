package ru.efive.dms.uifaces.lazyDataModel;

import org.primefaces.model.LazyDataModel;
import ru.entity.model.mapped.IdentifiedEntity;

/**
 * Author: Upatov Egor <br>
 * Date: 31.03.2015, 16:46 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class AbstractFilterableLazyDataModel<T extends IdentifiedEntity> extends LazyDataModel<T> {
    private String filter;

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

    @Override
    public Object getRowKey(T item) {
        return item.getId();
    }
}
