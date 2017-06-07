package ru.efive.dms.uifaces.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.entity.model.numerator.Numerator;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 18:05 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */

@ViewScopedLazyDataModel("numeratorLDM")
public class NumeratorLazyDataModel extends AbstractFilterableLazyDataModel<Numerator> {
    @Autowired
    public NumeratorLazyDataModel(@Qualifier("numeratorDao")NumeratorDao numeratorDao) {
        super(numeratorDao);
    }
}
