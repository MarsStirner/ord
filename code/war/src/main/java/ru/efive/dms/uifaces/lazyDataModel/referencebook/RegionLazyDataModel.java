package ru.efive.dms.uifaces.lazyDataModel.referencebook;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.referenceBook.Region;
import ru.hitsl.sql.dao.interfaces.referencebook.RegionDao;

/**
 * Author: Upatov Egor <br>
 * Date: 28.04.2015, 15:25 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("regionLDM")
public class RegionLazyDataModel extends AbstractFilterableLazyDataModel<Region> {
    @Autowired
    public RegionLazyDataModel(@Qualifier("regionDao")RegionDao regionDao) {
        super(regionDao);
    }
}
