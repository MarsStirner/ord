package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.entity.model.referenceBook.Group;
import ru.hitsl.sql.dao.interfaces.referencebook.GroupDao;

/**
 * Author: Upatov Egor <br>
 * Date: 17.04.2015, 17:37 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Component("groupLDM")
@SpringScopeView
public class LazyDataModelForGroup extends AbstractFilterableLazyDataModel<Group> {
    @Autowired
    public LazyDataModelForGroup(@Qualifier("groupDao")GroupDao groupDao) {
        super(groupDao);
    }
}