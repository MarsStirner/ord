package ru.efive.dms.uifaces.beans.officekeeping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.entity.model.document.OfficeKeepingFile;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;

/**
 * Author: Upatov Egor <br>
 * Date: 29.04.2015, 19:58 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("officeKeepingFileLDM")
public class OfficeKeepingFileLazyDataModel extends AbstractFilterableLazyDataModel<OfficeKeepingFile> {
    @Autowired
    public OfficeKeepingFileLazyDataModel(@Qualifier("officeKeepingFileDao") OfficeKeepingFileDao officeKeepingFileDao) {
        super(officeKeepingFileDao);
    }
}
