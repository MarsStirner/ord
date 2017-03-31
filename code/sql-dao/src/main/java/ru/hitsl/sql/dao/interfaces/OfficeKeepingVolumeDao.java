package ru.hitsl.sql.dao.interfaces;

import ru.entity.model.document.OfficeKeepingVolume;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

import java.util.List;

/**
 * Author: Upatov Egor <br>
 * Date: 30.03.2017, 19:14 <br>
 * Company: Bars Group [ www.bars.open.ru ]
 * Description:
 */
public interface OfficeKeepingVolumeDao extends CommonDao<OfficeKeepingVolume> {
    List<OfficeKeepingVolume> findAllVolumesByParentId(String parentId);
}
