package ru.efive.dms.uifaces.lazyDataModel;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.beans.abstractBean.AbstractFilterableLazyDataModel;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.entity.model.document.Numerator;
import ru.hitsl.sql.dao.interfaces.NumeratorDao;
import ru.hitsl.sql.dao.interfaces.mapped.CommonDao;

import java.io.Serializable;

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
