package ru.efive.dms.uifaces.beans.officekeeping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.entity.model.document.OfficeKeepingVolume;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingVolumeDao;

/**
 * Author: Upatov Egor <br>
 * Date: 12.03.2015, 17:28 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("officeKeepingVolumeLDM")
public class OfficeKeepingVolumeLazyDataModel extends AbstractFilterableLazyDataModel<OfficeKeepingVolume> {
    @Autowired
    public OfficeKeepingVolumeLazyDataModel(@Qualifier("officeKeepingVolumeDao")final OfficeKeepingVolumeDao officeKeepingVolumeDao) {
        super(officeKeepingVolumeDao);
    }
}
