package ru.efive.dms.uifaces.lazyDataModel.referencebook;

import com.github.javaplugs.jsf.SpringScopeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.efive.dms.uifaces.beans.annotations.ViewScopedLazyDataModel;
import ru.efive.dms.uifaces.lazyDataModel.AbstractFilterableLazyDataModel;
import ru.entity.model.referenceBook.ContragentType;
import ru.hitsl.sql.dao.interfaces.referencebook.ContragentTypeDao;

/**
 * Author: Upatov Egor <br>
 * Date: 20.04.2015, 16:00 <br>
 * Company: Korus Consulting IT <br>
 * Description: <br>
 */
@ViewScopedLazyDataModel("contragentTypeLDM")
public class ContragentTypeLazyDataModel extends AbstractFilterableLazyDataModel<ContragentType> {
    @Autowired
    public ContragentTypeLazyDataModel(
            @Qualifier("contragentTypeDao") ContragentTypeDao contragentTypeDao) {
        super(contragentTypeDao);
    }
}