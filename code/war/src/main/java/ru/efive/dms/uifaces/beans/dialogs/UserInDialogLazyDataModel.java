package ru.efive.dms.uifaces.beans.dialogs;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.entity.model.referenceBook.Group;
import ru.entity.model.user.User;
import ru.hitsl.sql.dao.interfaces.UserDao;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupDao;

import javax.json.JsonObject;
import java.util.List;
import java.util.Map;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("userDialogLDM")
public class UserInDialogLazyDataModel extends AbstractFilterableLazyDataModel<User> {
    @Autowired
    @Qualifier("groupDao")
    private GroupDao groupDao;

    private Group filterGroup;
    private boolean showFired = false;

    @Autowired
    public UserInDialogLazyDataModel(
            @Qualifier("userDao") UserDao userDao) {
        super(userDao);
    }

    public boolean isShowFired() {
        return showFired;
    }

    public void setShowFired(final boolean showFired) {
        this.showFired = showFired;
    }

    public Group getFilterGroup() {
        return filterGroup;
    }

    public void setFilterGroup(Group filterGroup) {
        this.filterGroup = filterGroup;
    }

    public void initWithSettings(JsonObject settings) {
        clearFilters();
        if (settings == null) {
            return;
        }
        initializeGroup(settings.getString("group", null));
    }

    public void initializeGroup(final String code) {
        if (StringUtils.isNotEmpty(code)) {
            log.info("UserSelectDialog: initialized with group code=\'{}\'", code);
            final Group group = groupDao.getByCode(code);
            if (group != null) {
                log.info("UserSelectDialog: initialized with group [{}]", group);
                setFilterGroup(group);
            } else {
                log.error("UserSelectDialog: group not founded by code= \'{}\'", code);
            }
        }
    }


    @Override
    public List<User> load(
            int first,
            int pageSize,
            String sortField,
            SortOrder sortOrder,
            Map<String, Object> filters
    ) {
        final UserDao userDao = (UserDao) this.dao;
        if (filterGroup == null) {
            setRowCount(userDao.countItems(filter, false, showFired));
            if (getRowCount() < first) {
                first = 0;
            }
            return userDao.getItemsForDialog(filter, false, showFired, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
        } else {
            setRowCount(userDao.countItemsForDialogByGroup(filter, false, showFired, filterGroup));
            if (getRowCount() < first) {
                first = 0;
            }
            return userDao.getItemsForDialogByGroup(filter, false, showFired, filterGroup, first, pageSize, sortField, SortOrder.ASCENDING.equals(sortOrder));
        }
    }

    @Override
    public void clearFilters() {
        super.clearFilters();
        filterGroup = null;
        showFired = false;
    }
}
