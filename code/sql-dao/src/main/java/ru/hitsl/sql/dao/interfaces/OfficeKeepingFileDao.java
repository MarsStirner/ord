package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.document.OfficeKeepingFile;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:24 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface OfficeKeepingFileDao extends CommonDao<OfficeKeepingFile>{
    long countItemsByNumber(String fileIndex);
}
