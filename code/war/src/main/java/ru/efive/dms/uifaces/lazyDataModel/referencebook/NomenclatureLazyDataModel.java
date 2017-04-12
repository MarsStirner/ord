package ru.efive.dms.uifaces.lazyDataModel.referencebook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.referenceBook.Nomenclature;
import ru.hitsl.sql.dao.interfaces.referencebook.NomenclatureDao;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 18:32 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("nomenclatureLDM")
public class NomenclatureLazyDataModel extends AbstractFilterableLazyDataModel<Nomenclature> {

    @Autowired
    public NomenclatureLazyDataModel(@Qualifier("nomenclatureDao") NomenclatureDao nomenclatureDao) {
        super(nomenclatureDao);
    }
}
