package ru.efive.dms.uifaces.beans.nomenclature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractDocumentHolderBean;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedController;
import ru.entity.model.referenceBook.Nomenclature;
import ru.hitsl.sql.dao.interfaces.referencebook.NomenclatureDao;
import ru.hitsl.sql.dao.util.AuthorizationData;

/**
 * Author: Upatov Egor <br>
 * Date: 16.02.2015, 13:29 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedController("nomenclature")
public class NomenclatureHolderBean extends AbstractDocumentHolderBean<Nomenclature, NomenclatureDao> {

    @Autowired
    public NomenclatureHolderBean(@Qualifier("nomenclatureDao") NomenclatureDao dao, @Qualifier("authData") AuthorizationData authData) {
        super(dao, authData);
    }

    @Override
    protected Nomenclature newModel(AuthorizationData authData) {
        return new Nomenclature();
    }


}
