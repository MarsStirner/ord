package ru.efive.dms.uifaces.beans.position;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.referenceBook.Position;
import ru.hitsl.sql.dao.interfaces.referencebook.PositionDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 18.09.2014, 12:13 <br>
 * Company: Korus Consulting IT <br>
 * Description: Бин для работы с должностью<br>
 */

@ViewScopedController("position")
public class PositionHolderBean extends AbstractDocumentHolderBean<Position, PositionDao> {


    @Autowired
    public PositionHolderBean(
            @Qualifier("positionDao") PositionDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    protected Position newModel(AuthorizationData authData) {
        final Position result = new Position();
        result.setDeleted(false);
        result.setValue("");
        return result;
    }

    @Override
    public boolean isCanCreate(AuthorizationData authData) {
        return authData.isAdministrator();
    }

    @Override
    public boolean isCanUpdate(AuthorizationData authData) {
        return authData.isAdministrator();
    }

    @Override
    public boolean isCanDelete(AuthorizationData authData) {
        return authData.isAdministrator();
    }
}
