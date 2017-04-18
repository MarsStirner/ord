package ru.efive.dms.uifaces.lazyDataModel.referencebook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
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
