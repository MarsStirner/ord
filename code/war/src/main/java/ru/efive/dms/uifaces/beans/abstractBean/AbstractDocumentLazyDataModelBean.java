package ru.efive.dms.uifaces.beans.abstractBean;

import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.mapped.DeletableEntity;

import java.io.Serializable;

/**
 * Author: Upatov Egor <br>
 * Date: 31.03.2015, 16:35 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
public abstract class AbstractDocumentLazyDataModelBean<T extends DeletableEntity> implements Serializable {
    protected String filter;
    protected AbstractFilterableLazyDataModel<T> lazyModel;

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    public AbstractFilterableLazyDataModel<T> getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(AbstractFilterableLazyDataModel<T> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public void applyFilter() {
        getLazyModel().setFilter(filter);
    }

}
