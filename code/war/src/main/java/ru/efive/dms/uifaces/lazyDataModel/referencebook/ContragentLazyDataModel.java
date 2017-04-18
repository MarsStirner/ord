package ru.efive.dms.uifaces.lazyDataModel.referencebook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
import ru.entity.model.referenceBook.Contragent;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentDao;

/**
 * Author: Upatov Egor <br>
 * Date: 19.11.2014, 16:33 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("contragentLDM")
public class ContragentLazyDataModel extends AbstractFilterableLazyDataModel<Contragent> {
    @Autowired
    public ContragentLazyDataModel(@Qualifier("contragentDao") ContragentDao contragentDao) {
        super(contragentDao);
    }
}
