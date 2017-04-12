package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;

import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("userLDM")
public class LazyDataModelForUser extends AbstractFilterableLazyDataModel<User> {

    private boolean showFired = false;

    @Autowired
    public LazyDataModelForUser(
            @Qualifier("userDao") UserDao userDao) {
        super(userDao);
    }

    public boolean isShowFired() {
        return showFired;
    }

    public void setShowFired(final boolean showFired) {
        this.showFired = showFired;
    }

    @Override
    @Transactional(value="ordTransactionManager", readOnly = true)
    public List<User> load(
            int first,
            int pageSize,
            String sortField,
            SortOrder sortOrder,
            Map<String, Object> filters
    ) {
        final UserDao dao = (UserDao) this.dao;
        setRowCount(dao.countItems(filter, false, showFired));
        if (getRowCount() < first) {
            first = 0;
        }
        return dao.getItems(filter, false, showFired, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
    }

}
