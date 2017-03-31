package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.entity.model.document.OfficeKeepingFile;
import ru.hitsl.sql.dao.interfaces.OfficeKeepingFileDao;

/**
 * Author: Upatov Egor <br>
 * Date: 29.04.2015, 19:58 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@Component("officeKeepingFileLDM")
@SpringScopeView
public class LazyDataModelForOfficeKeepingFile extends AbstractFilterableLazyDataModel<OfficeKeepingFile> {
    @Autowired
    public LazyDataModelForOfficeKeepingFile(@Qualifier("officeKeepingFileDao") OfficeKeepingFileDao officeKeepingFileDao) {
        super(officeKeepingFileDao);
    }
}
